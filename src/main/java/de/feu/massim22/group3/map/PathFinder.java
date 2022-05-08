package de.feu.massim22.group3.map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.*;

import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

class PathFinder {
	private int gComputeProgram;
	private long windowHandler;
	
	PathFinder(long windowHandler) {
		this.windowHandler = windowHandler;
	}

	static void init() {
		// Init GLFW Context - this must be done from the main thread and is therefore in the constructor
		if (!glfwInit()) {
			throw new IllegalStateException("Can't init GLFW");
		}
	}

	static void close() {
		// Free Resources
		glfwTerminate(); 
	}

	static long createOpenGlContext() {
		// Create Hidden Window to get OpenGL Context
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		ByteBuffer buf = BufferUtils.createByteBuffer(200);
		long win = glfwCreateWindow(200, 200, buf, 0, 0);
		glfwMakeContextCurrent(win);
		GL.createCapabilities();
		glfwMakeContextCurrent(0);
		
		// Log OpenGL Details
		StringBuilder b = new StringBuilder()
		.append("GL_VENDOR: " + glGetString(GL_VENDOR))
		.append(System.lineSeparator())
		.append("GL_RENDERER: " + glGetString(GL_RENDERER))
		.append(System.lineSeparator())
		.append("GL_VERSION: " + glGetString(GL_VERSION));
		AgentLogger.fine(b.toString());

		return win;
	} 
	
	private void create2dTexture(FloatBuffer imageData, int channelSize, int bindingIndex, int imageSizeX, int imageSizeY, boolean readonly) {
		int internalTextureFormat;
		int inputTextureFormat;
		switch (channelSize) {
		case 1:
			internalTextureFormat = GL_R32F;
			inputTextureFormat = GL_RED;
			break;
		case 2:
			internalTextureFormat = GL_RG32F;
			inputTextureFormat = GL_RG;
			break;
		default:
			internalTextureFormat = GL_RGBA32F;
			inputTextureFormat = GL_RGBA;
		}
        int texture = glGenTextures();
        glActiveTexture( GL_TEXTURE0 );
        glBindTexture( GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, internalTextureFormat, imageSizeX, imageSizeY, 0, inputTextureFormat, GL_FLOAT, imageData);
        
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        
        int readWrite = readonly ? GL_READ_ONLY : GL_READ_WRITE;
        glBindImageTexture( bindingIndex, texture, 0, false, 0, readWrite, internalTextureFormat );
        
        // Bind the compute program.
        glUseProgram(gComputeProgram);
	}
	
	private void create3dTexture(FloatBuffer imageData, int channelSize, int bindingIndex, int imageSizeX, int imageSizeY, int imageSizeZ, boolean readonly) {
		int internalTextureFormat;
		int inputTextureFormat;
		switch (channelSize) {
		case 1:
			internalTextureFormat = GL_R32F;
			inputTextureFormat = GL_RED;
			break;
		case 2:
			internalTextureFormat = GL_RG32F;
			inputTextureFormat = GL_RG;
			break;
		default:
			internalTextureFormat = GL_RGBA32F;
			inputTextureFormat = GL_RGBA;
		}
        int texture = glGenTextures();
        glActiveTexture( GL_TEXTURE0 );
        glBindTexture( GL_TEXTURE_3D, texture);
        glTexImage3D(GL_TEXTURE_3D, 0, internalTextureFormat, imageSizeX, imageSizeY, imageSizeZ, 0, inputTextureFormat, GL_FLOAT, imageData);
        
        glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        
        int readWrite = readonly ? GL_READ_ONLY : GL_READ_WRITE;
        glBindImageTexture( bindingIndex, texture, 0, true, 0, readWrite, internalTextureFormat );
        
        // Bind the compute program.
        glUseProgram(gComputeProgram);
	}
	
	private void testForErrors() {
	    int error = glGetError();
	    if (error != 0) { 
            AgentLogger.severe("Error " + glGetError());
	    }
	}
    
	private void initShader(int goalCount, Point mapSize, boolean mapDiscovered) throws IOException {
        // Create and compile the compute shader.
        int mComputeShader = glCreateShader(GL_COMPUTE_SHADER);
        String shader = getResourceFileAsString("shader.glsl");
        
        // set shader variables before compilation
        shader = shader
        	.replaceFirst("VAR1", "1") 	   // local Cores X
        	.replaceFirst("VAR2", goalCount + "") 	  // local Cores Y (goal Count)
        	.replaceFirst("VAR3", "1") 		// local Cores Z
        	.replaceFirst("VAR4", "300") 	// Queue Size 
        	.replaceFirst("VAR5", "4")		// Max values in Queue list
        	.replaceFirst("VAR6", mapSize.x + "")		   // Map size X 
        	.replaceFirst("VAR7", mapSize.y + "")        // Map size Y
			.replaceFirst("VAR8", mapDiscovered + "");  // Map discovered
        		
        GL20.glShaderSource(mComputeShader, shader);
        glCompileShader(mComputeShader);
        
        IntBuffer errorBuffer = BufferUtils.createIntBuffer(2);
        glGetShaderiv(mComputeShader, GL_COMPILE_STATUS, errorBuffer);
        
        String s = glGetShaderInfoLog(mComputeShader);
        
        // Attach and link the shader against the compute program.
        glAttachShader(gComputeProgram, mComputeShader);
        glLinkProgram(gComputeProgram);
        
        // Check if there were any issues linking the shader.
        glGetProgramiv(gComputeProgram, GL_LINK_STATUS, errorBuffer);
        
        String info = glGetProgramInfoLog(gComputeProgram, 100);

        int[] status = new int[2];
        errorBuffer.get(status);
        
        StringBuilder b = new StringBuilder()
            .append(System.lineSeparator())
            .append("ShaderInfoLog: " + s)
            .append(System.lineSeparator())
            .append("ProgramInfoLog: " + info)
            .append(System.lineSeparator())
            .append("ShaderCompileStatus: " + status[0])
            .append(System.lineSeparator())
            .append("ShaderLinkStatus: " + status[1])
            .append(System.lineSeparator());
        
        AgentLogger.fine(b.toString());
	}
	
    public String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    public void start(FloatBuffer mapBuffer, FloatBuffer dataBuffer, Point mapSize, Point dataSize, int agentCount, int goalCount, boolean mapDiscovered, String supervisor, int step) {

        // Create the compute program the compute shader is assigned to
		glfwMakeContextCurrent(this.windowHandler);
		GL.createCapabilities();
        gComputeProgram = glCreateProgram();

        try {
            initShader(goalCount, mapSize, mapDiscovered);
        } catch(IOException e) {
            AgentLogger.severe(e.getLocalizedMessage());
        }
        
        // Agent Data (x, y, colorChannel)
		/*
        float[][][] agentData = new float[agentSize + 10][goalNumber + 1][2];
        
        for (int i = 0; i < agentSize; i++) {
        	// Agent Form
        	agentData[1][0][0] = 100;
        	// Agent Position
        	agentData[i + 10][0][0] = i;
        	agentData[i + 10][0][1] = i;
        	// Agent Goals
        	for(int j = 0; j < goalNumber; j++) {
            	agentData[i + 10][j+1][0] = Math.round(Math.random() * mapSizeX);
            	agentData[i + 10][j+1][1] = Math.round(Math.random() * mapSizeY);
        	}
        }
		*/
		/*
        FloatBuffer agentDataBuffer = BufferUtils.createFloatBuffer((agentSize + 10) * (goalNumber + 1) * 2);
        for (int i = 0; i < goalNumber + 1; i++) {
        	for (int j = 0; j < agentSize + 10; j++) {
        		agentDataBuffer.put(agentData[j][i][0]);
        		agentDataBuffer.put(agentData[j][i][1]);
        	}
        }

        agentDataBuffer.flip();
		*/

		// Map and Output Texture
		create3dTexture(mapBuffer, 2, 0, mapSize.x, mapSize.y, agentCount + 1, false);

		// Data Input Texture
        create2dTexture(dataBuffer, 2, 1, dataSize.x, dataSize.y, true);
        
        // Set Timer
        long start = System.currentTimeMillis();
        
        glDispatchCompute(agentCount, 1, 1);
        
        // Wait until calculation ends
	    glMemoryBarrier( GL_ALL_BARRIER_BITS );
	    
	    testForErrors();
	    
		// Get Result
		float[] result = new float[mapSize.y * mapSize.x * (agentCount + 1) * 2];
	    glGetTexImage(GL_TEXTURE_3D, 0, GL_RG, GL_FLOAT, result);
	    		
        // Log Time spent
	    long end = System.currentTimeMillis();
	    long diff = end - start;
        AgentLogger.fine("Path Finding Duration: " + diff);

		// Image logging
		// TODO add logging parameter
		if (true) {
			logMap(mapSize, result, supervisor, step);
		}
		
		// Remove Context from thread
		glfwMakeContextCurrent(0);
    } 

	private void logMap(Point mapSize, float[] result, String supervisor, int step) {
		// Translate to RGBA
		int s = mapSize.x * mapSize.y;
		float[] c = new float[s * 4];
		int offset = s / 2;
		for (int i = 0; i < s; i++) {
			c[i*4] = (int)(result[i*2+offset] * 255);       //r
			c[i*4+1] = (int)(result[i*2+offset+1] * 255);   //g
			c[i*4+2] = 0; 				                    //b
			c[i*4+3] = 255;                                 //a
		}
		
		// Check if folder exists
		String folder = "logs/map";
		Path path = Paths.get(folder);
		try {
			if (!Files.exists(path)){
				Files.createDirectories(path);
			}
			//Save the screen image
			String format = "png";
			File file = new File(folder + "/map_supervisor" + supervisor + "_step" + step + "." + format);
			BufferedImage image = new BufferedImage(mapSize.x, mapSize.y, BufferedImage.TYPE_INT_ARGB);
			
			WritableRaster raster = image.getRaster();
			raster.setPixels(raster.getMinX() , raster.getMinY(), raster.getWidth(), raster.getHeight(), c);
			
			ImageIO.write(image, format, file);
		} catch (IOException e) { 
			e.printStackTrace();
		}
	}
}


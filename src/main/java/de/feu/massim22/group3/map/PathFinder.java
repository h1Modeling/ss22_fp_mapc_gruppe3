package de.feu.massim22.group3.map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.*;

import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

class PathFinder implements Runnable {
	private final ByteBuffer buf = BufferUtils.createByteBuffer(200);
	private int gComputeProgram;
	private int mapSizeX = 100;
	private int mapSizeY = 100;
	private int goalNumber = 32;
	private int agentSize = 51; // +1 for Input
	private int inputTexId = 0;
	

	PathFinder() { 
	}
	
	private void logResult() {

		float[] b = new float[mapSizeX * mapSizeY];

		glEnable(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, this.inputTexId);
		
	    glGetTexImage(GL_TEXTURE_2D, 0, GL_RED, GL_FLOAT, b);

		for (int i = 0; i < mapSizeY; i++) {
			for (int j = 0; j < mapSizeX; j++) {
				System.out.print(b[i * mapSizeY + j] + " ");
			}
			System.out.println();			
		}	
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
	
	private void createOpenGlContext() {
		// Configure GLFW
		if (!glfwInit()) {
			throw new IllegalStateException("Can't init GLFW");
		}
		
		// Create Hidden Window to get OpenGL Context
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		long win = glfwCreateWindow(200, 200, buf, 0, 0);
		glfwMakeContextCurrent(win);
		
		GL.createCapabilities();
		
        StringBuilder b = new StringBuilder()
            .append("GL_VENDOR: " + glGetString(GL_VENDOR))
            .append(System.lineSeparator())
            .append("GL_RENDERER: " + glGetString(GL_RENDERER))
            .append(System.lineSeparator())
            .append("GL_VERSION: " + glGetString(GL_VERSION));

        AgentLogger.fine(b.toString());
	}
    
	private void initShader() throws IOException {
        // Create and compile the compute shader.
        int mComputeShader = glCreateShader(GL_COMPUTE_SHADER);
        String shader = getResourceFileAsString("shader.glsl");
        
        // set shader variables before compilation
        // TODO set correct values
        shader = shader
        	.replaceFirst("VAR1", "1") 	    // Cores X (agents)
        	.replaceFirst("VAR2", "1") 		// Cores Y
        	.replaceFirst("VAR3", "1") 		// Cores Z
        	.replaceFirst("VAR4", "300") 	// Queue Size 
        	.replaceFirst("VAR5", "4")		// Max values in Queue list
        	.replaceFirst("VAR6", mapSizeX + "")		   // Map size X 
        	.replaceFirst("VAR7", mapSizeY + "");       // Map size Y
        		
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

    @Override
    public void run() {
		createOpenGlContext();
		
        // Create the compute program the compute shader is assigned to
        gComputeProgram = glCreateProgram();

        try {
            initShader();
        } catch(IOException e) {
            AgentLogger.severe(e.getLocalizedMessage());
        }

        // Create Input Data
        FloatBuffer imageData = BufferUtils.createFloatBuffer(mapSizeX * mapSizeY * agentSize * 2);
        
        for (int k = 0; k < agentSize; k++) {
            for (int i = 0; i < mapSizeY; i++) {
            	for (int j = 0; j < mapSizeX; j++) {
            		float v = k == 0 ? Math.round(Math.random() / 1.5) : 0;
            		imageData.put(v);
            		imageData.put(0.0f);
            	}
            }
        }
        imageData.flip();
        
        // Map Input
        create3dTexture(imageData, 2, 0, mapSizeX, mapSizeY, agentSize, false);
        
        // Agent Data (x, y, colorChannel)
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

        FloatBuffer agentDataBuffer = BufferUtils.createFloatBuffer((agentSize + 10) * (goalNumber + 1) * 2);
        for (int i = 0; i < goalNumber + 1; i++) {
        	for (int j = 0; j < agentSize + 10; j++) {
        		agentDataBuffer.put(agentData[j][i][0]);
        		agentDataBuffer.put(agentData[j][i][1]);
        	}
        }

        agentDataBuffer.flip();

        create2dTexture(agentDataBuffer, 2, 1, agentSize + 10, goalNumber + 1, true);
        
        // Set Timer
        long start = System.currentTimeMillis();
        
        glDispatchCompute( agentSize - 1 , goalNumber, 1 );
        
        // Wait until calculation ends
	    glMemoryBarrier( GL_ALL_BARRIER_BITS );
	    
	    testForErrors();
	    
		float[] b = new float[mapSizeY * mapSizeX * agentSize * 2];
	    glGetTexImage(GL_TEXTURE_3D, 0, GL_RG, GL_FLOAT, b);
	    
	    int s = mapSizeX * mapSizeY * 4;
	    float[] c = new float[s];
	    int offset = s / 2;
	    for (int i = 0; i < s / 4; i++) {
	    	int value = (int)b[i*2+offset];
	    	c[i*4] = value == 0 ? 0 : value + 150;      //r
	    	c[i*4+1] = b[i*2+offset+1];  //g
	    	c[i*4+2] = 0; 				 //b
	    	c[i*4+3] = 255;              //a
	    }
	    
	    for (int k = 0; k < agentSize; k++) {
			System.out.println("");
			String header = k == 0 ? "Map:" : "Agent: " + k;
			System.out.println(header);
			System.out.println("------------------------------");
			for (int i = 0; i < mapSizeY; i++) {
				for (int j = 0; j < mapSizeX; j++) {
					System.out.print(b[(k * mapSizeX * mapSizeY + i * mapSizeY + j) * 2] + " " + b[(k * mapSizeX * mapSizeY + i * mapSizeY + j) * 2 + 1] + " | ");
				}
				System.out.println();
				System.out.println("--------|---------|---------|-");			
			}
	    }
		
        // Log Time spent
	    long end = System.currentTimeMillis();
	    long diff = end - start;
        AgentLogger.fine("Path Finding Duration: " + diff);
	    
        //Save the screen image
        /*
        File file = new File("output.png"); // The file to save to.
        String format = "png"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(mapSizeX, mapSizeY, BufferedImage.TYPE_INT_ARGB);
        
        WritableRaster raster = image.getRaster();
        raster.setPixels(raster.getMinX() , raster.getMinY(), raster.getWidth(), raster.getHeight(), c);
           
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
	    */
	    
        // Free Resources
        glfwTerminate(); 
    }    
}


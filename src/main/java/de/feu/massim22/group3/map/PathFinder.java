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
import java.util.List;
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
    private String shaderCache;
    
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
        long start = System.currentTimeMillis();
        glLinkProgram(gComputeProgram);
        long end = System.currentTimeMillis();
        long diff = end - start;
        //AgentLogger.info("Linking Duration: " + diff);
        
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
        // Load from file
        if (shaderCache == null) {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            try (InputStream is = classLoader.getResourceAsStream(fileName)) {
                if (is == null) return null;
                try (InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader reader = new BufferedReader(isr)) {
                        shaderCache = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }
        }
        return shaderCache;
    }

    public PathFindingResult[][] start(FloatBuffer mapBuffer, FloatBuffer dataBuffer, List<InterestingPoint> goalPoints, Point mapSize, Point dataSize, int agentCount, int goalCount, boolean mapDiscovered, String supervisor, int step) {

        // Set Timer
        long start = System.currentTimeMillis();
        
        // Create the compute program the compute shader is assigned to
        glfwMakeContextCurrent(this.windowHandler);
        GL.createCapabilities();
        gComputeProgram = glCreateProgram();

        try {
            initShader(goalCount, mapSize, mapDiscovered);
        } catch(IOException e) {
            AgentLogger.severe(e.getLocalizedMessage());
        }

        // Map and Output Texture
        create3dTexture(mapBuffer, 2, 0, mapSize.x, mapSize.y, agentCount + 1, false);

        // Data Input Texture
        create2dTexture(dataBuffer, 2, 1, dataSize.x, dataSize.y, true);
        
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
        AgentLogger.info("Path Finding Duration: " + diff);

        // Image logging
        // TODO add logging parameter
        if (true) {
            logMap(mapSize, result, supervisor, step);
        }
        
        // Remove Context from thread
        glfwMakeContextCurrent(0);

        // Calculate Result
        return decodeResult(result, goalPoints, mapSize, agentCount);
    }

    private PathFindingResult[][] decodeResult(float[] map, List<InterestingPoint> goalPoints, Point mapSize, int agentCount) {
        int channels = 2;
        int imageSize2D = mapSize.x * mapSize.y * channels;
        PathFindingResult[][] result = new PathFindingResult[agentCount][goalPoints.size()];
        for (int i = 0; i < agentCount; i++) {
            int startIndex = (i + 1) * imageSize2D;
            for (int j = 0; j < goalPoints.size(); j++) {
                InterestingPoint ip = goalPoints.get(j);
                Point p = ip.point();
                int index = startIndex + p.y * mapSize.x * 2 + p.x * 2;
                int distance = (int)map[index];
                int direction = (int)map[index + 1];
                PathFindingResult resultData = new PathFindingResult(distance, direction);
                result[i][j] = resultData;
                // AgentLogger.fine("Path-Finding Result: " + ip.cellType() + " Distance: " + distance + " Direction " + direction);
            }
        }
        return result;
    }

    private void logMap(Point mapSize, float[] result, String supervisor, int step) {
        // Translate to RGBA
        int s = mapSize.x * mapSize.y;
        // Map
        float[] c = new float[s * 4];
        for (int i = 0; i < s; i++) {
            c[i*4] = (int)(result[i*2] * 255);       //r
            c[i*4+1] = (int)(result[i*2+1] * 255);   //g
            c[i*4+2] = 0; 				             //b
            c[i*4+3] = 255;                          //a
        }
        // Agent 1 Result
        float[] a = new float[s * 4];
        int offset = (s * 2);

        for (int i = 0; i < s; i++) {
            boolean noResult = result[offset + i*2] == 0;
            a[i*4] = noResult ? 255 : (int)(result[offset + i*2] * 10); //r
            a[i*4+1] = noResult ? 255 : (int)(result[offset + i*2+1]);  //g
            a[i*4+2] = 255; 				                            //b
            a[i*4+3] = 255;                                             //a
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

            File fileA = new File(folder + "/map_supervisor" + supervisor + "_step" + step + "_result." + format);
            BufferedImage imageA = new BufferedImage(mapSize.x, mapSize.y, BufferedImage.TYPE_INT_ARGB);
            WritableRaster rasterA = imageA.getRaster();
            rasterA.setPixels(rasterA.getMinX() , rasterA.getMinY(), rasterA.getWidth(), rasterA.getHeight(), a);
            ImageIO.write(imageA, format, fileA);

        } catch (IOException e) { 
            e.printStackTrace();
        }
    }
}


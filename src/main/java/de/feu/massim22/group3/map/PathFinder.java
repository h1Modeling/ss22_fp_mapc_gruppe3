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

/**
 * The Class <code>PathFinder</code> handles the communication with the OpenGL library GLFW to create and
 * manage OpenGL Contexts. The Contexts are used to perform calculations by a compute shader to receive 
 * path finding information. 
 *
 * @author Heinz Stadler
 */
class PathFinder {
    private int gComputeProgram = -1;
    private long windowHandler;
    private static String shader;
    private boolean debug;
    
    /**
     * Instantiates a new PathFinder.
     * 
     * @param windowHandler the handler to the openGl context
     * @param debug true if the PathFinder should write images of the path finding result to the log/map folder.
     */
    PathFinder(long windowHandler, boolean debug) {
        this.windowHandler = windowHandler;
        this.debug = debug;
    }

    /**
     * Instantiates the GLFW library and loads the compute shader.
     * This method must be called from the main thread.
     */
    static void init() {
        // Init GLFW Context - this must be done from the main thread and is therefore in the constructor
        if (!glfwInit()) {
            throw new IllegalStateException("Can't init GLFW");
        }
        // Load Shader Code
        try {
            shader = getResourceFileAsString("shader3.glsl");
        } catch (IOException e) {
            AgentLogger.severe("Failed to load PathFinding Shader - " + e.getLocalizedMessage());
        }
    }

    /**
     * Frees the resources used by the OpenGL context.
     *  
     * @param context the handler to the OpenGL context
     */
    static void close(long context) {
        // Free Resources
        glfwDestroyWindow(context);
        //glfwTerminate(); // Destroys all windows
    }

    /**
     * Creates a new OpenGL context.
     * @return the handler to the OpenGL context
     */
    static long createOpenGlContext() {
        // Create Hidden Window to get OpenGL Context
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        ByteBuffer buf = BufferUtils.createByteBuffer(200);
        long win = glfwCreateWindow(20, 20, buf, 0, 0);
        glfwMakeContextCurrent(win);
        GL.createCapabilities();

        // Log OpenGL Details
        StringBuilder b = new StringBuilder()
        .append("GL_VENDOR: " + glGetString(GL_VENDOR))
        .append(System.lineSeparator())
        .append("GL_RENDERER: " + glGetString(GL_RENDERER))
        .append(System.lineSeparator())
        .append("GL_VERSION: " + glGetString(GL_VERSION));
        AgentLogger.fine(b.toString());

        glfwMakeContextCurrent(0);

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
        
        GL20.glShaderSource(mComputeShader, shader);
        glCompileShader(mComputeShader);
        IntBuffer errorBuffer = BufferUtils.createIntBuffer(2);
        glGetShaderiv(mComputeShader, GL_COMPILE_STATUS, errorBuffer);
        String s = glGetShaderInfoLog(mComputeShader);
        testForErrors();
        // Attach and link the shader against the compute program.
        glAttachShader(gComputeProgram, mComputeShader);
        testForErrors();
        long start = System.currentTimeMillis();
        glLinkProgram(gComputeProgram);
        testForErrors();
        long end = System.currentTimeMillis();
        long diff = end - start;
        AgentLogger.info("Linking Duration: " + diff);
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
        
        AgentLogger.info(b.toString());
    }
    
    private static String getResourceFileAsString(String fileName) throws IOException {
        // Load from file
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    /**
     * Starts the path finding.
     * 
     * @param mapBuffer the buffer in which obstacles are stored as 1.0f and free cells are stored as 0.0f
     * @param dataBuffer the buffer with information about agent position and attached blocks to the agent
     * @param goalPoints a List of Points for which path finding should be started
     * @param mapSize the size of the map input texture
     * @param dataSize the size of the data input texture
     * @param agentCount the number of agents 
     * @param goalCount the number of goals
     * @param mapDiscovered indicates if the map size is already discovered or not 
     * @param supervisor the supervisor of the group for which the path finding should be started
     * @param step the current step of the simulation
     * @return a two dimensional Array of path finding results containing the distance and direction of every goal point from every agent
     */
    public PathFindingResult[][] start(FloatBuffer mapBuffer, FloatBuffer dataBuffer, List<InterestingPoint> goalPoints, Point mapSize, Point dataSize, int agentCount, int goalCount, boolean mapDiscovered, String supervisor, int step) {

        try {
            // Set Timer
            long start = System.currentTimeMillis();
            
            // Create the compute program the compute shader is assigned to
            glfwMakeContextCurrent(this.windowHandler);
            GL.createCapabilities();
            if (gComputeProgram == -1) {
                gComputeProgram = glCreateProgram();

                try {
                    initShader(goalCount, mapSize, mapDiscovered);
                } catch(IOException e) {
                    AgentLogger.severe(e.getLocalizedMessage());
                }
            }

            // Map and Output Texture
            create3dTexture(mapBuffer, 2, 0, mapSize.x, mapSize.y, agentCount + 1, false);

            // Data Input Texture
            create2dTexture(dataBuffer, 2, 1, dataSize.x, dataSize.y, true);
            
            glDispatchCompute(agentCount, goalCount, 1);
            //glDispatchCompute(1, 1, 1);
            
            // Wait until calculation ends
            glMemoryBarrier(GL_PIXEL_BUFFER_BARRIER_BIT);
            //glMemoryBarrier( GL_ALL_BARRIER_BITS );
            
            testForErrors();
            
            // Get Result
            float[] result = new float[mapSize.y * mapSize.x * (agentCount + 1) * 2];
            glGetTexImage(GL_TEXTURE_3D, 0, GL_RG, GL_FLOAT, result);
                    
            // Log Time spent
            long end = System.currentTimeMillis();
            long diff = end - start;
            AgentLogger.info("Path Finding Duration: " + diff);

            // Image logging
            if (debug) {
                logMap(mapSize, result, supervisor, step);
            }
            
            // Remove Context from thread
            glfwMakeContextCurrent(0);

            // Calculate Result
            return decodeResult(result, goalPoints, mapSize, agentCount);
        } catch (IllegalStateException e) {
            // If the program gets terminated during calculation the context will be gone and this exception will be fired
            float[] result = new float[mapSize.y * mapSize.x * (agentCount + 1) * 2];
            return decodeResult(result, goalPoints, mapSize, agentCount);
        }
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
                if (index >= 0 && index < map.length) {
                    int distance = (int)map[index];
                    int direction = (int)map[index + 1];
                    PathFindingResult resultData = new PathFindingResult(distance, direction);
                    result[i][j] = resultData;
                } else {
                    result[i][j] = new PathFindingResult(0, 1);
                }
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


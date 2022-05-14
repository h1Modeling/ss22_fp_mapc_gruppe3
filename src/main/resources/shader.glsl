#version 430
layout(local_size_x = VAR1, local_size_y = VAR2, local_size_z = VAR3) in;
layout(rg32f, binding = 0) uniform image3D map;
layout(rg32f, binding = 1) uniform image2D data;
// VAR Values have to be changed at compile time
const int queueSize = VAR4;
const int queueSize2 = VAR5;
const ivec2 mapSize = ivec2(VAR6,VAR7);
const bool sizeDiscovered = VAR8;

ivec3 getShortest(inout vec3[queueSize2] value, inout int curMin) {
    int i;
    for (i = 0; i < queueSize2; i++) {
        if (value[i].x != -1) {
            ivec3 result = ivec3(value[i]);
            value[i] = vec3(-1, -1, -1);
            if (value[0].x == -1 && value[1].x == -1 && value[2].x == -1) {
                curMin = 300;
            }
            return result;
        }
    }
    return ivec3(-1,-1,-1);
}

void add(inout vec3[queueSize2] array, vec3 value) {
    int i;
    for (i = 0; i < queueSize2; i++) {
        if (array[i].x == -1) {
            array[i] = value;
            break;
        }
    }
}

void main() {
    int agent = int(gl_GlobalInvocationID.x);
    int agentImageId = agent + 1;
    int task = int(gl_GlobalInvocationID.y);

    int i; // general iterator
    int j; // general second iterator
    
    // Array Structure to simulate priority queue
    int curMin = 0;
    vec3 distances[queueSize][queueSize2];
    
    // Initialize priority queue
    for (i = 0; i < queueSize; i=i+1) {
        for (j = 0; j < queueSize2; j=j+1) {
            distances[i][j] = vec3(-1,-1,-1);
        }
    }
    
    // Array Structure to save which cells are visited ([x][y])
    bool visited[mapSize.x][mapSize.y];
    for (i = 0; i < mapSize.x; i++) {
        for (j = 0; j < mapSize.y; j++) {
            visited[i][j] = false;
        }
    }
    
    // position agent
    ivec3 start = ivec3(imageLoad(data, ivec2(agent, 0)).rg, 0);
    // position goal
    ivec3 end = ivec3(imageLoad(data, ivec2(task, 2)).rg, 0); 
    
    distances[0][0] = start;
    visited[start.x][start.y] = true;
    
    // float mapValue = imageLoad(map, ivec3(start.xy, 0)).r;
    int iteration = 1;
    int dirFactor = 1;
    while (true) {
        ivec3 shortest = getShortest(distances[curMin], curMin);
        ivec3 pixelShortest = ivec3( shortest.xy, agentImageId );
        float pixelShortestValue = imageLoad( map, pixelShortest ).r;
        
        // Stop if node list is empty or if node is end node
        if (shortest.xy == end.xy || shortest.x == -1) {
            imageStore( map, ivec3(end.xy, agentImageId), vec4( pixelShortestValue, shortest.z, 0, 0) );
            break;
        }
        
        // Saves neighbour values
        ivec4 pixels[4];
        if (sizeDiscovered) {
            // Right
            pixels[0] = ivec4( (shortest.x + 1) % mapSize.x, shortest.y, shortest.z + dirFactor * 2, 0 );
            // Bottom
            pixels[1] = ivec4( shortest.x, (shortest.y + 1) % mapSize.y, shortest.z + dirFactor * 3, 0 );
            // Left
            pixels[2] = ivec4( (shortest.x - 1 + mapSize.x) % mapSize.x, shortest.y, shortest.z + dirFactor * 4, 0 );
            // Top
            pixels[3] = ivec4( shortest.x, (shortest.y - 1 + mapSize.y) % mapSize.y, shortest.z + dirFactor, 0 );
        } else {
            // Right
            int rightCheck = shortest.x + 1 < mapSize.x ? 0 : 1;
            pixels[0] = ivec4( shortest.x + 1, shortest.y, shortest.z + dirFactor * 2, rightCheck );
            // Bottom
            int bottomCheck = shortest.y + 1 < mapSize.y ? 0 : 1;
            pixels[1] = ivec4( shortest.x, shortest.y + 1, shortest.z + dirFactor * 3, bottomCheck );
            // Left
            int leftCheck = shortest.x - 1 >= 0 ? 0 : 1;
            pixels[2] = ivec4( shortest.x - 1, shortest.y, shortest.z + dirFactor * 4, leftCheck );
            // Top
            int topCheck = shortest.y - 1 >= 0 ? 0 : 1;
            pixels[3] = ivec4( shortest.x, shortest.y - 1, shortest.z + dirFactor, topCheck );			
        }
        
        // test all 4 sides
        for (i = 0; i < 4; i++) {
            ivec3 pixelMap = ivec3(pixels[i].xy, 0);
            // DEBUG
            //imageStore( map, pixelMap, vec4(0, 0.5, 0, 0));
            // Check for out of bounds
            if (pixels[i].w == 0) {
                float pixelMapValue = imageLoad( map, pixelMap).r;
                if (visited[pixelMap.x][pixelMap.y] == true) {
                    continue;
                }
                ivec3 pixelAgent = ivec3(pixelMap.xy, agentImageId);
                float pixelAgentValue = imageLoad( map, pixelAgent).r;

                // No obstacle
                if (pixelMapValue == 0.0) {
                    // Store Debug Info in Image
                    // imageStore( map, ivec3(pixelMap.xy, pixelAgent.z), vec4( 0, 0.5, 0, 0) );
                    int dist = int(abs(end.x - pixelMap.x) + abs(end.y - pixelMap.y));
                    add(distances[dist], pixels[i].xyz );
                    if (dist < curMin) {
                        curMin = dist;
                    }
                    if (pixelAgentValue == 0 || pixelAgentValue > pixelShortestValue + 1) {
                        imageStore( map, pixelAgent, vec4( pixelShortestValue + 1, 0, 0, 0) );
                    }
                }
                visited[pixelMap.x][pixelMap.y] = true;
            }
        }

        if (iteration < 3) {
            dirFactor *= 10;
        } else {
            dirFactor = 0;
        }
        iteration += 1;
    }
    // DEBUG
    // imageStore( map, ivec3(start, 0), vec4( 0, 1.0, 0, 0) );
}



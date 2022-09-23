/**
 * A custom implementation of the A* algorithm.
 * The implementation defines a compute shader which calculates the path between a set of start points
 * and a set of goal points. The input values are provided by two data buffers. The map buffer holds
 * information about obstacles which need to be avoided. The data buffer holds information about the
 * start and finish points. The result will be written back to the map buffer.
 *
 * This version uses a custom implementation of a linked list to simulate the original algorithmus as
 * close as possible.
 *
 * @author: Heinz Stadler
 */
#version 430
layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;
layout(rg32f, binding = 0) uniform coherent image3D map;
layout(rg32f, binding = 1) uniform image2D data;
const int queueSize = 400;
const float clearChance = 0.3;

// Holds Data of visited Cells
struct Cell {
	uint x;
   	uint y;
   	float value;
   	float moved;
   	int dir;
   	int next;
};

int cellsIndex;
Cell cells[queueSize];

// Add Visible Neighbour Cells
void add(Cell c) {
	// Add Cell
	uint nextIndex = 0;
    
	// Update References
	for (int i = 0; i <= cellsIndex; i++) {
        
		Cell current = cells[nextIndex];

		// Last Cell
		if (current.next == -1) {
			c.next = -1;
			cells[cellsIndex] = c;
			current.next = cellsIndex;
			cells[nextIndex] = current;
			break;
		}

		// Insert before next cell
		Cell next = cells[current.next];
		if (next.value >= c.value) {
			int oldNext = current.next;
			current.next = cellsIndex;
			cells[nextIndex] = current;
			c.next = oldNext;
			cells[cellsIndex] = c;
		}
		nextIndex = current.next;
	}
}

// Pop first Item from Queue
Cell pop(inout Cell[queueSize] cells) {
	Cell start = cells[0];
	// Queue not empty
	if (start.next != -1) {
		Cell c = cells[start.next];
		start.next = c.next;
		cells[0] = start;
		return c;
	}
	return Cell(0, 0, -1, 0, 0, -1);
}

// Get Manhatten Distance between points
float distance(ivec2 start, ivec2 finish, int mapDiscovered, ivec2 mapSize) {
	int x = mapDiscovered == 1 ? min(min(abs(finish.x - start.x), abs(finish.x - start.x - mapSize.x)), abs(finish.x - start.x + mapSize.x)) : abs(finish.x - start.x);
	int y = mapDiscovered == 1 ? min(min(abs(finish.y - start.y), abs(finish.y - start.y - mapSize.y)), abs(finish.y - start.y + mapSize.y)) : abs(finish.y - start.y);
	return x + y;
}

void main() {
	ivec2 mapSize = imageSize(map).xy;
    int agent = int(gl_GlobalInvocationID.x);
    int agentImageId = agent + 1;
    int task = int(gl_GlobalInvocationID.y);
	int mapDiscovered = int(imageLoad(data, ivec2(agent, 3)).r);

	// Start Cell to have Entry into list
	cells[0] = Cell(0, 0, 0, 0, 0, -1);
	cellsIndex = 0;
	
	// Array Structure to save which cells are visited ([x][y])
	uint visited[200][7];
    for (int i = 0; i < 200; i++) {
        for (int j = 0; j < 7; j++) {
            visited[i][j] = 0;
        }
    }

    // position agent
    ivec2 start = ivec2(imageLoad(data, ivec2(agent, 0)).rg);
    // position goal
    ivec2 end = ivec2(imageLoad(data, ivec2(task, 2)).rg);
    vec2 attachedData = imageLoad(data, ivec2(agent, 1)).rg;
    int attached = int(attachedData.r);
    int attachedCount = int(attachedData.g);
    
    // Start at Start Position
    Cell current = Cell(start.x, start.y, distance(start, end, mapDiscovered, mapSize), 0, 0, -1);
    
    ivec2 currPos = start;
    int count = 0;
    
    while (count < 500) {

        ivec2[4] sides;
		if (mapDiscovered == 1) {
			sides[0] = ivec2(currPos.x, (currPos.y + mapSize.y - 1) % mapSize.y);
			sides[1] = ivec2((currPos.x + 1) % mapSize.x, currPos.y);
			sides[2] = ivec2(currPos.x, (currPos.y + 1) % mapSize.y);
			sides[3] = ivec2((currPos.x + mapSize.x - 1) % mapSize.x, currPos.y);
		} else {
			sides[0] = ivec2(currPos.x, max(currPos.y - 1, 0));
			sides[1] = ivec2(min(currPos.x + 1, mapSize.x - 1), currPos.y);
    		sides[2] = ivec2(currPos.x, min(currPos.y + 1, mapSize.y - 1));
    		sides[3] = ivec2(max(currPos.x - 1, 0), currPos.y);
		}

    	 
    	// Test all 4 Sides
    	for (int i = 0; i < 4; i++) {
            
    		ivec2 pos = sides[i];
    		bool isBlocked = imageLoad(map, ivec3(pos, 0)).r == 0;
    		float clearCost = isBlocked ? 0 : 1 / clearChance;
    		float cost = distance(pos, end, mapDiscovered, mapSize) + clearCost;
			int currentDir = current.dir;
			int dirFactor = current.dir > 100 ? 0 : current.dir > 10 ? 100 : current.dir >= 1 ? 10 : 1;
			int newDir = currentDir + dirFactor * (i + 1);
    		Cell cell = Cell(pos.x, pos.y, cost, current.moved + 1 + clearCost, newDir, -1);
			
    		
    		// Test Attached Blocks
    		bool attachedOk = true;
            
            // Row
			/*
            for (int j = -attachedCount; j < attachedCount; j++) {
            	// Column
                for (int k = -attachedCount; k < attachedCount; k++) {
                    ivec3 testPixel = ivec3(pos.x + k, pos.y + j, 0);
                    float testPixelValue = imageLoad( map, testPixel).r;
                        
                    bool isBlockAttached = ((attached >> ((j+2) * 5 + (k+2))) & 0x1) == 0x1;

                    bool isOk = !isBlockAttached || testPixelValue == 0.0;
                    attachedOk = attachedOk && isOk;
                }
            }*/

			// Read Bit-Mask
			uint bitBlockIndex = uint(floor(pos.y / 32));
			uint bitBlock = visited[pos.x][bitBlockIndex];
			uint bitPos = pos.y % 32;
			bool isVisited = ((bitBlock >> bitPos) & 0x1) == 0x1;
    		
    		if (!isVisited && attachedOk) {
                cellsIndex = min(cellsIndex + 1, queueSize - 1);

				// Update Bit-Mask
				uint bit = 0x1 << bitPos;
				bitBlock = bitBlock | bit;
				visited[pos.x][bitBlockIndex] = bitBlock;

    			add(cell);
    		}
    	}
    	imageStore( map, ivec3(end.x,end.y,agentImageId), vec4( current.moved, current.dir, 0, 0) );
		// Stop at End
		if (currPos == end) {
			break;
		}
    	current = pop(cells);
    	currPos = ivec2(current.x, current.y);
    	count += 1;
    }
}
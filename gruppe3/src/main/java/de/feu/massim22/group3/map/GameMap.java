package de.feu.massim22.group3.map;

import java.awt.Point;

public class GameMap {
	
	private Point initialSize;
	private Point size = null;
	// First dimension are rows, second dimension are columns
	private MapCell[][] cells;
	private Point topLeft; // top left indices can be negative
	private int mapExtensionSize = 20;
	
	public GameMap(int x, int y) {
		initialSize = new Point(x, y);
		topLeft = new Point((int)(-x / 2), (int)(-y / 2));
		cells = new MapCell[y][x];
		
		// Initialize with empty cells
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				cells[i][j] = new MapCell();
			}
		}
	}
	
	public void addReport(int x, int y, CellType cellType, ZoneType zoneType, int agentId) {		
		// Check if Array is big enough
		checkBounds(x, y);
		int cellX = getCellX(x);
		int cellY = getCellY(y);

		MapCellReport report = new MapCellReport(cellType, zoneType, agentId); 
		cells[cellY][cellX].addReport(report);
	}
	
	public Point getTopLeft() {
		return topLeft;
	}
	
	public Point getBottomRight() {
		Point p = getTopLeft();
		Point s = size == null ? initialSize : size;
		return new Point(p.x + s.x, p.y + s.y);
	}
	
	public Point getOrigin() {
		return new Point(-topLeft.x, -topLeft.y);
	}
	
	public void setFinalSize(int x, int y) {
		size = new Point(x, y);
		updateMapToFinalSize();
	}
	
	private void updateMapToFinalSize() {
		MapCell[][] newCells = new MapCell[size.y][size.x];
		// Initialize with empty cells
		for (int i = 0; i < size.y; i++) {
			for (int j = 0; j < size.x; j++) {
				newCells[i][j] = new MapCell();
			}
		}

		// Merge existing cells
		for (int i = 0; i < initialSize.y; i++) {
			for (int j = 0; j < initialSize.x; j++) {
				newCells[i % size.y][j % size.x].mergeIntoCell(cells[i][j]);
			}
		}
		cells = newCells;
	}
	
	// TODO Strategy needed to update Agent Position of foreign group
	// Points are relative to the origin of their map
	public Point mergeIntoMap(GameMap foreignMap, Point foreignPoint, Point thisPoint) {
		int offsetX = thisPoint.x - foreignPoint.x;
		int offsetY = thisPoint.y - foreignPoint.y;
		
		// MapSize not yet discovered
		if (size == null) {
			
			// Get Translated Top Left of foreign map
			Point foreignTopLeft = foreignMap.getTopLeft();
			foreignTopLeft.translate(offsetX, offsetY);
			Point foreignBottomRight = foreignMap.getBottomRight();
			Point bottomRight = getBottomRight();

			Point newTopLeft = new Point(Math.min(foreignTopLeft.x, topLeft.x), Math.min(foreignTopLeft.y, topLeft.y));
			int sizeX = Math.max(foreignBottomRight.x, bottomRight.x) - newTopLeft.x;
			int sizeY = Math.max(foreignBottomRight.y, bottomRight.y) - newTopLeft.y;
			
			MapCell[][] merge = new MapCell[sizeY][sizeX];
			
			// Initialize with empty cells
			for (int i = 0; i < sizeY; i++) {
				for (int j = 0; j < sizeX; j++) {
					merge[i][j] = new MapCell();
				}
			}
			
			// Copy current Map
			int offsetXCells = topLeft.x - newTopLeft.x;
			int offsetYCells = topLeft.y - newTopLeft.y;
			for (int y = 0; y < initialSize.y; y++) {
				for (int x = 0; x < initialSize.x; x++) {
					merge[y + offsetYCells][x + offsetXCells] = cells[y][x];
				}
			}
			
			// Copy foreign Map
			offsetXCells = foreignTopLeft.x - newTopLeft.x;
			offsetYCells = foreignTopLeft.y - newTopLeft.y;
			for (int y = 0; y < foreignMap.initialSize.y; y++) {
				for (int x = 0; x < foreignMap.initialSize.x; x++) {
					MapCell f = foreignMap.cells[y][x];
					merge[y + offsetYCells][x + offsetXCells].mergeIntoCell(f);
				}
			}
			
			// update cells
			cells = merge;
			initialSize = new Point(sizeX, sizeY);
			topLeft = newTopLeft;

		}

		// Map size already discovered
		else {
			int originOffsetX = foreignMap.getTopLeft().x - topLeft.x;
			int originOffsetY = foreignMap.getTopLeft().y - topLeft.y;
			for (int y = 0; y < size.y; y++) {
				for (int x = 0; x < size.x; x++) {
					MapCell foreignCell = foreignMap.cells[y][x];
					MapCell myCell = cells[(y + offsetY + originOffsetY + size.y) % size.y][(x + offsetX + originOffsetX + size.x) % size.x];
					myCell.mergeIntoCell(foreignCell);
				}
			}
		}
		// Offset for agents of foreign map
		return new Point(offsetX, offsetY);
	}
	
	public CellType getCellType(int x, int y) {
		MapCell cell = getCell(x, y);
		return cell.getCellType();
	}
	
	private MapCell getCell(int x, int y) {
		return cells[getCellY(y)][getCellX(x)];
	}
	
	private int getCellX(int x) {
		return size == null ? x - topLeft.x : (x - topLeft.x) % size.x;
	}
	
	private int getCellY(int y) {
		return size == null ? y - topLeft.y : (y - topLeft.y) % size.y;
	}
	
	private void checkBounds(int x, int y) {
		// Double Array Size if size is not enough
		boolean extendLeft = x < topLeft.x;
		boolean extendRight = x >= initialSize.x + topLeft.x;
		boolean extendHorizontal = extendLeft || extendRight;
		boolean extendTop = y < topLeft.y;
		boolean extendBottom = y >= initialSize.y + topLeft.y;
		boolean extendVertical = extendBottom || extendTop;

		if (size == null && (extendHorizontal || extendVertical)) {
			Point newInitialSize;
			Point offset;
			// Extend to right
			if (extendRight) {
				newInitialSize = new Point(initialSize.x + mapExtensionSize, initialSize.y);
				offset = new Point(0, 0);
			}
			// Extend to left
			else if (extendLeft) {
				newInitialSize = new Point(initialSize.x + mapExtensionSize, initialSize.y);
				offset = new Point(mapExtensionSize, 0);				
			}
			// Extend to top
			else if (extendTop) {
				newInitialSize = new Point(initialSize.x, initialSize.y + mapExtensionSize);
				offset = new Point(0, mapExtensionSize);				
			}
			// Extend to bottom
			else {
				newInitialSize = new Point(initialSize.x, initialSize.y + mapExtensionSize);
				offset = new Point(0, 0);				
			}

			MapCell[][] newCells = new MapCell[newInitialSize.y][newInitialSize.x];		
			
			// Copy old values
			for (int i = 0; i < initialSize.y; i++) {
				for (int j = 0; j < initialSize.x; j++) {
					newCells[i + offset.y][j + offset.x] = cells[i][j];
				}
			}
			// Fill rest with unknowns
			if (extendHorizontal) {
				int startX = (initialSize.x + offset.x) % newInitialSize.x;
				for (int i = 0; i < newInitialSize.y; i++) {
					for (int j = 0; j < newInitialSize.x - initialSize.x; j++) {
						newCells[i][startX + j] = new MapCell();
					}
				}				
			} else {
				int startY = (initialSize.y + offset.y) % newInitialSize.y;
				for (int i = 0; i < newInitialSize.y - initialSize.y; i++) {
					for (int j = 0; j < newInitialSize.x; j++) {
						newCells[i + startY][j] = new MapCell();
					}
				}
			}

			initialSize = newInitialSize;
			cells = newCells;
			topLeft = new Point(topLeft.x - offset.x, topLeft.y - offset.y);
		}
	}
}

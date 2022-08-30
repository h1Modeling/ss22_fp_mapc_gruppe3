package de.feu.massim22.group3.utils.debugger;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import de.feu.massim22.group3.agents.desires.ActionInfo;
import de.feu.massim22.group3.agents.desires.GroupDesireTypes;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.InterestingPoint;
import de.feu.massim22.group3.map.PathFindingResult;
import de.feu.massim22.group3.utils.debugger.debugData.GroupDebugData;

import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.Map.Entry;
import java.awt.*;
import java.awt.event.*;

/** The Class <code>MapPanel</code> defines a Panel which displays the simulation map. 
 *
 * @author Heinz Stadler
 */
public class MapPanel extends JPanel {

    private GroupDebugData data;
    private Point mousePos = new Point();
    private Point hoveredCell = new Point(-1, -1);
    private Point selectedCell = new Point(-1, -1);
    private IGraphicalDebugger debugger;
    private int selectedAgentIndex = -1;
    private String selectedAgentName = "";
    private int selectedInterestingPointIndex = -1;

    /**
     * Instantiates a new MapPanel.
     * 
     * @param debugger the debugger the panel is part of
     */
    MapPanel(IGraphicalDebugger debugger) {
        this.debugger = debugger;
        
        setFocusable(true);

        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectElement();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                safeRepaint();
            }
        });

        createKeyActions();
    }

    private synchronized void safeRepaint() {
        try {
            repaint();
        } catch (Exception e) {
        }
    }

    private void createKeyActions() {
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK), "downClear");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK), "upClear");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK), "leftClear");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK), "rightClear");
        getActionMap().put("down", new KeyAction(ActionInfo.MOVE("s", "")));
        getActionMap().put("up", new KeyAction(ActionInfo.MOVE("n", "")));
        getActionMap().put("left", new KeyAction(ActionInfo.MOVE("w", "")));
        getActionMap().put("right", new KeyAction(ActionInfo.MOVE("e", "")));
        getActionMap().put("downClear", new KeyAction(ActionInfo.CLEAR(new Point(0, 1), "")));
        getActionMap().put("upClear", new KeyAction(ActionInfo.CLEAR(new Point(0, -1), "")));
        getActionMap().put("leftClear", new KeyAction(ActionInfo.CLEAR(new Point(-1, 0), "")));
        getActionMap().put("rightClear", new KeyAction(ActionInfo.CLEAR(new Point(1, 0), "")));
    }

    private void selectElement() {
        selectedCell = new Point(hoveredCell.x, hoveredCell.y);
        // Agent
        if (selectedCell.x >= 0 && selectedCell.y >= 0 && 
            data.map()[selectedCell.y][selectedCell.x] == CellType.TEAMMATE) {
            String name = data.agentPosition().get(selectedCell);
            debugger.selectAgent(name);
        } else {
            debugger.selectAgent(null);
            selectedAgentIndex = -1;
            selectedAgentName = "";
        }
        // Interesting Point
        if (data != null) {
            selectedInterestingPointIndex = getInterestingPointIndex(selectedCell);
        }
        requestFocusInWindow();
        safeRepaint();
    }

    private int getInterestingPointIndex(Point p) {
        for (int i = 0; i < data.interestingPoints().size(); i++) {
            InterestingPoint ip = data.interestingPoints().get(i);
            if (ip.point().x == p.x && ip.point().y == p.y) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Paints the map.
     * 
     * @param g the graphics context
     */
    @Override
    public synchronized void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = getSize();
        // Clear
        g2d.setColor(new Color(250, 250, 250));
        g2d.fillRect(0, 0, d.width, d.height);

        Stroke stroke = new BasicStroke(1f);
        g2d.setStroke(stroke);

        if (data != null) {
            CellType[][] cells = data.map();
            float cellWidth = Math.min(d.width / cells[0].length, d.height / cells.length);
            float mapWidth = cells[0].length * cellWidth;
            float mapHeight = cells.length * cellWidth;
            float offsetX = (d.width - mapWidth) / 2;
            float offsetY = (d.height - mapHeight) / 2;

            // Cell Types
            for (int y = 0; y < cells.length; y++) {
                CellType[] row = cells[y];
                for (int x = 0; x < row.length; x++) {
                    CellType t = row[x];
                    Rectangle2D.Double rect = new Rectangle2D.Double(x * cellWidth + offsetX, y * cellWidth + offsetY, cellWidth, cellWidth);
                    boolean isSelected = x == selectedCell.x && y == selectedCell.y;
                    String name = "";
                    String groupDesire = GroupDesireTypes.NONE;
                    if (t == CellType.TEAMMATE) {
                        String agentAtCell = data.agentPosition().get(new Point(x, y));
                        isSelected = selectedAgentName != null && selectedAgentName.equals(agentAtCell);
                        name = data.agentPosition().get(new Point(x, y));
                        groupDesire = debugger.getAgentGroupDesireType(name);
                    }
                    CellUtils.draw(g2d, t, rect, x, y, name, isSelected, groupDesire);
                }
            }

            // Goal Zones
            for (int i = 0; i < data.goalZones().size(); i++) {
                Point p = getGoalZone(i);
                if (p != null) {
                    Rectangle2D.Double rect = new Rectangle2D.Double(p.x * cellWidth + offsetX, p.y * cellWidth + offsetY, cellWidth, cellWidth);
                    g2d.setColor(new Color(237,51, 59));
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.4f));
                    g2d.fill(rect);
                    g2d.setComposite(AlphaComposite.SrcOver.derive(1f));
                }
            }

            // Role Zones
            for (int i = 0; i < data.roleZones().size(); i++) {
                Point p = getRoleZone(i);
                if (p != null) {
                    Rectangle2D.Double rect = new Rectangle2D.Double(p.x * cellWidth + offsetX, p.y * cellWidth + offsetY, cellWidth, cellWidth);
                    g2d.setColor(new Color(93,65, 213));
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.4f));
                    g2d.fill(rect);
                    g2d.setComposite(AlphaComposite.SrcOver.derive(1f));
                }
            }

            // Interesting Points Distances
            g2d.setStroke(new BasicStroke(2));
            for (int i = 0; i< data.interestingPoints().size(); i++) {
                InterestingPoint ip = getInterestingPoint(i);
                if (ip != null) {
                    Point p = ip.point();
                    Rectangle2D.Double rect = new Rectangle2D.Double(p.x * cellWidth + offsetX + 1, p.y * cellWidth + offsetY + 1, cellWidth - 2, cellWidth - 2);
                    g2d.setColor(new Color(38,162, 255));
                    g2d.draw(rect);   
                    
                    if (selectedAgentIndex >= 0) {
                        PathFindingResult[][] groupResult = data.pathFindingResult();
                        PathFindingResult result = groupResult[selectedAgentIndex][i];
                        String distance = String.valueOf(result.distance());
                        CellUtils.drawCenteredString(g2d, distance, rect, Color.BLACK);
                    }
                }
            }

            // Interesting Points direction
            if (selectedInterestingPointIndex >= 0) {
                PathFindingResult[][] groupResult = data.pathFindingResult();
                for (int i = 0; i < groupResult.length; i++) {
                    PathFindingResult result = groupResult[i][selectedInterestingPointIndex];
                    String agent = data.agents().get(i);
                    // Get Position of agent
                    for (Entry<Point, String> entry : data.agentPosition().entrySet()) {
                        if (agent.equals(entry.getValue())) {
                            Point p = entry.getKey();
                            int direction = result.direction();
                            // Draw Arrows
                            while (direction % 10 > 0) {
                                int step = direction % 10;
                                GeneralPath path = new GeneralPath();
                                switch (step) {
                                    case 1: 
                                        p = new Point(p.x, p.y - 1);
                                        path.moveTo(p.x * cellWidth + offsetX, p.y * cellWidth + offsetY + cellWidth);
                                        path.lineTo(p.x * cellWidth + offsetX + cellWidth, p.y * cellWidth + offsetY + cellWidth);
                                        path.lineTo(p.x * cellWidth + offsetX + cellWidth / 2, p.y * cellWidth + offsetY + cellWidth * 2 / 3);
                                        path.closePath();
                                        break;
                                    case 2:
                                        p = new Point(p.x + 1, p.y);
                                        path.moveTo(p.x * cellWidth + offsetX, p.y * cellWidth + offsetY);
                                        path.lineTo(p.x * cellWidth + offsetX, p.y * cellWidth + offsetY + cellWidth);
                                        path.lineTo(p.x * cellWidth + offsetX + cellWidth / 3, p.y * cellWidth + offsetY + cellWidth / 2);
                                        path.closePath();
                                        break;
                                    case 3:
                                        p = new Point(p.x, p.y + 1);
                                        path.moveTo(p.x * cellWidth + offsetX, p.y * cellWidth + offsetY);
                                        path.lineTo(p.x * cellWidth + offsetX + cellWidth, p.y * cellWidth + offsetY);
                                        path.lineTo(p.x * cellWidth + offsetX + cellWidth / 2, p.y * cellWidth + offsetY + cellWidth / 3);
                                        path.closePath();
                                        break;
                                    case 4:
                                        p = new Point(p.x - 1, p.y);
                                        path.moveTo(p.x * cellWidth + offsetX + cellWidth, p.y * cellWidth + offsetY);
                                        path.lineTo(p.x * cellWidth + offsetX + cellWidth, p.y * cellWidth + offsetY + cellWidth);
                                        path.lineTo(p.x * cellWidth + offsetX + cellWidth * 2 / 3, p.y * cellWidth + offsetY + cellWidth / 2);
                                        path.closePath();
                                        break;
                                }

                                g2d.setColor(new Color(28, 113, 216));
                                g2d.fill(path);
                                direction = direction / 10;
                            }
                            break;
                        }
                    }
                }
            }

            // Hover
            int cellX = (int)((mousePos.x - offsetX) / cellWidth);
            int cellY = (int)((mousePos.y - offsetY) / cellWidth);

            if (mousePos.x > offsetX && mousePos.y > offsetX && mousePos.x < offsetX + mapWidth && mousePos.y < offsetY + mapHeight) {
                Rectangle2D.Double rect = new Rectangle2D.Double(cellX * cellWidth + offsetX, cellY * cellWidth + offsetY, cellWidth, cellWidth);
                g2d.setColor(new Color(38, 162, 105));
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.15f));
                g2d.fill(rect);
                g2d.setComposite(AlphaComposite.SrcOver.derive(1f));
                // Save cell
                hoveredCell = new Point(cellX, cellY);
            }
        }
    }

    private synchronized Point getGoalZone(int index) {
        return data.goalZones().size() > index ? data.goalZones().get(index) : null;
    }

    private synchronized Point getRoleZone(int index) {
        return data.roleZones().size() > index ? data.roleZones().get(index) : null;
    }

    private synchronized InterestingPoint getInterestingPoint(int index) {
        return data.interestingPoints().size() > index ? data.interestingPoints().get(index) : null;
    }

    /**
     * Selects the agent in the map and shows it's path finding information.
     * 
     * @param name the name of the agent
     */
    synchronized void selectAgent(String name) {
        selectedAgentName = name;
        // Get index of selected agent for pathfinding results
        List<String> agents = data.agents();
        for (int i = 0; i < agents.size(); i++) {
            String agent = agents.get(i);
            if (name != null && name.equals(agent)) {
                selectedAgentIndex = i;
                break;
            }
        }
    }

    /**
     * Sets the debug data of the agent group.
     * 
     * @param data the data of the agent group
     */
    synchronized void setData(GroupDebugData data) {
        this.data = data;
        this.safeRepaint();
    }

    private class KeyAction extends AbstractAction {

        private eis.iilang.Action action;

        KeyAction(ActionInfo i) {
            action = i.value();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MapPanel.this.debugger.setActionForAgent(selectedAgentName, action);
        }
    } 
}

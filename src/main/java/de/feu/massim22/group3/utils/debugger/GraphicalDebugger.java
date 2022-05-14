package de.feu.massim22.group3.utils.debugger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.Point;

import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.InterestingPoint;
import de.feu.massim22.group3.map.PathFindingResult;
import de.feu.massim22.group3.map.ZoneType;
import massim.protocol.data.NormInfo;
import massim.protocol.data.Subject;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class GraphicalDebugger extends JFrame implements Runnable, IGraphicalDebugger {

    private JPanel header;
    private AgentPanel agentPanel;
    private SimulationPanel simulationPanel;
    private MapPanel mapPanel;

    private Map<String, AgentDebugData> agentData = new HashMap<>();
    private Map<String, GroupDebugData> groupData = new HashMap<>();
    private String selectedGroup = "";

    public static void main( String[] args ) {
        SwingUtilities.invokeLater(new GraphicalDebugger());
    }

    public GraphicalDebugger() {
        setTitle("Debugger - Massim 22 - Gruppe 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        System.out.println("START ...");

        header = new Header();
        add(header, BorderLayout.NORTH);

        agentPanel = new AgentPanel();
        add(new JScrollPane(agentPanel), BorderLayout.WEST);

        simulationPanel = new SimulationPanel();
        JScrollPane scrollableSimPanel = new JScrollPane(simulationPanel);
        //scrollableSimPanel.setBorder(null);
        add(scrollableSimPanel, BorderLayout.EAST);

        // Agent Debug Data
        AgentDebugData a1 = new AgentDebugData("A1", "AX", "default", 90, "move n", "success");
        AgentDebugData a2 = new AgentDebugData("A2", "AY", "digger", 100, "move s", "success");
        agentData.put("A1", a1);
        agentData.put("A2", a2);

        mapPanel = new MapPanel(this);
        add(mapPanel);

        //mapPanel.setData(createTestMap());

        simulationPanel.setTasks(createTestTask());

        simulationPanel.setNorms(createTestNorm());
        // ....

        setPreferredSize(new Dimension(1000, 800));
        setSize(1000, 800);
        setResizable(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        toFront();
    }

    private GroupDebugData createTestMap() {
        CellType[] row1 = {CellType.BLOCK_0, CellType.ENEMY, CellType.DISPENSER_2, CellType.OBSTACLE};
        CellType[] row2 = {CellType.FREE, CellType.FREE, CellType.FREE, CellType.TEAMMATE};
        CellType[] row3 = {CellType.FREE, CellType.TEAMMATE, CellType.OBSTACLE, CellType.FREE};
        CellType[] row4 = {CellType.OBSTACLE, CellType.OBSTACLE, CellType.OBSTACLE, CellType.FREE};
        CellType[][] map = {row1, row2, row3, row4};

        List<InterestingPoint> interestingPoints = new ArrayList<>();
        interestingPoints.add(new InterestingPoint(new Point(0, 1), ZoneType.GOALZONE, CellType.FREE));
        interestingPoints.add(new InterestingPoint(new Point(2, 1), ZoneType.NONE, CellType.DISPENSER_2));

        PathFindingResult[] result = {
            new PathFindingResult(5, 11),
            new PathFindingResult(10, 33)
        };
        PathFindingResult[] result2 = {
            new PathFindingResult(2, 22),
            new PathFindingResult(4, 44)
        };
        PathFindingResult[][] pathFindingResults = { result, result2 };

        Map<Point, String> agentPosition = new HashMap<>();
        agentPosition.put(new Point(3, 1), "A1");
        agentPosition.put(new Point(1, 2), "A2");

        List<Point> goalZones = new ArrayList<>();
        goalZones.add(new Point(0,1));
        goalZones.add(new Point(0, 2));

        List<Point> roleZones = new ArrayList<>();
        roleZones.add(new Point(3, 2));
        roleZones.add(new Point(3,3));

        List<String> agents = new ArrayList<>();
        agents.add("A1");
        agents.add("A2");

        return new GroupDebugData("Hallo", map, new Point(-2, -2),
            interestingPoints, pathFindingResults, agentPosition, roleZones, goalZones, agents);  
    }

    private Set<TaskInfo> createTestTask() {
        Set<TaskInfo> result = new HashSet<>();
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(-1, -1, Thing.TYPE_BLOCK, "b1"));
        things.add(new Thing(0, -1, Thing.TYPE_BLOCK, "b0"));
        things.add(new Thing(0, 1, Thing.TYPE_BLOCK, "b1"));
        TaskInfo info = new TaskInfo("Test", 230, 50, things);
        result.add(info);
        return result;
    }

    private Set<NormInfo> createTestNorm() {
        Set<NormInfo> result = new HashSet<>();
        Set<Subject> requirements = new HashSet<>();
        requirements.add(new Subject(Subject.Type.BLOCK, "test", 2, "test test"));

        NormInfo info = new NormInfo("test", 20, 100, requirements, 30);
        result.add(info);
        return result;
    }

    @Override
    public void selectAgent(String agent) {
        AgentDebugData data = agentData.get(agent);
        agentPanel.setAgentData(data);
    }

    public static record GroupDebugData(String supervisor, CellType[][] map, Point mapTopLeft, 
        List<InterestingPoint> interestingPoints, PathFindingResult[][] pathFindingResult,
        Map<Point, String> agentPosition, List<Point> roleZones, List<Point> goalzones,
        List<String> agents) { }

    public static record AgentDebugData(
        String name,
        String supervisor,
        String role,
        int energy,
        String lastAction,
        String lastActionSuccess
    ) {}

    @Override
    public void setGroupData(GroupDebugData data) {
        groupData.put(data.supervisor, data);
        // TODO remove
        if (selectedGroup == "") {
            selectedGroup = data.supervisor;
        }

        // update current view
        if (data.supervisor() == selectedGroup) {
            mapPanel.setData(data);
        }  
    }
}

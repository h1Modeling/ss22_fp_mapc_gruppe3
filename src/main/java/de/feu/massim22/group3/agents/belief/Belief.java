package de.feu.massim22.group3.agents.belief;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.Point;

import de.feu.massim22.group3.agents.V2utils.AgentCooperations;
import de.feu.massim22.group3.agents.V2utils.StepUtilities;
import de.feu.massim22.group3.agents.belief.reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.belief.reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.belief.reachable.ReachableRoleZone;
import de.feu.massim22.group3.agents.belief.reachable.ReachableTeammate;
import de.feu.massim22.group3.agents.desires.GroupDesireTypes;
import de.feu.massim22.group3.agents.supervisor.AgentReport;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.map.ZoneType;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.PerceptUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Function;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import massim.protocol.data.NormInfo;
import massim.protocol.data.Role;
import massim.protocol.data.Subject;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;
import massim.protocol.messages.scenario.Actions;
import massim.protocol.data.Subject.Type;

/** The Class <code>Belief</code> defines a data structure to store the agents information about the simulation. 
 *
 * @author Heinz Stadler
 * @author Melinda Betz (minor contribution)
 * @author Phil Heger (minor contribution)
 */
public class Belief {

    // Start Beliefs
    private String agentFullName = "";
    private String agentShortName = "";
    private String team;
    private int teamSize;
    private int steps;
    private Map<String, Role> roles = new HashMap<>();

    // Step Beliefs
    private int step;
    private Set<Thing> things = new HashSet<>();
    private Set<Thing> marker = new HashSet<>();
    private Set<TaskInfo> taskInfo = new HashSet<>();
    private Set<TaskInfo> taskInfoAtLastStep = new HashSet<>();
    private List<TaskInfo> newTasks = new ArrayList<>();
    private Set<NormInfo> normsInfo = new HashSet<>();
    private long score;
    private String lastAction;
    private String lastActionResult;
    private List<String> lastActionParams = new ArrayList<>();
    private String lastActionIntention;

    private List<Point> attachedPoints = new ArrayList<>();
    private List<Point> ownAttachedPoints = new ArrayList<>();
    private List<Thing> attachedThings = new ArrayList<>();
    private int energy;
    private boolean deactivated;
    private String role = "default";
    private List<StepEvent> stepEvents = new ArrayList<>();
    private List<String> violations = new ArrayList<>();
    private List<Point> goalZones = new ArrayList<>();
    private List<Point> roleZones = new ArrayList<>();
    private boolean simEnd = false;

    // Group 3 Beliefs
    private Point position = new Point(0, 0);
    private List<ReachableDispenser> reachableDispensers = new ArrayList<>();
    private List<ReachableGoalZone> reachableGoalZones = new ArrayList<>();
    private List<ReachableRoleZone> reachableRoleZones = new ArrayList<>();
    private List<ReachableTeammate> reachableTeammates = new ArrayList<>();
    private List<ForbiddenThing> forbiddenThings = new ArrayList<>();
    private String groupDesireType = GroupDesireTypes.NONE;
    private List<ConnectionReport> connectionReports = new ArrayList<>();
    private String groupDesireBlockDetail = "";
    private String groupDesirePartner = "";
    private boolean isWaiting = false;
    private String lastMoveDirection = "n";
    
    private Point mapSize;
    private Point mapTopLeft;
    private Point absolutePosition;

    /**
     * Instantiates a new Belief.
     * 
     * @param agentName the name of the agent
     */
    public Belief(String agentName) {
        this.agentShortName = agentName;
    }

    /**
     * Updates the belief based on the provided Percepts.
     * 
     * @param percepts the List of Percept
     */
    public void update(List<Percept> percepts) {
        clearLists();
        for (Percept percept : percepts) {
            List<Parameter> p = percept.getParameters();
            switch (percept.getName()) {
                case "step":
                    step = toNumber(p, 0, Integer.class);
                    // Update Forbidden Things
                    forbiddenThings.removeIf(t -> t.stepDiscovered + t.duration < step);
                    break;
                case "lastAction":
                    lastAction = toStr(p, 0);
                    break;
                case "lastActionResult":
                    lastActionResult = toStr(p, 0);
                    break;
                case "lastActionParams":
                    lastActionParams = toStrList(p, 0);
                    break;
                case "score":
                    score = toNumber(p, 0, Long.class);
                    break;
                case "thing":
                    int x = toNumber(p, 0, Integer.class);
                    int y = toNumber(p, 1, Integer.class);
                    String type = toStr(p, 2);
                    String details = toStr(p, 3);
                    if (type.equals(Thing.TYPE_MARKER)) {
                        marker.add(new Thing(x, y, type, details));
                    } else {
                        things.add(new Thing(x, y, type, details));
                    }
                    break;
                case "task":
                    String name = toStr(p, 0);
                    int deadline = toNumber(p, 1, Integer.class);
                    int reward = toNumber(p, 2, Integer.class);
                    Set<Thing> requirements = toThingSet(p, 3);
                    TaskInfo info = new TaskInfo(name, deadline, reward, requirements);
                    taskInfo.add(info);
                    break;
                case "attached":
                    int attX = toNumber(p, 0, Integer.class);
                    int attY = toNumber(p, 1, Integer.class);
                    attachedPoints.add(new Point(attX, attY));
                    break;
                case "energy":
                    energy = toNumber(p, 0, Integer.class);
                    break;
                case "deactivated":
                    deactivated = toStr(p, 0).equals("true");
                    break;
                case "role":
                    // Start percept
                    if (p.size() > 1) {
                        String roleName = toStr(p, 0);
                        int roleVision = toNumber(p, 1, Integer.class);
                        Set<String> roleActions = new HashSet<String>(toStrList(p, 2));
                        List<Integer> roleSpeedList = toIntList(p, 3);
                        int[] roleSpeedArray = new int[roleSpeedList.size()];
                        for (int i = 0; i < roleSpeedList.size(); i++) {
                            roleSpeedArray[i] = roleSpeedList.get(i);
                        }
                        double change = toNumber(p, 4, Double.class);
                        int maxDistance = toNumber(p, 1, Integer.class);                    
                        Role r = new Role(roleName, roleVision, roleActions, roleSpeedArray, change, maxDistance);
                        roles.put(roleName, r);     
                    } 
                    // Step percept
                    else {
                        role = toStr(p, 0);
                    }               
                    break;
                case "violation":
                    violations.add(toStr(p, 0));
                    break;
                case "norm":
                    String normName = toStr(p, 0);
                    int start = toNumber(p, 1, Integer.class);
                    int until = toNumber(p, 2, Integer.class);
                    int punishment = toNumber(p, 4, Integer.class);
                    Set<Subject> normRequirements = toSubjectSet(p, 3);
                    normsInfo.add(new NormInfo(normName, start, until, normRequirements, punishment));
                    break;
                case "roleZone":
                    int roleX = toNumber(p, 0, Integer.class);
                    int roleY = toNumber(p, 1, Integer.class);
                    roleZones.add(new Point(roleX, roleY));
                    break;
                case "goalZone":
                    int goalX = toNumber(p, 0, Integer.class);
                    int goalY = toNumber(p, 1, Integer.class);
                    goalZones.add(new Point(goalX, goalY));
                    break;
                case "surveyed":
                    String surveyType = toStr(p, 0);
                    if (surveyType.equals("agent")) {
                        String surveyName = toStr(p, 1);
                        String surveyRole = toStr(p, 2);
                        int surveyEnergy = toNumber(p, 3, Integer.class);
                        StepEvent e = new AgentSurveyStepEvent(surveyName, surveyRole, surveyEnergy);
                        stepEvents.add(e);
                    } else {
                        int distance = toNumber(p, 1, Integer.class);
                        StepEvent e = new ThingSurveyStepEvent(surveyType, distance);
                        stepEvents.add(e);
                    }
                    break;
                case "hit":
                    int hitX = toNumber(p, 0, Integer.class);
                    int hitY = toNumber(p, 1, Integer.class);
                    Point hitPoint = new Point(hitX, hitY);
                    StepEvent ev = new HitStepEvent(hitPoint);
                    stepEvents.add(ev);
                    break;
                case "name":
                    agentFullName = toStr(p, 0);
                    break;
                case "team":
                    team = toStr(p, 0);
                    break;
                case "teamSize":
                    teamSize = toNumber(p, 0, Integer.class);
                    break;
                case "steps":
                    steps = toNumber(p, 0, Integer.class);
                    break;
                case "simStart":
                    break;
                case "deadline":
                    break;
                case "actionID":
                    break;
                case "timestamp":
                    break;
                case "requestAction":
                    break;
                case "ranking":
                    break;
                case "time":
                    break;
                case "simEnd":
                    simEnd = true;
                    break;
                case "position":
                    int posX = toNumber(p, 0, Integer.class);
                    int posY = toNumber(p, 1, Integer.class);
                    absolutePosition = new Point(posX, posY);
                    break;
                default:
                    AgentLogger.warning("Percept not transferred to Belief: " + percept.getName());
                }
            }
        updateOwnAttachedPoints();
        if (StepUtilities.getAgent(agentShortName) == null)
            // BdiAgentV1 is running
            updatePosition();
        updateNewTasks();
        updateAttachedThings();
    }

    private void updateOwnAttachedPoints() {

        if (lastAction != null && lastActionResult != null) {
            // Attach
            if (lastAction.equals("attach") && lastActionResult.equals(ActionResults.SUCCESS) && lastActionParams.size() > 0) {
                Point p = null;
                switch (lastActionParams.get(0)) {
                    case "n": p = new Point(0, -1); break;
                    case "e": p = new Point(1, 0); break;
                    case "s": p = new Point(0, 1); break;
                    case "w": p = new Point(-1, 0); break;
                }
                // Test for duplicates
                if (p != null && !ownAttachedPoints.contains(p)) {
                    ownAttachedPoints.add(p);
                }
            }
            // Detach
            if (lastAction.equals("detach") && lastActionResult.equals(ActionResults.SUCCESS) && lastActionParams.size() > 0) {
                switch (lastActionParams.get(0)) {
                    case "n": ownAttachedPoints.remove(new Point(0, -1)); break;
                    case "e": ownAttachedPoints.remove(new Point(1, 0)); break;
                    case "s": ownAttachedPoints.remove(new Point(0, 1)); break;
                    case "w": ownAttachedPoints.remove(new Point(-1, 0)); break;
                }
            }        
            // Rotate
            if (lastAction.equals("rotate") && lastActionResult.equals(ActionResults.SUCCESS) && lastActionParams.size() > 0) {
                if (lastActionParams.get(0).equals("cw")) {
                    // Clock wise
                    for (Point p : ownAttachedPoints) {
                        Point rotated = new Point(-p.y, p.x);
                        p.x = rotated.x;
                        p.y = rotated.y;
                    }
                } else {
                    // Counter Clock wise
                    for (Point p : ownAttachedPoints) {
                        Point rotated = new Point(p.y, -p.x);
                        p.x = rotated.x;
                        p.y = rotated.y;
                    } 
                }
            }
            // Connect
            if (lastAction.equals("connect") && lastActionResult.equals(ActionResults.SUCCESS) && lastActionParams.size() > 0) {
                String agent = lastActionParams.get(0);
                ConnectionReport report = null;
                for (ConnectionReport r : connectionReports) {
                    if (r.step == step - 1 && r.agent.equals(agent)) {
                        report = r;
                        break;
                    }
                }
                if (report != null) {
                    for (Point p : report.points) {
                        ownAttachedPoints.add(p);
                    }
                }
            }
            // Disconnect
            if (lastAction.equals("disconnect") && lastActionResult.equals(ActionResults.SUCCESS) && lastActionParams.size() > 0) {
                int x = Integer.parseInt(lastActionParams.get(0));
                int y = Integer.parseInt(lastActionParams.get(1));
                ownAttachedPoints.remove(new Point(x, y));
                if (lastActionParams.size() == 4) {
                    int x2 = Integer.parseInt(lastActionParams.get(2));
                    int y2 = Integer.parseInt(lastActionParams.get(3));
                    ownAttachedPoints.remove(new Point(x2, y2));
                }
            }
            // Compare with attached Points to remove submitted or cleared points
            ownAttachedPoints.removeIf(p -> !attachedPoints.contains(p));
        } else {
            ownAttachedPoints.addAll(attachedPoints);
        }
    }

    /**
     * Updates the belief with path finding information.
     * 
     * @param points a list containing information about reachable points
     */
    public void updateFromPathFinding(List<Parameter> points) {
        reachableDispensers.clear();
        reachableGoalZones.clear();
        reachableRoleZones.clear();
        reachableTeammates.clear();

        for (Parameter p : points) {
            if (!(p instanceof Function)) {
                throw new IllegalArgumentException("Path Finding Results must be of Type function");
            }
            // Path Finding Result
            if (((Function)p).getName().equals("pointResult")) {
                // Data
                List<Parameter> paras = ((Function) p).getParameters();
                String detail = toStr(paras, 0);
                boolean isZone = toBool(paras, 1);
                int px = toNumber(paras, 2, Integer.class);
                int py = toNumber(paras, 3, Integer.class);
                int distance = toNumber(paras, 4, Integer.class);
                int direction = toNumber(paras, 5, Integer.class);
                String data = toStr(paras, 6);
                Point pos = new Point(px, py);

                // Goal Zones
                if (isZone && detail.equals(ZoneType.GOALZONE.name())) {
                    ReachableGoalZone gz = new ReachableGoalZone(pos, distance, direction);
                    reachableGoalZones.add(gz);
                }
                // Role Zones
                if (isZone && detail.equals(ZoneType.ROLEZONE.name())) {
                    ReachableRoleZone rz = new ReachableRoleZone(pos, distance, direction);
                    reachableRoleZones.add(rz);
                }
                // Dispenser
                if (!isZone && CellType.valueOf(detail).isDispenser()) {
                    ReachableDispenser rd = new ReachableDispenser(pos, CellType.valueOf(detail), distance, direction,
                            data);
                    reachableDispensers.add(rd);
                }
                // Teammate
                if (!isZone && CellType.valueOf(detail) == CellType.TEAMMATE) {
                    ReachableTeammate rt = new ReachableTeammate(pos, data, distance, direction);
                    reachableTeammates.add(rt);
                }
            }
            // Point which is not reachable
            else {
                // Data
                List<Parameter> paras = ((Function) p).getParameters();
                String detail = toStr(paras, 0);
                boolean isZone = toBool(paras, 1);
                int px = toNumber(paras, 2, Integer.class);
                int py = toNumber(paras, 3, Integer.class);

                // Dispenser
                if (!isZone && CellType.valueOf(detail).isDispenser()) {
                    String thingDetail = Convert.cellTypeToThingDetail(CellType.valueOf(detail));
                    Thing t = new Thing(px - this.position.x, py - this.position.y, Thing.TYPE_DISPENSER, thingDetail);
                    things.add(t);
                }

                // Goal Zone
                if (isZone && ZoneType.valueOf(detail).equals(ZoneType.GOALZONE)) {
                    goalZones.add(new Point(px - this.position.x, py - this.position.y));
                }
            }
        }

        // Sort
        reachableRoleZones.sort((a, b) -> a.distance() - b.distance());
        reachableGoalZones.sort((a, b) -> a.distance() - b.distance());
    }

    private <T extends Number> T toNumber(List<Parameter> parameters, int index, Class<T> type) {
        return PerceptUtil.toNumber(parameters, index, type);
    }

    private String toStr(List<Parameter> parameters, int index) {
        return PerceptUtil.toStr(parameters, index);
    }

    private boolean toBool(List<Parameter> parameters, int index) {
        return PerceptUtil.toBool(parameters, index);
    }

    private List<String> toStrList(List<Parameter> parameters, int index) {
        return PerceptUtil.toStrList(parameters, index);
    }

    private List<Integer> toIntList(List<Parameter> parameters, int index) {
        return PerceptUtil.toIntList(parameters, index);
    }

    private Set<Thing> toThingSet(List<Parameter> parameters, int index) {
        return PerceptUtil.toThingSet(parameters, index);
    }

    private Set<Subject> toSubjectSet(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof ParameterList))
            throw new IllegalArgumentException();
        Set<Subject> result = new HashSet<>();
        for (Parameter para : (ParameterList) p) {
            if (!(para instanceof Function))
                throw new IllegalArgumentException();
            List<Parameter> funcParameter = ((Function) para).getParameters();
            Type type = toStr(funcParameter, 0).equals("block") ? Type.BLOCK : Type.ROLE;
            String name = toStr(funcParameter, 1);
            int quantity = toNumber(funcParameter, 2, Integer.class);
            String details = toStr(funcParameter, 3);
            result.add(new Subject(type, name, quantity, details));
        }
        return result;
    }

    private record ThingSurveyStepEvent(String name, int distance) implements StepEvent {
        public String toString() {
            return name + " " + distance + " cells away";  
        }
    }

    private record HitStepEvent(Point position) implements StepEvent {
        public String toString() {
            return "Hit at " + position.x + "/" + position.y;  
        }
    }

    /**
     * Adds a <code>ConnectionReport</code> based on the provided information.
     * 
     * @param message the message containing the connection information
     */
    public void addPossibleConnection(Percept message) {
        List<Parameter> paras = message.getParameters();
        String agent = toStr(paras, 0);
        int step = toNumber(paras, 1, Integer.class);
        List<Point> points = new ArrayList<>();
        for (int i = 1; i < paras.size() / 2; i++) {
            int x = toNumber(paras, i*2, Integer.class);
            int y = toNumber(paras, i*2+1, Integer.class);
            Point p = new Point(x - position.x, y - position.y);
            points.add(p);
        }
        ConnectionReport report = new ConnectionReport(agent, step, points);
        connectionReports.add(report);
    }

    /**
     * Gets the name of the team mate which performs a group task with the agent.
     * 
     * @return the name of the team mate or an empty string if not team mate performs a group task with the agent
     */
    public String getGroupDesirePartner() {
        return groupDesirePartner;
    }

    /**
     * Gets the details of all attached things to the agent combined in a String.
     * 
     * @return the details of all attached things to the agent combined in a String
     */
    public String getAttachedThingsDebugString() {
        String result = "";
        for (Thing t : attachedThings) {
            result += t.details + " ";
        }
        return result;
    }

    /**
     * Saves the name of the team mate which performs a group task with the agent.
     * 
     * @param groupDesirePartner the name of the team mate
     */
    public void setGroupDesirePartner(String groupDesirePartner) {
        this.groupDesirePartner = groupDesirePartner;
    }

    /**
     * Gets the block type of the current group task.
     * 
     * @return the block type
     */
    public String getGroupDesireBlockDetail() {
        return groupDesireBlockDetail;
    }

    /**
     * Sets the block type of the current group task.
     * 
     * @param groupDesireBlockDetail the block type
     */
    public void setGroupDesireBlockDetail(String groupDesireBlockDetail) {
        this.groupDesireBlockDetail = groupDesireBlockDetail;
    }

    /**
     * Gets if the agent is currently waiting for another agent.
     * 
     * @return true if the agent is waiting for another agent
     */
    public boolean isWaiting() {
        return isWaiting;
    }

    /**
     * Sets if the agent is waiting for another agent.
     * 
     * @param isWaiting true if the agent is waiting for another agent
     */
    public void setWaiting(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }

    /**
     * Gets if the simulation has ended.
     * 
     * @return true if the simulation has ended
     */
    public boolean isSimEnd() {
        return simEnd;
    }

    /**
     * Gets the name of the team the agent is part of.
     * 
     * @return the name of the team
     */
    public String getTeam() {
        return team;
    }

    /**
     * Gets the short name of the agent.
     * 
     * @return the short name of the agent
     */
    public String getAgentShortName() {
        return agentShortName;
    }

    /**
     * Gets the full name of the agent generated by the server.
     * 
     * @return the full name of the agent
     */
    public String getAgentFullName() {
        return agentFullName;
    }

    /**
     * Gets the size of the current vision.
     * 
     * @return the size of the vision
     */
    public int getVision() {
        Role r = roles.get(role);
        return r == null ? 0 : r.vision();
    }

    /**
     * Gets the current step of the simulation.
     * 
     * @return the step of the simulation
     */
    public int getStep() {
        return step;
    }

    /**
     * Gets the last step of the simulation.
     * 
     * @return the last step of the simulation
     */
    public int getSteps() {
        return steps;
    }

    /**
     * Gets the size of the team the agent is part of.
     * 
     * @return the team size 
     */
    public int getTeamSize() {
        return teamSize;
    }

    /**
     * Gets the Things, excluding markers, the agent currently has in vision.
     * 
     * @return the things in vision
     */
    public Set<Thing> getThings() {
        return things;
    }

    /**
     * Gets the marker the agent currently has in vision.
     * 
     * @return the markers in vision
     */
    public List<Thing> getMarker() {
        return new ArrayList<>(marker);
    }

    /**
     * Gets a List of Points containing position information about the marker in vision.
     * 
     * @return the position of the marker in vision
     */
    public List<Point> getMarkerPoints() {
        List<Point> result = new ArrayList<>();
        for (var m : marker) {
            result.add(new Point(m.x, m.y));
        }
        return result;
    }

    /**
     * Gets the current tasks of the simulation.
     * 
     * @return the current tasks
     */
    public Set<TaskInfo> getTaskInfo() {
        taskInfo.removeIf(e -> e.deadline < step);
        return taskInfo;
    }

    /**
     * Gets the current norms of the simulation.
     * 
     * @return the current norms
     */
    public Set<NormInfo> getNormsInfo() {
        return normsInfo;
    }

    /**
     * Gets the current score of the team the agent is part of.
     * 
     * @return the score
     */
    public long getScore() {
        return score;
    }

    /**
     * Gets the last action name the agent has sent to the server.
     * 
     * @return last action name
     */
    public String getLastAction() {
        return lastAction == null ? Actions.NO_ACTION : lastAction;
    }

    /**
     * Gets the result of the last action sent to the server.
     * 
     * @return the result of the last action
     */
    public String getLastActionResult() {
        return lastActionResult == null ? ActionResults.UNPROCESSED : lastActionResult;
    }

    /**
     * Gets the last action combined with its parameters as a String.
     * 
     * @return the last action string
     */
    public String getLastActionDebugString() {
        String paras = "";
        for (String s: lastActionParams) {
            paras += " " + s;
        }
        return lastAction + paras;
    }

    /**
     * Sets the name of the intention the last action came from.
     * 
     * @param lastActionIntention the name of the last intention
     */
    public void setLastActionIntention(String lastActionIntention) {
        this.lastActionIntention = lastActionIntention;
    }

    /**
     * Gets the name of the intention the last action came from.
     * 
     * @return the name of the last intention
     */
    public String getLastActionIntention() {
        return lastActionIntention;
    }

    /**
     * Gets the parameters of the last action.
     * 
     * @return the parameters of the last action
     */
    public List<String> getLastActionParams() {
        return lastActionParams;
    }

    /**
     * Gets the attached Things of the agent.
     * 
     * @return the attached Things of the agent
     */
    public List<Thing> getAttachedThings() {
        return attachedThings;
    }

    /**
     * Gets the attached Points of the agent.
     * 
     * @return the attached Points of the agent
     */
    public List<Point> getOwnAttachedPoints() {
        return ownAttachedPoints;
    }

    /**
     * Gets the attached Points in vision of the agent. These can be on different agents.
     * 
     * @return the attached Points
     */
    public List<Point> getAttachedPoints() {
        return attachedPoints;
    }

    /**
     * Gets the current energy of the agent.
     * 
     * @return the current energy of the agent
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Gets if the agent is deactivated.
     * 
     * @return true if the agent is deactivated
     */
    public boolean isDeactivated() {
        return deactivated;
    }

    /**
     * Gets the role name of the agent.
     * 
     * @return the role name
     */
    public String getRoleName() {
        return role;
    }

    /**
     * Gets the current role of the agent.
     * 
     * @return the current role
     */
    public Role getRole() {
        return roles.get(role);
    }

    /**
     * Get all possible roles in the simulation.
     * 
     * @return all possible Roles in a map.
     */
    public Map<String, Role> getRoles() {
        return roles;
    }

    /**
     * Get a role which supports all provided actions.
     * 
     * @param actions the actions the role must support
     * @return the role
     */
    public Role getRoleByActions(String[] actions) {
        // determine Role with best values for clear
        if (actions.length == 1 && actions[0].equals("clear")) {
            double maxFactor = 0;
            Role bestClearRole = null;
            for (Role r : roles.values()) {
                double curFactor = r.clearChance() * r.clearMaxDistance();
                if (curFactor > maxFactor) {
                    maxFactor = curFactor;
                    bestClearRole = r;
                }
            }
            AgentLogger.info("bestClearRole is " + bestClearRole.name());
            return bestClearRole;
        }

        List<Role> possibleRoles = new ArrayList<>();
        for (Role r : roles.values()) {
            boolean allFound = true;
            for (String action : actions) {
                if (!r.actions().contains(action)) {
                    allFound = false;
                }
            }
            if (allFound) {
                possibleRoles.add(r);
            }
        }
        possibleRoles.sort((a, b) -> b.maxSpeed(0) - a.maxSpeed(0));
        return possibleRoles.size() > 0 ? possibleRoles.get(0) : null;
    }

    /**
     * Get all step events.
     * 
     * @return the step events
     */
    public List<StepEvent> getStepEvents() {
        return stepEvents;
    }

    /**
     * Get current norm violations.
     * 
     * @return the norm violations
     */
    public List<String> getViolations() {
        return violations;
    }

    /**
     * Get all Points in goal zones.
     * 
     * @return a list of goal zone points
     */
    public List<Point> getGoalZones() {
        return goalZones;
    }

    /**
     * Get all Points in role zones.
     * 
     * @return a list of role zone points
     */    
    public List<Point> getRoleZones() {
        return roleZones;
    }

    /**
     * Get the current position of the agent.
     * 
     * @return the current position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Get all reachable points next to a dispenser.
     * 
     * @return a list of reachable points next to a dispenser
     */
    public List<ReachableDispenser> getReachableDispensers() {
        return reachableDispensers;
    }

    /**
     * Get all reachable goal zones.
     * 
     * @return a list of reachable goal zones
     */
    public List<ReachableGoalZone> getReachableGoalZones() {
        return reachableGoalZones;
    }

    /**
     * Get all reachable role zones.
     * 
     * @return a list of reachable role zones
     */
    public List<ReachableRoleZone> getReachableRoleZones() {
        return reachableRoleZones;
    }

    /**
     * Get path finding information to reachable team mates.
     * 
     * @return a list of path finding information to reachable team mates
     */ 
    public List<ReachableTeammate> getReachableTeammates() {
        return reachableTeammates;
    }

    /**
     * Gets the nearest reachable role zone.
     * 
     * @return the nearest reachable role zone
     */
    public ReachableRoleZone getNearestRoleZone() {
        // Zone is sorted
        return reachableRoleZones.size() > 0 ? reachableRoleZones.get(0) : null;
    }
    
    /**
     * Gets the nearest role zone measured in Manhattan distance.
     * 
     * @return the nearest role zone in Manhattan distance
     */
    public Point getNearestRelativeManhattanRoleZone() {
        roleZones.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));
        return roleZones.size() > 0 ? roleZones.get(0) : null;
    }

    /**
     * Gets the nearest goal zone.
     * 
     * @return the nearest goal zone
     */
    public ReachableGoalZone getNearestGoalZone() {
        // Zone is sorted
        return reachableGoalZones.size() > 0 ? reachableGoalZones.get(0) : null;
    }

    /**
     * Gets the nearest goal zone measured in Manhattan distance.
     * 
     * @return the nearest goal zone in Manhattan distance
     */
    public Point getNearestRelativeManhattanGoalZone() {
        goalZones.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));
        return goalZones.size() > 0 ? goalZones.get(0) : null;
    }

    /**
     * Gets the nearest point next to a dispenser with the provided type.
     * 
     * @param t the type of the dispenser
     * @return the nearest point next to a dispenser with the provided type
     */
    public ReachableDispenser getNearestDispenser(CellType t) {
        List<ReachableDispenser> rd = new ArrayList<>(reachableDispensers);
        // Filter
        rd.removeIf(r -> !r.type().equals(t));
        // Sort
        rd.sort((a, b) -> a.distance() - b.distance());

        return rd.size() > 0 ? rd.get(0) : null;
    }

    /**
     * Gets the nearest point next to a dispenser with the provided type measured in Manhattan distance.
     * 
     * @param type the type of the dispenser
     * @return the nearest point next to a dispenser with the provided type
     */
    public Point getNearestRelativeManhattanDispenser(String type) {
        List<Thing> d = new ArrayList<>(things);
        // Filter
        d.removeIf(r -> !r.details.equals(type) || !r.type.equals(Thing.TYPE_DISPENSER));
        // Sort
        d.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));

        return d.size() > 0 ? new Point(d.get(0).x, d.get(0).y) : null;
    }
    
    /**
     * Gets all dispenser in vision.
     * 
     * @return the nearest point next to a dispenser with the provided type
     */
    public List<Thing> getDispenser() {
        List<Thing> d = new ArrayList<>(things);
        // Filter
        d.removeIf(r -> !r.type.equals(Thing.TYPE_DISPENSER));
        // Sort
        //d.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));

        return d.size() > 0 ? d : null;
    }

    /**
     * Gets the position of an abandoned block in vision.
     *  
     * @param detail the type of block
     * @return the position of the block if there is one, otherwise null
     */
    public Point getAbandonedBlockPosition(String detail) {
        // Test if agent in vision
        for (Thing t : things) {
            if (t.type.equals(Thing.TYPE_ENTITY)) {
                return null;
            }
        }
        // Look for block
        for (Thing t : things) {
            if (t.type.equals(Thing.TYPE_BLOCK) && t.details.equals(detail)) {
                return new Point(t.x, t.y);
            }
        }
        return null;
    }

    /**
     * Gets the Thing at the relative position.
     * 
     * @param p the relative position
     * @return the thing at the position or null if the cell is empty
     */
    public Thing getThingAt(Point p) {
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets the thing which is connected to the agent at the provided position.
     * 
     * @param p the relative point
     * @return the connected thing at the position or null if there is no connected thing at the position
     */
    public Thing getConnectedThingAt(Point p) {
        for (Point oap : ownAttachedPoints) {
            if (oap.equals(p)) {
                for (Thing t : things) {
                    if (t.x == p.x && t.y == p.y && !t.type.equals(Thing.TYPE_DISPENSER)) {
                        return t;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the agent id.
     * 
     * @return the agent id
     */
    public int getAgentId() {
        String id = agentShortName.substring(team.length());
        return Integer.parseInt(id);
    }

    /**
     * Gets the Thing which is at the position of a clock wise rotated point.
     * 
     * @param p the point which get rotated
     * @return the thing at the position or null if the cell is empty
     */
    public Thing getThingCRotatedAt(Point p) {
        Point rotated = new Point(-p.y, p.x);
        return getThingAt(rotated);
    }

    /**
     * Gets the Thing which is at the position of a counter clock wise rotated point.
     * 
     * @param p the point which get rotated
     * @return the thing at the position or null if the cell is empty
     */
    public Thing getThingCCRotatedAt(Point p) {
        Point rotated = new Point(p.y, -p.x);
        return getThingAt(rotated);
    }

    /**
     * Gets the Thing which is at the provided direction.
     * 
     * @param d the direction
     * @return the thing at the direction or null if the cell is empty
     */
    public Thing getThingAt(String d) {
        Point p = DirectionUtil.getCellInDirection(d);
        return getThingAt(p);
    }

    /**
     * Gets a Thing of a type at the provided direction.
     * 
     * @param d the direction
     * @param type the type of the thing
     * @return the thing or null if there is no thing with the provided type at the position
     */
    public Thing getThingWithTypeAt(String d, String type) {
        Point p = DirectionUtil.getCellInDirection(d);
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y && t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets a Thing of a type at the provided position.
     * 
     * @param p the position
     * @param type the type of the thing
     * @return the thing or null if there is no thing with the provided type at the position
     */
    public Thing getThingWithTypeAt(Point p, String type) {
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y && t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets a Thing of a type and with a certain detail at the provided direction.
     * 
     * @param d the direction
     * @param type the type of the thing
     * @param detail the detail of the thing
     * @return the thing or null if there is no thing with the provided type and detail at the position
     */    
    public Thing getThingWithTypeAndDetailAt(String d, String type, String detail) {
        Point p = DirectionUtil.getCellInDirection(d);
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y && t.type.equals(type) && t.details.equals(detail)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Tests if a certain position is marked at forbidden.
     * 
     * @param p the point to test
     * @return true if the position is marked as forbidden
     */
    public boolean isForbidden(Point p) {
        for (ForbiddenThing t : forbiddenThings) {
            if (t.position().equals(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Marks a position as forbidden for a certain period of time.
     * 
     * @param p the position to mark
     * @param duration the duration of the limitation
     */
    public void addForbiddenThing(Point p, int duration) {
        ForbiddenThing t = new ForbiddenThing(p, this.step, duration);
        forbiddenThings.add(t);
    }

    /**
     * Sets the position of the agent.
     * 
     * @param position the position of the agent
     */
    public void setPosition(Point position) {
        this.position = position;
    }

    /**
     * Gets a String containing of path finding information.
     * 
     * @return the String containing path finding information
     */
    public String reachablesToString() {
        StringBuilder b = new StringBuilder();
        for (ReachableDispenser d : reachableDispensers) {
            b.append(d);
            b.append(System.lineSeparator());
        }
        for (ReachableGoalZone d : reachableGoalZones) {
            b.append(d);
            b.append(System.lineSeparator());
        }
        for (ReachableRoleZone d : reachableRoleZones) {
            b.append(d);
            b.append(System.lineSeparator());
        }
        return b.toString();
    }

    /**
     * Gets the agent report of the current step.
     * 
     * @return the agent report
     */
    public AgentReport getAgentReport() {
        // Calculate available Actions
        Set<String> availableActions = new HashSet<>();
        Role d = roles.get("default");
        if (d != null) {
            availableActions.addAll(d.actions());
        }
        Role r = roles.get(role);
        if (r != null) {
            availableActions.addAll(r.actions());
        }
        // Calculate dispenser
        int[] distanceDispenser = {999, 999, 999, 999, 999};
        for (ReachableDispenser dispenser : reachableDispensers) {
            int i = Integer.parseInt(dispenser.type().name().substring(10));
            distanceDispenser[i] = Math.min(distanceDispenser[i], dispenser.distance());
        }
        int distGoalZone = 999;
        Point nearestGoalZone = new Point(0, 0);
        List<Point> uniqueGoalZones = new ArrayList<Point>();
        Point gameMapSize = Navi.<INaviAgentV1>get().getGameMapSize(getAgentShortName());
        for (ReachableGoalZone goalZone : reachableGoalZones) {
            if (goalZone.distance() < distGoalZone) {
                distGoalZone = goalZone.distance();
                nearestGoalZone = goalZone.position();
            }
            if (uniqueGoalZones.size() == 0) {
                uniqueGoalZones.add(goalZone.position());
            }
            else {
                for (Point uniqueGoalZone : uniqueGoalZones) {
                    // Goal zone already in uniqueGoalZones then check next reachable goal zone
                    if (!DirectionUtil.pointsWithinDistance(uniqueGoalZone, goalZone.position(), gameMapSize, 15)) {
                        uniqueGoalZones.add(goalZone.position());
                        break;
                    }
                }
            }
        }
        int distRoleZone = 999;
        for (ReachableRoleZone roleZone : reachableRoleZones) {
            if (roleZone.distance() < distRoleZone) {
                distRoleZone = roleZone.distance();
            }
        }
        AgentLogger.fine(getAgentShortName() + " Belief",
                "uniqueGoalZones: " + uniqueGoalZones.toString());
        AgentLogger.fine(getAgentShortName() + " Belief",
                "nearestGoalZone: " + nearestGoalZone.toString());
        Point goalZone2 = new Point(0, 0);
        int numOfDistinctGoalZones = uniqueGoalZones.size();
        // Select a different goal zone (not the nearest goal zone)
        for (Point uniqueGoalZone : uniqueGoalZones) {
            if (!nearestGoalZone.equals(new Point(0, 0))
                    && !DirectionUtil.pointsWithinDistance(uniqueGoalZone, nearestGoalZone, gameMapSize, 15)) {
                goalZone2 = uniqueGoalZone;
                break;
            }
        }
        AgentLogger.fine(getAgentShortName() + " Belief",
                "goalZone2: " + goalZone2.toString());

        return new AgentReport(attachedThings, energy, deactivated, availableActions,
            position, distanceDispenser, distGoalZone, groupDesireType, step, agentFullName,
            numOfDistinctGoalZones, nearestGoalZone, goalZone2, groupDesireBlockDetail, distRoleZone);
    }

    /**
     * Sets the group desire type.
     * 
     * @param groupDesireType the group desire type
     */
    public void setGroupDesireType(String groupDesireType) {
        this.groupDesireType = groupDesireType;
    }

    /**
     * Gets the group desire type.
     * 
     * @return the group desire type
     */
    public String getGroupDesireType() {
        return groupDesireType;
    }

    /**
     * Gets tasks which started at the current step.
     * 
     * @return a list of new tasks
     */
    public List<TaskInfo> getNewTasks() {
        return newTasks;
    }

    /**
     * Gets the task with the provided name.
     * 
     * @param name the name of the task
     * @return the task with the provided name or null if there doesn't exist a task with the name
     */
    public TaskInfo getTask(String name) {
        for (TaskInfo info : taskInfo) {
            if (info.name.equals(name)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Gets the belief as a debug String.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder()
                .append("Simulation Beliefs:")
                .append(System.lineSeparator())
                .append("Name: ").append(agentFullName)
                .append(System.lineSeparator())
                .append("Team: ").append(team)
                .append(System.lineSeparator())
                .append("TeamSize: ").append(teamSize)
                .append(System.lineSeparator())
                .append("Steps: ").append(steps)
                .append(System.lineSeparator())
                .append("Roles: ")
                .append(System.lineSeparator());
        for (Role r : roles.values()) {
            b.append(r.toJSON())
                    .append(System.lineSeparator());
        }
        b.append(System.lineSeparator())
                .append("Step Beliefs:")
                .append(System.lineSeparator())
                .append("Things: ");
        for (Thing t : things) {
            b.append(t.toJSON())
                    .append(System.lineSeparator());
        }
        if (things.isEmpty()) {
            b.append(System.lineSeparator());
        }
        b.append("Tasks: ")
                .append(System.lineSeparator());
        for (TaskInfo t : taskInfo) {
            b.append(t.toJSON())
                    .append(System.lineSeparator());
        }
        b.append("Norms: ")
                .append(System.lineSeparator());
        for (NormInfo t : normsInfo) {
            b.append(t.toJSON())
                    .append(System.lineSeparator());
        }
        b.append("Score: ").append(score)
                .append(System.lineSeparator())
                .append("Last Action: ").append(lastAction)
                .append(System.lineSeparator())
                .append("Last Action Result: ").append(lastActionResult)
                .append(System.lineSeparator());
        b.append("Last Action Parameter: ");
        for (String t : lastActionParams) {
            b.append(t + " | ");
        }
        b.append(System.lineSeparator())
                .append("Attached Things: ");
        for (Point t : attachedPoints) {
            b.append("[" + t.x + ", " + t.y + "] | ");
        }
        b.append(System.lineSeparator())
                .append("Energy: ").append(energy)
                .append(System.lineSeparator())
                .append("Deactivated: ").append(deactivated)
                .append(System.lineSeparator())
                .append("Role: ").append(role)
                .append(System.lineSeparator())
                .append("Step Events: ")
                .append(System.lineSeparator());
        for (StepEvent t : stepEvents) {
            b.append(t.toString())
                    .append(System.lineSeparator());
        }
        b.append("Violations: ");
        for (String t : violations) {
            b.append(t + " | ");
        }
        b.append(System.lineSeparator())
                .append("Goal Zones: ");
        for (Point t : goalZones) {
            b.append("[" + t.x + ", " + t.y + "] | ");
        }
        b.append(System.lineSeparator())
                .append("Role Zones: ");
        for (Point t : roleZones) {
            b.append("[" + t.x + ", " + t.y + "] | ");
        }
        return b.toString();
    }

    private void updateAttachedThings() {
        attachedThings.clear();

        for (Point p : ownAttachedPoints) {
            Thing t = getThingAt(p);
            attachedThings.add(t);
        }
    }

    private void updateNewTasks() {
        newTasks.clear();
        // Compare with last step
        for (TaskInfo n : taskInfo) {
            boolean isNew = true;
            for (TaskInfo o : taskInfoAtLastStep) {
                if (o.name.equals(n.name)) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                newTasks.add(n);
            }
        }
    }

    private void updatePosition() {
        if (lastAction != null && !lastActionResult.equals(ActionResults.FAILED) && lastAction.equals(Actions.MOVE)) {
            if (lastActionParams.size() == 0) return;
            String dir = lastActionParams.get(0);
            // Success
            if (lastActionResult.equals(ActionResults.SUCCESS)) {
                move(dir);
                if (lastActionParams.size() == 2) {
                    String dir2 = lastActionParams.get(1);
                    move(dir2);
                }
            }
            // Partial Success
            if (lastActionResult.equals(ActionResults.PARTIAL_SUCCESS)) {
                move(dir);
            }
        }
    }

    private void clearLists() {
        // Remove old connection reports (step is not updated yet is actually from last step)
        connectionReports.removeIf(r -> r.step == step - 1);
        // copy things
        taskInfoAtLastStep = new HashSet<>(taskInfo);
        // clearing
        roles.clear();
        things.clear();
        marker.clear();
        taskInfo.clear();
        normsInfo.clear();
        lastActionParams.clear();
        attachedThings.clear();
        attachedPoints.clear();
        stepEvents.clear();
        violations.clear();
        goalZones.clear();
        roleZones.clear();
    }

    private void move(String dir) {
        lastMoveDirection = dir;
        switch (dir) {
            case "n":
                position.y = mapSize == null ? position.y - 1 : (((position.y + mapSize.y - 1 - mapTopLeft.y) % mapSize.y) + mapTopLeft.y);
                break;
            case "e":
                position.x = mapSize == null ? position.x + 1 : (((position.x + mapSize.x + 1 - mapTopLeft.x) % mapSize.x) + mapTopLeft.x);
                break;
            case "s":
                position.y = mapSize == null ? position.y + 1 : (((position.y + mapSize.y + 1 - mapTopLeft.y) % mapSize.y) + mapTopLeft.y);
                break;
            case "w":
                position.x = mapSize == null ? position.x - 1 : (((position.x + mapSize.x - 1 - mapTopLeft.x) % mapSize.x) + mapTopLeft.x);
                break;
        }
    }

    private static record ForbiddenThing(Point position, int stepDiscovered, int duration) {
    }

    private static record ConnectionReport(String agent, int step, List<Point> points) {
    }

    /**
     * Gets the absolute position from a agent.
     * 
     * @return the absolute position
     */
    public Point getAbsolutePosition() {
        return absolutePosition;
    }
    
    /**
     * Updates the position of an agent (used from the outside).
     * 
     */
    public void updatePositionFromExternal() {
        setMapSize(AgentCooperations.mapSize.x, AgentCooperations.mapSize.y);
        String dir = null;
        AgentLogger.info(Thread.currentThread().getName() + " updatePositionFromExternal - Agent: " + agentShortName + " , Step: " +  step + " , Vorher: " +  getPosition());
        if (lastAction != null && lastAction.equals(Actions.MOVE) && !lastActionResult.equals(ActionResults.FAILED)) {
            // Success
            if (lastActionResult.equals(ActionResults.SUCCESS)) {
                AgentLogger.info(Thread.currentThread().getName() + " updatePositionFromExternal - Agent: " + agentShortName + " , Step: " +  step + " , Success: " +  lastActionParams);
                
                for (int i = 0; i < lastActionParams.size(); i++) {
                    dir = lastActionParams.get(i);
                    moveOld(dir);
                    moveNonModuloPosition(dir);
                    moveMapSizePosition(dir);
                }
            }

            // Partial Success (Only realy OK for max speed two ?!? Maybe compare changed vision for better results ?)
            if (lastActionResult.equals(ActionResults.PARTIAL_SUCCESS)) {
                AgentLogger.info(Thread.currentThread().getName() + " updatePositionFromExternal - Agent: " + agentShortName + " , Step: " +  step + " , Partial: " +  lastActionParams);
                moveOld(lastActionParams.get(0));
                moveNonModuloPosition(lastActionParams.get(0));
                moveMapSizePosition(lastActionParams.get(0));
            }
        }
        
        position = calcPositionModulo(position);
        AgentLogger.info(Thread.currentThread().getName() + " updatePositionFromExternal - Agent: " + agentShortName + " , Step: " +  step + " , Nachher: " +  getPosition());
    }
    
    /**
     * recalculates the position of an agent using modulo with map size.
     * 
     * @param position - non modulo position
     * @return modulo position
     */
    public Point calcPositionModulo(Point position) {
        setMapSize(AgentCooperations.mapSize.x, AgentCooperations.mapSize.y);
        position.x = (((position.x % mapSize.x) + mapSize.x) % mapSize.x);
        position.y = (((position.y % mapSize.y) + mapSize.y) % mapSize.y);
        return position;
    }
        
    private Point nonModuloPosition = new Point(0, 0);
    private Point exploreMapSizePosition = new Point(0, 0);
    
    /**
     * Gets the non modulo position of an agent as point.
     * 
     * @return the non modulo position of an agent as point
     */
    public Point getNonModPosition() {
        return nonModuloPosition;
    }

    /**
     * Sets the top left position of the map.
     * This is only used if the map size is already discovered.
     * 
     * @param topLeft the position of the top left cell in the game map
     */
    public void setTopLeft(Point topLeft) {
        this.mapTopLeft = topLeft; 
    }
    
    /**
     * Sets the non modulo position of an agent as point.
     * 
     * @param pos - the non modulo position of an agent as point
     */
    public void setNonModPosition(Point pos) {
        nonModuloPosition = new Point(pos);
    }
    
    private void moveNonModuloPosition(String dir) {
        switch (dir) {
            case "n":
                nonModuloPosition.y -= 1;
                break;
            case "e":
                nonModuloPosition.x += 1;
                break;
            case "s":
                nonModuloPosition.y += 1;
                break;
            case "w":
                nonModuloPosition.x -= 1;
                break;
        }
    }
    
    private void moveOld(String dir) {
        switch (dir) {
            case "n":
                position.y -= 1;
                break;
            case "e":
                position.x += 1;
                break;
            case "s":
                position.y += 1;
                break;
            case "w":
                position.x -= 1;
                break;
        }
    }
    
    /**
     * Gets the exploreMapSizePosition position of an agent as point.
     * 
     * @return the exploreMapSizePosition of an agent as point
     */
    public Point getMapSizePosition() {
        return exploreMapSizePosition;
    }
    
    /**
     * Sets the exploreMapSizePosition of an agent as point.
     * 
     * @param pos - the new exploreMapSizePosition of an agent as point
     */
    public void setMapSizePosition(Point pos) {
        exploreMapSizePosition = new Point(pos);
    }
    
    private void moveMapSizePosition(String dir) {
        switch (dir) {
            case "n":
                exploreMapSizePosition.y -= 1;
                break;
            case "e":
                exploreMapSizePosition.x += 1;
                break;
            case "s":
                exploreMapSizePosition.y += 1;
                break;
            case "w":
                exploreMapSizePosition.x -= 1;
                break;
        }
    }
    
    /**
     * Gets a list of all reachable dispensers.
     * 
     * @return list of all reachable dispensers
     */
    public synchronized List<ReachableDispenser> getReachableDispensersX() {
        List<ReachableDispenser> reachableDispensers = new ArrayList<>(this.reachableDispensers);
        List<ReachableDispenser> reachableDispensersX = new ArrayList<>();
        
        for (ReachableDispenser rd : reachableDispensers) {
            if (rd.data().equals("x")) {
                Point agentPos = getPosition();
                Point pos = new Point((((rd.position().x % mapSize.x) + mapSize.x) % mapSize.x), 
                        (((rd.position().y % mapSize.y) + mapSize.y) % mapSize.y));          
                int distance = Math.min(Math.abs(pos.x - agentPos.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(pos.x - agentPos.x)) % mapSize.x)
                        + Math.min(Math.abs(pos.y - agentPos.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(pos.y - agentPos.y)) % mapSize.y);               
                int direction = DirectionUtil.stringToInt(DirectionUtil.getDirection(agentPos, pos));             
                ReachableDispenser rdnew = new ReachableDispenser(pos, rd.type(),  distance,  direction, rd.data());
                reachableDispensersX.add(rdnew);
            }
        }
        
        return reachableDispensersX;
    }
    
    /**
     * Gets a list of all reachable goal zones.
     * 
     * @return list of all reachable goal zones
     */
    public List<ReachableGoalZone> getReachableGoalZonesX() {
        List<ReachableGoalZone> reachableGoalZones = new ArrayList<>(this.reachableGoalZones);
        List<ReachableGoalZone> reachableGoalZonesX = new ArrayList<>();
        
        for (ReachableGoalZone rd : reachableGoalZones) {
            Point agentPos = getPosition();
            Point pos = new Point((((rd.position().x % mapSize.x) + mapSize.x) % mapSize.x), 
                    (((rd.position().y % mapSize.y) + mapSize.y) % mapSize.y));          
            int distance = Math.min(Math.abs(pos.x - agentPos.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(pos.x - agentPos.x)) % mapSize.x)
                    + Math.min(Math.abs(pos.y - agentPos.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(pos.y - agentPos.y)) % mapSize.y);     
            int direction = DirectionUtil.stringToInt(DirectionUtil.getDirection(agentPos, pos));
            ReachableGoalZone rdnew = new ReachableGoalZone(pos, distance, direction);
            reachableGoalZonesX.add(rdnew);
        }
 
        reachableGoalZonesX.sort((a, b) -> a.distance() - b.distance());
        return reachableGoalZonesX;
    }
    
    /**
     * Gets a list of all reachable role zones.
     * 
     * @return list of all reachable role zones
     */
    public List<ReachableRoleZone> getReachableRoleZonesX() {
        List<ReachableRoleZone> reachableRoleZones = new ArrayList<>(this.reachableRoleZones);
        List<ReachableRoleZone> reachableRoleZonesX = new ArrayList<>();
        
        for (ReachableRoleZone rd : reachableRoleZones) {
            Point agentPos = getPosition();
            Point pos = new Point((((rd.position().x % mapSize.x) + mapSize.x) % mapSize.x), 
                    (((rd.position().y % mapSize.y) + mapSize.y) % mapSize.y));          
            int distance = Math.min(Math.abs(pos.x - agentPos.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(pos.x - agentPos.x)) % mapSize.x)
                    + Math.min(Math.abs(pos.y - agentPos.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(pos.y - agentPos.y)) % mapSize.y);    
            int direction = DirectionUtil.stringToInt(DirectionUtil.getDirection(agentPos, pos));
            ReachableRoleZone rdnew = new ReachableRoleZone(pos, distance, direction);
            reachableRoleZonesX.add(rdnew);
        }
        
        return reachableRoleZonesX;
    }
    
    /**
     * Sets the reachableGoalZones without pathfinding.
     * 
     * @param inSet - Set of goal zone points
     */
    public void updateRgz(Set<Point> inSet) {
        List<ReachableGoalZone> reachableGoalZonesX = new ArrayList<>();
        
        for (Point inPos : inSet) {
            Point pos = inPos;
            Point agentPos = getPosition();     
            int distance = Math.min(Math.abs(pos.x - agentPos.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(pos.x - agentPos.x)) % mapSize.x)
                    + Math.min(Math.abs(pos.y - agentPos.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(pos.y - agentPos.y)) % mapSize.y);    
            int direction = DirectionUtil.stringToInt(DirectionUtil.getDirection(agentPos, pos));
            ReachableGoalZone rdnew = new ReachableGoalZone(pos, distance, direction);
            reachableGoalZonesX.add(rdnew);
        }
   
        reachableGoalZonesX.sort((a, b) -> a.distance() - b.distance());
        this.reachableGoalZones = reachableGoalZonesX;
    }
    
    /**
     * Sets the reachableDispensers without pathfinding.
     * 
     * @param inSet - Set of dispensers
     */
    public void updateDisp(Set<Thing> inSet) {
        List<ReachableDispenser> reachableDispensersX = new ArrayList<>();
        
        for (Thing inThing : inSet) {
            Point pos = new Point(inThing.x, inThing.y);
            Point agentPos = getPosition();     
            int distance = Math.min(Math.abs(pos.x - agentPos.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(pos.x - agentPos.x)) % mapSize.x)
                    + Math.min(Math.abs(pos.y - agentPos.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(pos.y - agentPos.y)) % mapSize.y);    
            int direction = DirectionUtil.stringToInt(DirectionUtil.getDirection(agentPos, pos));
            ReachableDispenser rdnew = new ReachableDispenser(pos,  Convert.dispenserToCellType(inThing.details), distance, direction, "x");
            reachableDispensersX.add(rdnew);
        }
   
        reachableDispensersX.sort((a, b) -> a.distance() - b.distance());
        this.reachableDispensers = reachableDispensersX;
    }

    /**
     * Sets the map size.
     * 
     * @param x the width of the map
     * @param y the height of the map
     */
    public void setMapSize(int x, int y) {
        mapSize = new Point(x, y);
    }
    
    /**
     * Tests if the agent or an attached Thing to the agent is overlapping with a clear marker.
     * 
     * @return true if the agent or an attached Thing to the agent is overlapping with a clear marker
     */
    public boolean isInClearDanger() {
        List<Point> toTest = new ArrayList<>(getOwnAttachedPoints());
        toTest.add(new Point(0, 0));

        for (Thing m : marker) {
            for (Point p : toTest) {
                if (p.equals(new Point(m.x, m.y))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the direction of the last move.
     * 
     * @return the direction of the last move or "n" if no move was made before.
     */
    public String getLastMoveDirection() {
        return lastMoveDirection;
    }
}
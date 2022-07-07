package de.feu.massim22.group3.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.Point;

import de.feu.massim22.group3.agents.Desires.BDesires.GroupDesireTypes;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import de.feu.massim22.group3.agents.Reachable.ReachableTeammate;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.ZoneType;
import de.feu.massim22.group3.utils.Convert;
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
    private Set<Thing> thingsAtLastStep = new HashSet<>();
    private List<ReachableDispenser> reachableDispensers = new ArrayList<>();
    private List<ReachableGoalZone> reachableGoalZones = new ArrayList<>();
    private List<ReachableRoleZone> reachableRoleZones = new ArrayList<>();
    private List<ReachableTeammate> reachableTeammates = new ArrayList<>();
    private List<ForbiddenThing> forbiddenThings = new ArrayList<>();
    private String groupDesireType = GroupDesireTypes.NONE;
    private List<ConnectionReport> connectionReports = new ArrayList<>();

    public Belief(String agentName) {
        this.agentShortName = agentName;
    }

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
                    things.add(new Thing(x, y, type, details));
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
                    case "violation" :
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
                default:
                    AgentLogger.warning("Percept not transfered to Belief: " + percept.getName());
                }
            }
        updateOwnAttachedPoints();
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
        }
    }

    void updateFromPathFinding(List<Parameter> points) {
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

    private record AgentSurveyStepEvent(String name, String role, int energy) implements StepEvent {
        public String toString() {
            return "Agent " + name + " with role " + role + " and energy " + energy;
        }
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

    public boolean isSimEnd() {
        return simEnd;
    }

    public String getTeam() {
        return team;
    }

    public String getAgentShortName() {
        return agentShortName;
    }

    public String getAgentFullName() {
        return agentFullName;
    }

    public int getVision() {
        Role r = roles.get(role);
        return r == null ? 0 : r.vision();
    }

    public int getStep() {
        return step;
    }

    int getSteps() {
        return steps;
    }

    int getTeamSize() {
        return teamSize;
    }

    public Set<Thing> getThings() {
        return things;
    }

    public Set<TaskInfo> getTaskInfo() {
        taskInfo.removeIf(e -> e.deadline < step);
        return taskInfo;
    }

    public Set<NormInfo> getNormsInfo() {
        return normsInfo;
    }

    public long getScore() {
        return score;
    }

    public String getLastAction() {
        return lastAction;
    }

    public String getLastActionResult() {
        return lastActionResult;
    }

    public String getLastActionDebugString() {
        String paras = "";
        for (String s: lastActionParams) {
            paras += " " + s;
        }
        return lastAction + paras;
    }

    public void setLastActionIntention(String lastActionIntention) {
        this.lastActionIntention = lastActionIntention;
    }

    public String getLastActionIntention() {
        return lastActionIntention;
    }

    public List<String> getLastActionParams() {
        return lastActionParams;
    }

    public List<Thing> getAttachedThings() {
        return attachedThings;
    }

    public List<Point> getOwnAttachedPoints() {
        return ownAttachedPoints;
    }

    public List<Point> getAttachedPoints() {
        return attachedPoints;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public String getRoleName() {
        return role;
    }

    public Role getRole() {
        return roles.get(role);
    }

    public Map<String, Role> getRoles() {
        return roles;
    }

    public Role getRoleByActions(String[] actions) {
        for (Role r : roles.values()) {
            boolean allFound = true;
            for (String action : actions) {
                if (!r.actions().contains(action)) {
                    allFound = false;
                }
            }
            if (allFound) {
                return r;
            }
        }
        return null;
    }

    public List<StepEvent> getStepEvents() {
        return stepEvents;
    }

    public List<String> getViolations() {
        return violations;
    }

    public List<Point> getGoalZones() {
        return goalZones;
    }

    public List<Point> getRoleZones() {
        return roleZones;
    }

    public Point getPosition() {
        return position;
    }

    public List<ReachableDispenser> getReachableDispensers() {
        return reachableDispensers;
    }

    public List<ReachableGoalZone> getReachableGoalZones() {
        return reachableGoalZones;
    }

    public List<ReachableRoleZone> getReachableRoleZones() {
        return reachableRoleZones;
    }

    public List<ReachableTeammate> getReachableTeammates() {
        return reachableTeammates;
    }

    public ReachableRoleZone getNearestRoleZone() {
        // Zone is sorted
        return reachableRoleZones.size() > 0 ? reachableRoleZones.get(0) : null;
    }
    
    //Melinda
    public Point getNearestRelativeManhattenRoleZone() {
        roleZones.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));
        return roleZones.size() > 0 ? roleZones.get(0) : null;
    }
    //Melinda Ende

    public ReachableGoalZone getNearestGoalZone() {
        // Zone is sorted
        return reachableGoalZones.size() > 0 ? reachableGoalZones.get(0) : null;
    }

    public Point getNearestRelativeManhattenGoalZone() {
        goalZones.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));
        return goalZones.size() > 0 ? goalZones.get(0) : null;
    }

    public ReachableDispenser getNearestDispenser(CellType t) {
        List<ReachableDispenser> rd = new ArrayList<>(reachableDispensers);
        // Filter
        rd.removeIf(r -> !r.type().equals(t));
        // Sort
        rd.sort((a, b) -> a.distance() - b.distance());

        return rd.size() > 0 ? rd.get(0) : null;
    }

    public Point getNearestRelativeManhattenDispenser(String type) {
        List<Thing> d = new ArrayList<>(things);
        // Filter
        d.removeIf(r -> !r.details.equals(type) || !r.type.equals(Thing.TYPE_DISPENSER));
        // Sort
        d.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));

        return d.size() > 0 ? new Point(d.get(0).x, d.get(0).y) : null;
    }

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

    public Thing getThingAt(Point p) {
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y) {
                return t;
            }
        }
        return null;
    }

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

    public int getAgentId() {
        String id = agentShortName.substring(team.length());
        return Integer.parseInt(id);
    }

    public Thing getThingCRotatedAt(Point p) {
        Point rotated = new Point(-p.y, p.x);
        return getThingAt(rotated);
    }

    public Thing getThingCCRotatedAt(Point p) {
        Point rotated = new Point(p.y, -p.x);
        return getThingAt(rotated);
    }

    public Thing getThingAt(String d) {
        Point p = DirectionUtil.getCellInDirection(d);
        return getThingAt(p);
    }

    public Thing getThingWithTypeAt(String d, String type) {
        Point p = DirectionUtil.getCellInDirection(d);
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y && t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }

    public Thing getThingWithTypeAt(Point p, String type) {
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y && t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }

    public Thing getThingWithTypeAndDetailAt(String d, String type, String detail) {
        Point p = DirectionUtil.getCellInDirection(d);
        for (Thing t : things) {
            if (t.x == p.x && t.y == p.y && t.type.equals(type) && t.details.equals(detail)) {
                return t;
            }
        }
        return null;
    }

    public boolean isForbidden(Point p) {
        for (ForbiddenThing t : forbiddenThings) {
            if (t.position().equals(p)) {
                return true;
            }
        }
        return false;
    }

    public void addForbiddenThing(Point p, int duration) {
        ForbiddenThing t = new ForbiddenThing(p, this.step, duration);
        forbiddenThings.add(t);
    }

    void setPosition(Point position) {
        this.position = position;
    }

    String reachablesToString() {
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

    AgentReport getAgentReport() {
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
        for (ReachableGoalZone goalZone : reachableGoalZones) {
            distGoalZone = Math.min(distGoalZone, goalZone.distance());
        }
        return new AgentReport(attachedThings, energy, deactivated, availableActions, position, distanceDispenser, distGoalZone, groupDesireType, step, agentFullName);
    }

    public void setGroupDesireType(String groupDesireType) {
        this.groupDesireType = groupDesireType;
    }

    public String getGroupDesireType() {
        return groupDesireType;
    }

    public List<TaskInfo> getNewTasks() {
        return newTasks;
    }

    public TaskInfo getTask(String name) {
        for (TaskInfo info : taskInfo) {
            if (info.name.equals(name)) {
                return info;
            }
        }
        return null;
    }

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
            }
            // Partial Success
            /*
            if (lastActionResult.equals(ActionResults.PARTIAL_SUCCESS)) {
                Role currentRole = roles.get(role);
                // With max speed 2 we can be sure that agent moved one cell
                if (currentRole != null && currentRole.speed()[1] < 3) {
                    move(dir);
                }
                // TODO Partial Success with speed > 2
                else {
                    // Try to guess the position with information from last step
                }
            } */
        }
    }

    private void clearLists() {
        // Remove old connection reports (step is not updated yet is actually from last step)
        connectionReports.removeIf(r -> r.step == step - 1);
        // copy things
        thingsAtLastStep = new HashSet<>(things);
        taskInfoAtLastStep = new HashSet<>(taskInfo);
        // clearing
        roles.clear();
        things.clear();
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

    private static record ForbiddenThing(Point position, int stepDiscovered, int duration) {
    }

    private static record ConnectionReport(String agent, int step, List<Point> points) {
    }

    // Melinda
    public void updatePositionFromExternal() {
        String dir = null;

        if (lastAction != null && lastAction.equals(Actions.MOVE) && !lastActionResult.equals(ActionResults.FAILED)) {
            // Success
            if (lastActionResult.equals(ActionResults.SUCCESS)) {
                for (int i = 0; i < lastActionParams.size(); i++) {
                    dir = lastActionParams.get(i);
                    move(dir);
                }
            }

            // Partial Success (Only realy OK for max speed two ?!? Maybe compare changed vision for better results ?)
            if (lastActionResult.equals(ActionResults.PARTIAL_SUCCESS)) {
                move(dir);
            }
        }
    }
    // Melinda Ende
}
  package de.feu.massim22.group3.agents;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.awt.Point;

    import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
    import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
    import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
    import de.feu.massim22.group3.agents.Reachable.ReachableTeammate;
    import de.feu.massim22.group3.map.CellType;
    import de.feu.massim22.group3.map.ZoneType;
    import de.feu.massim22.group3.utils.logging.AgentLogger;
    import eis.iilang.Function;
    import eis.iilang.Identifier;
    import eis.iilang.Numeral;
    import eis.iilang.Parameter;
    import eis.iilang.ParameterList;
    import eis.iilang.Percept;
    import eis.iilang.TruthValue;
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
        private String name = "";
        private String team;
        private int teamSize;
        private int steps;
        private Map<String, Role> roles = new HashMap<>();
        
        // Step Beliefs
        private int step;
        private Set<Thing> things = new HashSet<>();
        private Set<TaskInfo> taskInfo = new HashSet<>();
        private Set<NormInfo> normsInfo = new HashSet<>();
        private long score;
        private String lastAction;
        private String lastActionResult;
        private List<String> lastActionParams = new ArrayList<>();
        
        private List<Point> attachedThings = new ArrayList<>();
        private int energy;
        private boolean deactivated;
        private String role = "default";
        private List<StepEvent> stepEvents = new ArrayList<>();
        private List<String> violations = new ArrayList<>();
        private List<Point> goalZones = new ArrayList<>();
        private List<Point> roleZones = new ArrayList<>();
        
        // Group 3 Beliefs
        private Point position = new Point(0, 0);
        private Set<Thing> thingsAtLastStep = new HashSet<>();
        private List<ReachableDispenser> reachableDispensers = new ArrayList<>();
        private List<ReachableGoalZone> reachableGoalZones = new ArrayList<>();
        private List<ReachableRoleZone> reachableRoleZones = new ArrayList<>();
        private List<ReachableTeammate> reachableTeammates = new ArrayList<>();

        Belief(String agentName) { 
            this.name = agentName;
        }
        
        void update(List<Percept> percepts) {
            clearLists();
            for (Percept percept : percepts) {
                List<Parameter> p = percept.getParameters();
                switch (percept.getName()) {
                case "step":
                    step = toNumber(p, 0, Integer.class);
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
                    attachedThings.add(new Point(attX, attY));
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
                    name = toStr(p, 0);
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
                    break;
                default:
                    AgentLogger.warning("Percept not transfered to Belief: " + percept.getName());
                }
            }
            
            updatePosition();
        }

        int getSteps() {
            return steps;
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
                // Data
                List<Parameter> paras = ((Function)p).getParameters();
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
                    ReachableDispenser rd = new ReachableDispenser(pos, CellType.valueOf(detail), distance, direction, data);
                    reachableDispensers.add(rd);
                }
                // Teammate
                if (!isZone && CellType.valueOf(detail) == CellType.TEAMMATE) {
                    ReachableTeammate rt = new ReachableTeammate(pos, data, distance, direction);
                    reachableTeammates.add(rt);
                }
            }
        }

        String getTeam() {
            return team;
        }

        public int getVision() {
            Role r = roles.get(role);
            if (r == null) { throw new IllegalArgumentException("Current role is not in existing roles"); }
            return r.vision();
        }
        
        public int getStep() {
            return step;
        }
        
        // Melinda
        int getTeamSize() {
            return teamSize;
        }
        // Melinda Ende

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

        public List<String> getLastActionParams() {
            return lastActionParams;
        }

        public List<Point> getAttachedThings() {
            return attachedThings;
        }

        public int getEnergy() {
            return energy;
        }

        public boolean isDeactivated() {
            return deactivated;
        }

        public String getRole() {
            return role;
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

        public List<ReachableDispenser> getReachableDispensers(){
            return reachableDispensers;
        }

        public List<ReachableGoalZone> getReachableGoalZones(){
            return reachableGoalZones;
        }

        public List<ReachableRoleZone> getReachableRoleZones(){
            return reachableRoleZones;
        }

        public List<ReachableTeammate> getReachableTeammates(){
            return reachableTeammates;
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
            for (ReachableTeammate d : reachableTeammates) {
                b.append(d);
                b.append(System.lineSeparator());
            }
            return b.toString();
        }
        
        public String toString() {
            StringBuilder b = new StringBuilder()
                .append("Simulation Beliefs:")
                .append(System.lineSeparator())
                .append("Name: ").append(name)
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
            for (Point t : attachedThings) {
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
        
        private void updatePosition() {
            if (lastAction != null && lastAction.equals(Actions.MOVE)) {
                String dir = lastActionParams.get(0);
                // Success
                if (lastActionResult.equals(ActionResults.SUCCESS)) {
                    move(dir);
                }
                // Partial Success
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
                }
            }
        }
        
        private void clearLists() {
            // copy things
            thingsAtLastStep = new HashSet<Thing>(things);
            // clearing
            roles.clear();
            things.clear();
            taskInfo.clear();
            normsInfo.clear();
            lastActionParams.clear();
            attachedThings.clear();
            stepEvents.clear();
            violations.clear();
            goalZones.clear();
            roleZones.clear();
        }

        private <T extends Number> T toNumber(List<Parameter> parameters, int index, Class<T> type) {
            Parameter p = parameters.get(index);
            if (!(p instanceof Numeral)) throw new IllegalArgumentException("Parameter is no Number");
            return type.cast(((Numeral)p).getValue());
        }
        
        private String toStr(List<Parameter> parameters, int index) {
            Parameter p = parameters.get(index);
            if (!(p instanceof Identifier)) throw new IllegalArgumentException("Parameter is no String");
            return (String)((Identifier)p).getValue();
        }

        private boolean toBool(List<Parameter> parameters, int index) {
            Parameter p = parameters.get(index);
            if (!(p instanceof TruthValue)) throw new IllegalArgumentException("Parameter is no Bool");
            return ((TruthValue)p).getValue() == "true";
        }
        
        private List<String> toStrList(List<Parameter> parameters, int index) {
            Parameter p = parameters.get(index);
            if (!(p instanceof ParameterList)) throw new IllegalArgumentException();
            List<String> result = new ArrayList<>();
            for (Parameter para : (ParameterList)p) {
                if (!(para instanceof Identifier)) throw new IllegalArgumentException();
                String s = (String)((Identifier)para).getValue();
                result.add(s);
            }
            return result;
        }
        
        private List<Integer> toIntList(List<Parameter> parameters, int index) {
            Parameter p = parameters.get(index);
            if (!(p instanceof ParameterList)) throw new IllegalArgumentException();
            List<Integer> result = new ArrayList<>();
            for (Parameter para : (ParameterList)p) {
                if (!(para instanceof Numeral)) throw new IllegalArgumentException();
                int s = (int)((Numeral)para).getValue();
                result.add(s);
            }
            return result;
        }
        
        private Set<Thing> toThingSet(List<Parameter> parameters, int index) {
            Parameter p = parameters.get(index);
            if (!(p instanceof ParameterList)) throw new IllegalArgumentException();
            Set<Thing> result = new HashSet<>();
            for (Parameter para : (ParameterList)p) {
                if (!(para instanceof Function)) throw new IllegalArgumentException();
                List<Parameter> funcParameter = ((Function)para).getParameters();
                int x = toNumber(funcParameter, 0, Integer.class);
                int y = toNumber(funcParameter, 1, Integer.class);
                String type = toStr(funcParameter, 2);
                result.add(new Thing(x, y, type, ""));
            }
            return result;
        }
        
        private Set<Subject> toSubjectSet(List<Parameter> parameters, int index) {
            Parameter p = parameters.get(index);
            if (!(p instanceof ParameterList)) throw new IllegalArgumentException();
            Set<Subject> result = new HashSet<>();
            for (Parameter para : (ParameterList)p) {
                if (!(para instanceof Function)) throw new IllegalArgumentException();
                List<Parameter> funcParameter = ((Function)para).getParameters();
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

        private void move(String dir) {
            switch (dir) {
                case "n": position.y -= 1; break;
                case "e": position.x += 1; break;
                case "s": position.y += 1; break;
                case "w": position.x -= 1; break;
            }
        }
    }
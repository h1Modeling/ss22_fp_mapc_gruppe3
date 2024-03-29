package de.feu.massim22.group3.communication;

import eis.AgentListener;
import eis.EnvironmentListener;
import eis.exceptions.ActException;
import eis.exceptions.AgentException;
import eis.exceptions.PerceiveException;
import eis.exceptions.RelationException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Percept;
import massim.eismassim.EnvironmentInterface;

import org.json.JSONObject;

import de.feu.massim22.group3.agents.Agent;
import de.feu.massim22.group3.agents.BdiAgentV1;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import de.feu.massim22.group3.map.INavi;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.debugger.DebugStepListener;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A scheduler for agent creation and execution.
 * EISMASSim scheduling needs to be enabled (via config), so that getAllPercepts()
 * blocks until new percepts are available!
 * (Also, queued and notifications should be disabled)
 * 
 * @author massim team
 * @author Heinz Stadler
 * @author Melinda Betz
 */
public class Scheduler implements AgentListener, EnvironmentListener, EisSender, DebugStepListener {

    /**
     * Holds configured agent data.
     */
    private class AgentConf {
        String name;
        String entity;
        String team;
        String className;
        int index;

        AgentConf(String name, String entity, String team, String className, int index){
            this.name = name;
            this.entity = entity;
            this.team = team;
            this.className = className;
            this.index = index;
        }
    }

    private EnvironmentInterface eis;
    private List<AgentConf> agentConfigurations = new Vector<>();
    private Map<String, Agent> agents = new HashMap<>();
    private boolean manualMode = false;
    private Queue<AgentStep> actionQueue = new ConcurrentLinkedQueue<>();
    private record AgentStep(String agentName, Action action) {}
    private boolean delay = false;
    private String overrideAgent = "";
    private Action overrideAction = null;

    /**
     * Create a new scheduler based on the given configuration file
     * @param path path to a java agents configuration file
     */
    public Scheduler(String path) {
        parseConfig(path);
    }

    /**
     * Parses the java agents config.
     * @param path the path to the config
     */
    private void parseConfig(String path) {
        try {
            var config = new JSONObject(new String(Files.readAllBytes(Paths.get(path, "javaagentsconfig.json"))));
            var agents = config.optJSONArray("agents");
            if (agents != null) {
                for (int i = 0; i < agents.length(); i++) {
                    var agentBlock = agents.getJSONObject(i);
                    var count = agentBlock.getInt("count");
                    var startIndex = agentBlock.getInt("start-index");
                    var agentPrefix = agentBlock.getString("agent-prefix");
                    var entityPrefix = agentBlock.getString("entity-prefix");
                    var team = agentBlock.getString("team");
                    var agentClass = agentBlock.getString("class");

                    for (int index = startIndex; index < startIndex + count; index++) {
                        agentConfigurations.add(
                                new AgentConf(agentPrefix + index, entityPrefix + index, team, agentClass, index));
                    }
                }
            }

            // Sets manual Step mode
            var manualMode = config.optBoolean("manualMode");
            if (manualMode) {
                this.manualMode = manualMode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects to an Environment Interface
     * @param ei the interface to connect to
     */
    public void setEnvironment(EnvironmentInterface ei) {
        this.eis = ei;
        MailService mailService = new MailService();
        Navi.<INavi>get().setMailService(mailService);
        for (AgentConf agentConf: agentConfigurations) {

            Agent agent = null;
            switch (agentConf.className) {
                case "BdiAgentV1":
                	agent = new BdiAgentV1(agentConf.name, mailService, this, agentConf.index);
                	Thread t = new Thread((Runnable)agent);
                	t.start();
                	break;
                case "BdiAgentV2":
                    agent = new BdiAgentV2(agentConf.name, mailService, agentConf.index);
                    break;
                default:
                    AgentLogger.warning("Unknown agent type/class " + agentConf.className);
            }
            if(agent == null) continue;

            mailService.registerAgent(agent, agentConf.team);

            Navi.get().registerAgent(agent.getName(), agentConf.team);
            Navi.<INavi>get().setDebugStepListener(this, manualMode);

            try {
                ei.registerAgent(agent.getName());
            } catch (AgentException e) {
                e.printStackTrace();
            }

            try {
                ei.associateEntity(agent.getName(), agentConf.entity);
                AgentLogger.info("associated agent \"" + agent.getName() + "\" with entity \"" + agentConf.entity + "\"");
            } catch (RelationException e) {
                e.printStackTrace();
            }

            ei.attachAgentListener(agent.getName(), this);
            agents.put(agentConf.name, agent);
        }
        ei.attachEnvironmentListener(this);
    }

    /**
     * Steps all agents and relevant infrastructure.
     */
    public void step() {
        // retrieve percepts for all agents
        List<Agent> newPerceptAgents = new ArrayList<>();
        agents.values().forEach(ag -> {
            try {
                var addList = new ArrayList<Percept>();
                var delList = new ArrayList<Percept>();
                eis.getPercepts(ag.getName()).values().forEach(pUpdate -> {
                    addList.addAll(pUpdate.getAddList());
                    delList.addAll(pUpdate.getDeleteList());
                });
                if (!addList.isEmpty() || !delList.isEmpty()) newPerceptAgents.add(ag);
                ag.setPercepts(addList, delList);
            } catch (PerceiveException ignored) { }
        });
        // log
        if (newPerceptAgents.size() > 0) {
            AgentLogger.info("");
            AgentLogger.info("------------------------------");
            AgentLogger.info("------------ STEP ------------");
            AgentLogger.info("------------------------------");
            AgentLogger.info("");
        }

        // for safety initStep at supervisor first
        newPerceptAgents.forEach(agent -> {
            if (agent instanceof Supervisable) {
                ((Supervisable)agent).initSupervisorStep();
            }
        });

        // step all agents which have new percepts
		newPerceptAgents.forEach(agent -> {
			// Notify multithreaded agents
			if (agent instanceof Runnable) {
				String sender = "Scheduler";
				Percept message = new Percept(EventName.UPDATE.name());
				agent.handleMessage(message, sender);
			}

			//Melinda
			else {
				Runnable runnable = () -> {
				    eis.iilang.Action action = null;
				    try {
				        action = agent.step();
				    } catch (Exception e) {
				        AgentLogger.warning(" Step failed for: " + agent.getName() + " , " + e);
				        e.printStackTrace(System.out);
                    }
					
					if (action != null) {
						try {
							eis.performAction(agent.getName(), action);
						} catch (ActException e) {
							AgentLogger.warning(
								"Could not perform action " + action.getName() + " for " + agent.getName());
						}
					}
				};
				Thread t1 = new Thread(runnable);
				t1.start();
			}
			//Melinda Ende
		});
		
        if(newPerceptAgents.size() == 0) try {
            Thread.sleep(100); // wait a bit in case no agents have been executed
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void handlePercept(String agent, Percept percept) {
        agents.get(agent).handlePercept(percept);
    }

    @Override
    public void handleStateChange(EnvironmentState newState) {}

    @Override
    public void handleFreeEntity(String entity, Collection<String> agents) {}

    @Override
    public void handleDeletedEntity(String entity, Collection<String> agents) {}

    @Override
    public void handleNewEntity(String entity) {}

	@Override
	public void send(Agent agent, Action action) {
        if (action != null) {
            // Manual Debug Mode
            if (manualMode) {
                actionQueue.add(new AgentStep(agent.getName(), action));
            }
            // Default Mode
            else {
                // Delay
                if (delay) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    // Override Action from Debugger or send calculated Action
                    if (overrideAgent.equals(agent.getName()) && overrideAction != null) {
                        eis.performAction(agent.getName(), overrideAction);
                        overrideAction = null;
                    } else {
                        eis.performAction(agent.getName(), action);
                    }
                } catch (ActException e) {
                    AgentLogger.warning("Could not perform action " + action.getName() + " for " + agent.getName());
                }
            }
        }
	}

    @Override
    public void debugStep() {
        for (AgentStep s : actionQueue) {
            try {
                eis.performAction(s.agentName(), s.action());
            } catch (ActException e) {
                AgentLogger.warning("Could not perform action " + s.action.getName() + " for " + s.agentName);
            }
        }
        actionQueue.clear();
    }

    @Override
    public void setDelay(boolean value) {
        this.delay = value;
    }

    @Override
    public void setAction(String agent, Action a) {
        overrideAgent = agent;
        overrideAction = a;   
    }
}

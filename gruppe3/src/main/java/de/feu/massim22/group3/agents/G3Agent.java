package massim.javaagents.agents;

import java.util.*;
import java.awt.Point;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import eis.iilang.*;
import massim.eismassim.Log;
import massim.javaagents.MailService;

public class G3Agent extends Agent {

	private int lastID = -2;
	private Identifier lastAction = new Identifier(" ");;
	private Identifier lastActionParams = new Identifier("n");
	private int circleSize = 1;
	private int directionCounter = 0;
	int distanceDispenser = 0;
	boolean loaded = false; // Agent has all blocks attached (in right order)
	boolean blockOK = false; // Are all blocks in the right order? (construct)

    ActiveTasks activeTasks = new ActiveTasks(); // Datastructure for all active tasks
    ActiveNorms activeNorms = new ActiveNorms(); // Datastructure for all active norms


	/**
	 * Constructor.
	 * 
	 * @param name - the agent's name
	 * @param mailbox -  the mail facility
	 */
	public G3Agent(String name, MailService mailbox) {
		super(name, mailbox);
	}

	@Override
	public void handlePercept(Percept percept) {
	}

	@Override
	public void handleMessage(Percept message, String sender) {
	}

    @Override
    public Action step() {
        Point nullPoint = new Point(0, 0);
        List<Percept> percepts = getPercepts();
        Identifier newDirection = lastActionParams;

        for (Percept percept : percepts) {
            if (percept.getName().equals("actionID")) {
                Parameter param = percept.getParameters().get(0);
                if (param instanceof Numeral) {
                    int id = ((Numeral) param).getValue().intValue();
                    if (id > lastID) {
                        lastID = id;

                        activeTasks = buildStructureForTasks(getTasks(percepts));
                        activeNorms = buildStructureForNorms(getNorms(percepts)); // to Do *****************
                        // activeRoles = buildStructureForRoles(getRoles(percepts)); // to Do *****************
                        
                        // What task should I work on ? Central decision ? // to Do ****************
                        // What role do I need for it ? Central decision ? // to Do *****************
                        
                        logStep(percepts);                        

                        if (!loaded) { // Agent has not got a block yet
/* Agent searches for in task requested blocks and attaches them */                          
                            Point pointDispenser = findDispenser(percepts);

                            if (!pointDispenser.equals(nullPoint)) { // agent has a dispenser in his vision

                                if ((pointDispenser.x == 0 && (pointDispenser.y == 1 || pointDispenser.y == -1))
                                        || (pointDispenser.y == 0
                                                && (pointDispenser.x == 1 || pointDispenser.x == -1))) { // agent is next to a dispenser

                                    if (pointDispenser.x == 1) newDirection = new Identifier("e");
                                    if (pointDispenser.x == -1) newDirection = new Identifier("w");
                                    if (pointDispenser.y == 1) newDirection = new Identifier("s");
                                    if (pointDispenser.y == -1) newDirection = new Identifier("n");

                                    if (existsBlock(percepts, pointDispenser)) { // a block has already been requested 
                                        loaded = true;
                                        circleSize = 5;
                                        return new Action("attach", newDirection);
                                    } else { // else from a block has already been requested
                                        return new Action("request", newDirection);
                                    }

                                } else { // else from agent is next to a dispenser
                                    String direction = getDirection(pointDispenser);
                                    newDirection = new Identifier(direction);
                                    return new Action("move", newDirection);
                                }

                            } else { // else from agent has a dispenser in his vision
                                // get next dispenser from map (to Do) ************************
                                // walk around in circles to search
                                newDirection = walkCircles(percepts, 1);
                                lastAction = new Identifier("move");
                                lastActionParams = newDirection;
                                return new Action("move", newDirection);
                            }

                        } else { // else from agent has not got a block yet
/* Agent searches for goal zone and submits the blocks there */  
                            List<Percept> listGoalZones = findGoalZones(percepts);
                            if (!(listGoalZones.isEmpty())) { // agent has a goal zone in his vision

                                if (agentInGoalZone(listGoalZones)) { // agent is on a goal zone
                                    if (!blockOK) {
                                        blockOK = true;
                                        return new Action("rotate", new Identifier("ccw"));
                                    } else {
                                        loaded = false;
                                        blockOK = false;
                                        return new Action("submit", new Identifier("task1"));
                                    }
                                } else { // else from agent is on a goal zone
                                    String direction = getDirection(listGoalZones);
                                    newDirection = new Identifier(direction);
                                    return new Action("move", newDirection);
                                }
                            } else { // else from agent has a goal zone in his vision
                                // get GoalZone from map (to Do) ************************
                                // walk around in circles to search
                                newDirection = walkCircles(percepts, 2);
                                lastAction = new Identifier("move");
                                lastActionParams = newDirection;
                                return new Action("move", newDirection);
                            }
                        }
                    }
                }
                break;
            }
        }
        return null;
    }

	/**
	 * Finds the nearest dispenser.
	 * 
	 * @param percepts - list of Percepts from Server
	 * 
	 * @return The point where the nearest dispenser is.    
	 */
	private Point findDispenser(List<Percept> percepts) {
		Point result = new Point(0, 0);

		for (Percept percept : percepts) {
			if (percept.getName().equals("thing")) {

				if (percept.getParameters().get(2).toString().equals("dispenser")) {
					result.x = Integer.parseInt(percept.getParameters().get(0).toString());
					result.y = Integer.parseInt(percept.getParameters().get(1).toString());
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the direction where to find the target.
	 * 
	 * @param pointTarget - the target's point
	 * 
	 * @return The direction where to find the target.
	 */
	private String getDirection(Point pointTarget) {
		String result = " ";
		if (pointTarget.x == 0) {
			if (pointTarget.y < 0)
				result = "n";
			else
				result = "s";
		}

		if (pointTarget.y == 0) {
			if (pointTarget.x < 0)
				result = "w";
			else
				result = "e";
		}

		if (pointTarget.x != 0 && pointTarget.y != 0) {
			if (java.lang.Math.abs(pointTarget.x) > java.lang.Math.abs(pointTarget.y))
				if (pointTarget.x < 0)
					result = "w";
				else
					result = "e";
			else if (pointTarget.y < 0)
				result = "n";
			else
				result = "s";
		}

		return result;
	}
	
	/**
	 * gets the direction where to find the goal zone.
	 * 
	 * @param listGoalZones - list of Percepts from goal zone
	 * 
	 * @return The direction where to find the goal zone.
	 */
	   private String getDirection(List<Percept> listGoalZones) {
	        String result = " ";
	        Point goalZoneCell = new Point(0, 0);
	        Point targetCell = new Point(1000, 1000);
	        
	        if (!(listGoalZones.isEmpty())) {
	            for (Percept percept : listGoalZones) {
	                goalZoneCell.x = Integer.parseInt(percept.getParameters().get(0).toString());
	                goalZoneCell.y = Integer.parseInt(percept.getParameters().get(1).toString());
	                
	                if ((java.lang.Math.abs(goalZoneCell.x) + java.lang.Math.abs(goalZoneCell.y)) 
	                        < (java.lang.Math.abs(targetCell.x) + java.lang.Math.abs(targetCell.y))) {
	                    targetCell = goalZoneCell;
	                }
	            }
	        }   

	        return result = getDirection(targetCell);
	    }
	   
	   /**
		 * checks if the Agent is already in a goal zone
		 * 
		 * @param listGoalZones - list of Percepts from goal zone
		 * 
		 * @return if the agent is already in a goal zone or not
		 */
       private boolean agentInGoalZone(List<Percept> listGoalZones) {
           boolean result = false;
           Point goalZoneCell = new Point(0, 0);
           Point agentCell = new Point(0, 0);
           
           if (!(listGoalZones.isEmpty())) {
               for (Percept percept : listGoalZones) {
                   goalZoneCell.x = Integer.parseInt(percept.getParameters().get(0).toString());
                   goalZoneCell.y = Integer.parseInt(percept.getParameters().get(1).toString());
                   
                   if (goalZoneCell.equals(agentCell)) {
                       result = true;
                       break;
                   }
               }
           }   

           return result;
       }
	   

	/**
	 * Print percepts
	 * 
	 * @param percepts list of Percepts from Server
	 */
	void logStep(List<Percept> percepts) {
		System.out.println();
	      // log Step
		for (Percept percept : percepts) {
			if (percept.getName().equals("step")) {
				Parameter param = percept.getParameters().get(0);
				if (param instanceof Numeral) {
					int perceptStep = ((Numeral) param).getValue().intValue();
					Log.log("Percept Step: " + perceptStep);
					break;
				}
			}
		}
        // log Tasks
        System.out.print("Tasks: "); 
        List<Percept> listTasks = getTasks(percepts);       
        if (!(listTasks.isEmpty())) {                     
            for (Percept percept : listTasks) {
                System.out.print("[" + percept.getParameters().get(0).toString() + "," + percept.getParameters().get(1).toString() + ", " +percept.getParameters().get(0).toString() + "] ");  
            }
        }   
		// log Dispenser
	    Point locD = findDispenser(percepts);
        Log.log("Dispenser: (" + locD.x + "," + locD.y + ")");
        // log GoalZones
        Point goalZoneCell = new Point(0, 0);
        List<Percept> listGoalZones = findGoalZones(percepts);
        System.out.print("goalZones: ");        
        if (!(listGoalZones.isEmpty())) {
            for (Percept percept : listGoalZones) {
                goalZoneCell.x = Integer.parseInt(percept.getParameters().get(0).toString());
                goalZoneCell.y = Integer.parseInt(percept.getParameters().get(1).toString());
                System.out.print("[" + goalZoneCell.x + "," + goalZoneCell.y + "] ");
            }
        }	
        // log last Action       
        System.out.println();
		for (Percept percept : percepts) {
			if (percept.getName().equals("lastAction")) {
				Parameter param = percept.getParameters().get(0);
				Log.log("lastAction: " + param.toString());
				break;
			}
		}
        // log last Action Result		
		for (Percept percept : percepts) {
			if (percept.getName().equals("lastActionResult")) {
				Parameter param = percept.getParameters().get(0);
				Log.log("lastActionResult: " + param.toString());
				break;
			}
		}
	}

	/**
	 * checks if a block exists on a certain cell
	 * 
	 * @param listGoalZones list of Percepts from Percept
	 *  @param pointTarget the target's point
	 *  
	 *  @return if a block exists on a certain cell or not
	 */
	private boolean existsBlock(List<Percept> percepts, Point pointTarget) {
		Point block = new Point(0, 0);
		boolean result = false;

		for (Percept percept : percepts) {
			if (percept.getName().equals("thing")) {

				if (percept.getParameters().get(2).toString().equals("block")) {
					block.x = Integer.parseInt(percept.getParameters().get(0).toString());
					block.y = Integer.parseInt(percept.getParameters().get(1).toString());
					
					if (block.equals(pointTarget)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * finds the nearest goal zone
	 * 
	 * @param percepts list of Percepts from Server
	 * 
	 * @return the Percepts of the nearest goal zone
	 */
	private List<Percept> findGoalZones(List<Percept> percepts) {
		List<Percept> result = new ArrayList<Percept>();
		
		for (Percept percept : percepts) {
			if (percept.getName().equals("goalZone")) {
				result.add(percept);
			}
		}
		return result;
	}
	
	   /**
     * get all tasks
     * 
     * @param percepts list of Percepts from Server
     * 
     * @return all tasks in a list
     */
    private List<Percept> getTasks(List<Percept> percepts) {
        List<Percept> result = new ArrayList<Percept>();
        
        for (Percept percept : percepts) {
            if (percept.getName().equals("task")) {
                result.add(percept);
            }
        }
        return result;
    }
    
    /**
  * Build Datastructure for all tasks
  * 
  * @param percepts list of task-Percepts from Server
  * 
  * @result the datastructure for all (new) tasks
  */
    private ActiveTasks buildStructureForTasks(List<Percept> listTasks) {
        ActiveTasks result = new ActiveTasks();
        ParameterList parameterList;
        Function function;

        if (!(listTasks.isEmpty())) {
            for (Percept percept : listTasks) {
                activeTasks.tasks.add(new Task(percept.getParameters().get(0).toString(),
                        Integer.parseInt(percept.getParameters().get(1).toString()),
                        Integer.parseInt(percept.getParameters().get(2).toString())));

                parameterList = (ParameterList) percept.getParameters().get(3);

                for (int i = 0; i < parameterList.size(); i++) {
                    function = (Function) parameterList.get(i);

                    activeTasks.tasks.get(activeTasks.tasks.size() - 1).blocks
                            .add(new Block(Integer.parseInt(function.getParameters().toArray()[0].toString()),
                                    Integer.parseInt(function.getParameters().toArray()[1].toString()),
                                    function.getParameters().toArray()[2].toString()));
                }
            }
        }
        return result;
    }
    
    /**
  * Get all norms
  * 
  * @param percepts list of Percepts from Server
  * 
  * @result a list with all the norms
  */
 private List<Percept> getNorms(List<Percept> percepts) {
     List<Percept> result = new ArrayList<Percept>();
     
     for (Percept percept : percepts) {
         if (percept.getName().equals("norm")) {
             result.add(percept);
         }
     }
     return result;
 }
 
 /**
* Build Datastructure for all norms
* 
* @param percepts list of norm-Percepts from Server
* 
* @result a datastructure for all the norms
*/
 private ActiveNorms buildStructureForNorms(List<Percept> listNorms) {
     ActiveNorms result = new ActiveNorms();
     ParameterList parameterList;
     Function function;

     if (!(listNorms.isEmpty())) {
         for (Percept percept : listNorms) {
          // to Do ********************************
         }
     }
     return result;
 }
	
	/**
	 * Agent walks in circles 
	 * 
	 * @param percepts list of Percepts from Server
	 * @param circleSize   size of the circle the agent has to walk
	 * @param steps    the circle is getting that much bigger each time the agent is done with a circle
	 * 
	 * @result the next identifier ( direction) where the agent should go to ( to form a circle)
	 */
	private Identifier walkCircles (List<Percept> percepts, int steps) {
		Identifier resultDirection = new Identifier("n");
		directionCounter++;
        if (lastAction.getValue().equals("move") && directionCounter >= circleSize) {
            if (lastActionParams.getValue().equals("n")) resultDirection = new Identifier("e");
            if (lastActionParams.getValue().equals("e")) resultDirection = new Identifier("s");
            if (lastActionParams.getValue().equals("s")) resultDirection = new Identifier("w");
            if (lastActionParams.getValue().equals("w")) {
                resultDirection = new Identifier("n");
                circleSize = circleSize + steps;
            }
            directionCounter = 0;
        }
        return resultDirection;
	}

}

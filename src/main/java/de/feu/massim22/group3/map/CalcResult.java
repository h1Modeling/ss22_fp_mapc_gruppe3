package de.feu.massim22.group3.map;

import eis.iilang.Percept;

/**
 * The Record <code>CalcResult</code> stores the path finding result message to an agent with the agent name.
 *
 * @param agent the name of the agent
 * @param percepts the Percept in which the path finding result is stored
 * 
 * @author Melinda Betz
 * @author Heinz Stadler (documentation)
 */
public record CalcResult(String agent, Percept percepts) {
}

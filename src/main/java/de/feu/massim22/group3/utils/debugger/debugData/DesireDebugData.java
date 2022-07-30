package de.feu.massim22.group3.utils.debugger.debugData;

import de.feu.massim22.group3.agents.Desires.BDesires.BooleanInfo;

/**
 * The Record <code>DesireDebugData</code> provides a data structure to store information about a desire of an agent.
 * 
 * @param name the name of the desire
 * @param isExecutable information if the desire is executable and if not the reason why
 *
 * @author Heinz Stadler
 */
public record DesireDebugData(String name, BooleanInfo isExecutable) {
}

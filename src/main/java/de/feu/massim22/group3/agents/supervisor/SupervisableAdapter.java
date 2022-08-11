package de.feu.massim22.group3.agents.supervisor;

import eis.iilang.Percept;

/** 
 * The Class <code>SupervisableAdapter</code> creates an Adapter for the Interface <code>Supervisable</code>.
 * The Adapter doesn't contain any logic and is mainly made for use in Test classes. 
 *
 * @author Heinz Stadler
 */
public class SupervisableAdapter implements Supervisable {

    @Override
    public void forwardMessage(Percept message, String receiver, String sender) {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void initSupervisorStep() {
    }
}

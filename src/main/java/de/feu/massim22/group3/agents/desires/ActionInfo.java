package de.feu.massim22.group3.agents.desires;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

import java.awt.Point;

/**
 * The Record <code>ActionInfo</code> provides a data structure to store an Action combined with additional information about
 * the reasoning process.
 * In Addition it provides static methods to generate ActionInfo instances for the Actions accepted by the massim 2022 simulation.
 * 
 * @author Heinz Stadler
 * @author Melinda Betz (minor contribution)
 */
public record ActionInfo(Action value, String info) {

    /**
     * Creates an ActionInfo instance for the massim Action move performing a single position change.
     * 
     * @param dir the direction of the move
     * @param info additional information about the reasoning which provided the action
     * @return the move ActionInfo
     */
    public static ActionInfo MOVE(String dir, String info) {
        Action a = new Action(Actions.MOVE, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action rotate with parameter clock wise.
     * 
     * @param info additional information about the reasoning which provided the action
     * @return the clock wise rotation ActionInfo
     */
    public static ActionInfo ROTATE_CW(String info) {
        Action a = new Action(Actions.ROTATE, new Identifier("cw"));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action rotate with parameter counter clock wise.
     * 
     * @param info additional information about the reasoning which provided the action
     * @return the counter clock wise rotation ActionInfo
     */
    public static ActionInfo ROTATE_CCW(String info) {
        Action a = new Action(Actions.ROTATE, new Identifier("ccw"));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action clear.
     * 
     * @param p the point which should be cleared
     * @param info additional information about the reasoning which provided the action
     * @return the clear ActionInfo
     */
    public static ActionInfo CLEAR(Point p, String info) {
        Action a = new Action(Actions.CLEAR, new Numeral(p.x), new Numeral(p.y));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action request.
     * 
     * @param dir the direction from which to request a block
     * @param info additional information about the reasoning which provided the action
     * @return the request ActionInfo
     */
    public static ActionInfo REQUEST(String dir, String info) {
        Action a = new Action(Actions.REQUEST, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action attach.
     * 
     * @param dir the direction from which a thing should be attached
     * @param info additional information about the reasoning which provided the action
     * @return the attach ActionInfo
     */
    public static ActionInfo ATTACH(String dir, String info) {
        Action a = new Action(Actions.ATTACH, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action detach.
     * 
     * @param dir the direction from which a thing should be detached
     * @param info additional information about the reasoning which provided the action
     * @return the detach ActionInfo
     */
    public static ActionInfo DETACH(String dir, String info) {
        Action a = new Action(Actions.DETACH, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action adopt.
     * 
     * @param role the name of the role which should be adopted
     * @param info additional information about the reasoning which provided the action
     * @return the adopt ActionInfo
     */
    public static ActionInfo ADOPT(String role, String info) {
        Action a = new Action(Actions.ADOPT, new Identifier(role));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action submit.
     * 
     * @param task the name of the task which should be submitted
     * @param info additional information about the reasoning which provided the action (V2: Number of blocks for task)
     * @return the submit ActionInfo
     */
    public static ActionInfo SUBMIT(String task, String info) {
        Action a = new Action(Actions.SUBMIT, new Identifier(task), new Identifier(info));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action skip.
     * 
     * @param info additional information about the reasoning which provided the action
     * @return the skip ActionInfo
     */
    public static ActionInfo SKIP(String info) {
        Action a = new Action(Actions.SKIP);
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action connect.
     * 
     * @param agent the name of the agent to which a block should be connected
     * @param t the block which should be connected
     * @param info additional information about the reasoning which provided the action
     * @return the connect ActionInfo
     */
    public static ActionInfo CONNECT(String agent, Thing t, String info) {
        Action a = new Action(Actions.CONNECT, new Identifier(agent), new Numeral(t.x), new Numeral(t.y));
        return new ActionInfo(a, info);
    }

    /**
     * Creates an ActionInfo instance for the massim Action connect.
     * 
     * @param agent the name of the agent to which a block should be connected
     * @param p the position relative to the agent on which a block should be connected
     * @param info additional information about the reasoning which provided the action
     * @return the connect ActionInfo
     */
    public static ActionInfo CONNECT(String agent, Point p, String info) {
        Action a = new Action(Actions.CONNECT, new Identifier(agent), new Numeral(p.x), new Numeral(p.y));
        return new ActionInfo(a, info);
    }
    
    /**
     * Creates an ActionInfo instance for the massim Action move performing a double position change.
     * 
     * @param dir1 the first direction of the move
     * @param dir2 the second direction of the move
     * @param info additional information about the reasoning which provided the action
     * @return the move ActionInfo
     */ 
    public static ActionInfo MOVE(String dir1, String dir2, String info) {
        Action a = new Action(Actions.MOVE, new Identifier(dir1), new Identifier(dir2));
        return new ActionInfo(a, info);
    }
   
    /**
     * Creates an ActionInfo instance for the massim Action disconnect.
     * 
     * @param agent the name of the agent to which a block should be connected
     * @param p1 the position relative to the agent on which a block should be disconnected
     * @param p2 the position relative to the agent on which a second block should be disconnected
     * @param info additional information about the reasoning which provided the action
     * @return the disconnect ActionInfo
     */
    public static ActionInfo DISCONNECT(Point p1, Point p2, String info) {
        Action a = new Action(Actions.DISCONNECT, new Numeral(p1.x), new Numeral(p1.y), new Numeral(p2.x), new Numeral(p2.y));
        return new ActionInfo(a, info);
    }
}

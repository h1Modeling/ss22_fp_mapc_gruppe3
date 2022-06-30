package de.feu.massim22.group3.agents.Desires.BDesires;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

import java.awt.Point;

public record ActionInfo(Action value, String info) {

    static ActionInfo MOVE(String dir, String info) {
        Action a = new Action(Actions.MOVE, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    static ActionInfo ROTATE_CW(String info) {
        Action a = new Action(Actions.ROTATE, new Identifier("cw"));
        return new ActionInfo(a, info);
    }

    static ActionInfo ROTATE_CCW(String info) {
        Action a = new Action(Actions.ROTATE, new Identifier("ccw"));
        return new ActionInfo(a, info);
    }

    static ActionInfo CLEAR(Point p, String info) {
        Action a = new Action(Actions.CLEAR, new Numeral(p.x), new Numeral(p.y));
        return new ActionInfo(a, info);
    }

    static ActionInfo REQUEST(String dir, String info) {
        Action a = new Action(Actions.REQUEST, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    static ActionInfo ATTACH(String dir, String info) {
        Action a = new Action(Actions.ATTACH, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    static ActionInfo DETACH(String dir, String info) {
        Action a = new Action(Actions.DETACH, new Identifier(dir));
        return new ActionInfo(a, info);
    }

    static ActionInfo ADOPT(String role, String info) {
        Action a = new Action(Actions.ADOPT, new Identifier(role));
        return new ActionInfo(a, info);
    }

    static ActionInfo SUBMIT(String task, String info) {
        Action a = new Action(Actions.SUBMIT, new Identifier(task));
        return new ActionInfo(a, info);
    }

    static ActionInfo SKIP(String info) {
        Action a = new Action(Actions.SKIP);
        return new ActionInfo(a, info);
    }

    static ActionInfo CONNECT(String agent, Thing t, String info) {
        Action a = new Action(Actions.CONNECT, new Identifier(agent), new Numeral(t.x), new Numeral(t.y));
        return new ActionInfo(a, info);
    }

    static ActionInfo CONNECT(String agent, Point p, String info) {
        Action a = new Action(Actions.CONNECT, new Identifier(agent), new Numeral(p.x), new Numeral(p.y));
        return new ActionInfo(a, info);
    }
}

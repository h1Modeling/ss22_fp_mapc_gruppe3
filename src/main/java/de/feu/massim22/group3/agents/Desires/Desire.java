package de.feu.massim22.group3.agents.Desires;

import java.util.LinkedList;
import java.util.List;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;

public abstract class Desire {

    protected Desires desireType;
    protected List<SubDesire> subDesires = new LinkedList<SubDesire>();
    protected BdiAgent agent;

    Desire(BdiAgent agent) {
        this.agent = agent;
        setType();
        defineSubDesires();
    }

    @Override
    public String toString() {
        return desireType.name();
    }

    abstract protected void defineSubDesires();

    public List<SubDesire> getSubDesires() {
        return subDesires;
    }

    abstract protected void setType();
}

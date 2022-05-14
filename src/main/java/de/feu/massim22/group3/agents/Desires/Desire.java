package de.feu.massim22.group3.agents.Desires;

import java.util.LinkedList;
import java.util.List;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;

public abstract class Desire {

    protected Desires desireType;
    protected List<SubDesire> subDesires = new LinkedList<SubDesire>();

    Desire() {
        setType();
        defineSubDesires();
    }

    @Override
    public String toString() {
        return desireType.name();
    }

    abstract void defineSubDesires();

    public List<SubDesire> getSubDesires() {
        return subDesires;
    }

    abstract void setType();
}

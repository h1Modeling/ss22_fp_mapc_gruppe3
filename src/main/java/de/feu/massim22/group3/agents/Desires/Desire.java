package de.feu.massim22.group3.agents.Desires;

import java.util.LinkedList;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;

public abstract class Desire {

    protected Desires desireType;
    protected LinkedList<SubDesire> subDesires = new LinkedList<SubDesire>();

    Desire(Desires desireType) {
        this.desireType = desireType;
        defineSubDesires();
    }

    @Override
    public String toString() {
        return desireType.name();
    }

    abstract void defineSubDesires();

    public LinkedList<SubDesire> getSubDesires() {
        return subDesires;
    }
}

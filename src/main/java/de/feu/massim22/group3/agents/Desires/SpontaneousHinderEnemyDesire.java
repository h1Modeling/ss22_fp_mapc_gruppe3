package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;
import de.feu.massim22.group3.agents.BdiAgent;

public class SpontaneousHinderEnemyDesire extends Desire {

    public SpontaneousHinderEnemyDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    protected void defineSubDesires() {
        subDesires.add(SubDesires.SPONTANEOUS_HINDER_ENEMY.getSubDesireObj(agent));
    }

    @Override
    protected void setType() {
        this.desireType = Desires.SPONTANEOUS_HINDER_ENEMY;
    }
}

package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class SpontaneousHinderEnemyDesire extends Desire {

    public SpontaneousHinderEnemyDesire() {
        super();
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.SPONTANEOUS_HINDER_ENEMY.getSubDesireObj());
    }
    void setType() {
        this.desireType = Desires.SPONTANEOUS_HINDER_ENEMY;
    }
}

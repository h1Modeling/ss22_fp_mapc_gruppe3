package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.BdiAgent;

public class AttackEnemyDesire extends Desire {

    public AttackEnemyDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    void defineSubDesires() {
        // TODO Auto-generated method stub
    }
    @Override
    void setType() {
        this.desireType = Desires.ATTACK_ENEMY;
    }
}

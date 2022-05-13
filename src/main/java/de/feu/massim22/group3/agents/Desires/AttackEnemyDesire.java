package de.feu.massim22.group3.agents.Desires;

public class AttackEnemyDesire extends Desire {

    public AttackEnemyDesire() {
        super();
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

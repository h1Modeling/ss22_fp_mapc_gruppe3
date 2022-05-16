package de.feu.massim22.group3.agents.Desires.SubDesires;

import de.feu.massim22.group3.agents.BdiAgent;

public enum SubDesires {
    DIG_FREE,
    DODGE_CLEAR,
    DODGE_OTHER_AGENT,
    SPONTANEOUS_HINDER_ENEMY,
    GO_TO_UNKNOWN_AREA,
    GET_ROLE,
    CHOOSE_TASK,
    GET_BLOCKS,
    ASSEMBLE_TASK,
    GO_TO_GOAL_AREA,
    SUBMIT_TASK,
    REACT_TO_NORM,;

    public SubDesire getSubDesireObj(BdiAgent agent) {
        switch (this) {
        case DIG_FREE:
            return new DigFreeSubDesire(agent);
        case DODGE_CLEAR:
            return new DodgeClearSubDesire(agent);
        case DODGE_OTHER_AGENT:
            return new DodgeOtherAgentSubDesire(agent);
        case SPONTANEOUS_HINDER_ENEMY:
            return new SpontaneousHinderEnemySubDesire(agent);
        case GO_TO_UNKNOWN_AREA:
            return new GoToUnkownAreaSubDesire(agent);
        case GET_ROLE:
            return new GetRoleSubDesire(agent);
        case CHOOSE_TASK:
            return new ChooseTaskSubDesire(agent);
        case GET_BLOCKS:
            return new GetBlocksSubDesire(agent);
        case ASSEMBLE_TASK:
            return new AssembleTaskSubDesire(agent);
        case GO_TO_GOAL_AREA:
            return new GoToGoalAreaSubDesire(agent);
        case SUBMIT_TASK:
            return new SubmitTaskSubDesire(agent);
        case REACT_TO_NORM:
            return new ReactToNormSubDesire(agent);
        default: {
            throw new IllegalArgumentException("Unknown type in during SubDesire instantiation.");
        }
        }
    }
}

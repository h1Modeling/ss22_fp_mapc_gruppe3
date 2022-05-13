package de.feu.massim22.group3.agents.Desires.SubDesires;

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

    public SubDesire getSubDesireObj() {
        switch (this) {
        case DIG_FREE:
            return new DigFreeSubDesire();
        case DODGE_CLEAR:
            return new DodgeClearSubDesire();
        case DODGE_OTHER_AGENT:
            return new DodgeOtherAgentSubDesire();
        case SPONTANEOUS_HINDER_ENEMY:
            return new SpontaneousHinderEnemySubDesire();
        case GO_TO_UNKNOWN_AREA:
            return new GoToUnkownAreaSubDesire();
        case GET_ROLE:
            return new GetRoleSubDesire();
        case CHOOSE_TASK:
            return new ChooseTaskSubDesire();
        case GET_BLOCKS:
            return new GetBlocksSubDesire();
        case ASSEMBLE_TASK:
            return new AssembleTaskSubDesire();
        case GO_TO_GOAL_AREA:
            return new GoToGoalAreaSubDesire();
        case SUBMIT_TASK:
            return new SubmitTaskSubDesire();
        case REACT_TO_NORM:
            return new ReactToNormSubDesire();
        default: {
            throw new IllegalArgumentException("Unknown type in during SubDesire instantiation.");
        }
        }
    }
}

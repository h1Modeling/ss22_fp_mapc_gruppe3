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
            return new DigFreeSubDesire(DIG_FREE);
        case DODGE_CLEAR:
            return new DodgeClearSubDesire(DODGE_CLEAR);
        case DODGE_OTHER_AGENT:
            return new DodgeOtherAgentSubDesire(DODGE_OTHER_AGENT);
        case SPONTANEOUS_HINDER_ENEMY:
            return new SpontaneousHinderEnemySubDesire(SPONTANEOUS_HINDER_ENEMY);
        case GO_TO_UNKNOWN_AREA:
            return new GoToUnkownAreaSubDesire(GO_TO_UNKNOWN_AREA);
        case GET_ROLE:
            return new GetRoleSubDesire(GET_ROLE);
        case CHOOSE_TASK:
            return new ChooseTaskSubDesire(CHOOSE_TASK);
        case GET_BLOCKS:
            return new GetBlocksSubDesire(GET_BLOCKS);
        case ASSEMBLE_TASK:
            return new AssembleTaskSubDesire(ASSEMBLE_TASK);
        case GO_TO_GOAL_AREA:
            return new GoToGoalAreaSubDesire(GO_TO_GOAL_AREA);
        case SUBMIT_TASK:
            return new SubmitTaskSubDesire(SUBMIT_TASK);
        case REACT_TO_NORM:
            return new ReactToNormSubDesire(REACT_TO_NORM);
        default: {
            throw new IllegalArgumentException("Unknown type in during SubDesire instantiation.");
        }
        }
    }
}

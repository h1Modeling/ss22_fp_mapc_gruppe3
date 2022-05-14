package de.feu.massim22.group3.agents.Desires;

public enum Desires {
    // Team Desires
    PROCESS_TASK,
    HELP_TASK_AGENT,
    EXPLORE_MAP,
    DETERMINE_MAP_SIZE,
    ATTACK_ENEMY,
    GOAL_ZONE_GUARD,
    // Agent Desires
    DIG_FREE,
    DODGE_CLEAR,
    DODGE_OTHER_AGENT,
    REACT_TO_NORM,
    SPONTANEOUS_HINDER_ENEMY,
    GO_TO_UNKNOWN_AREA,;

    public Desire getDesireObj() {
        switch (this) {
        case PROCESS_TASK:
            return new ProcessTaskDesire();
        case ATTACK_ENEMY:
            return new AttackEnemyDesire();
        case DETERMINE_MAP_SIZE:
            return new DetermineMapSizeDesire();
        case EXPLORE_MAP:
            return new ExploreMapDesire();
        case GOAL_ZONE_GUARD:
            return new GoalZoneGuardDesire();
        case HELP_TASK_AGENT:
            return new HelpTaskAgentDesire();
        case DIG_FREE:
            return new DigFreeDesire();
        case DODGE_CLEAR:
            return new DogeClearDesire();
        case DODGE_OTHER_AGENT:
            return new DogeOtherAgentDesire();
        case REACT_TO_NORM:
            return new ReactToNormDesire();
        case SPONTANEOUS_HINDER_ENEMY:
            return new SpontaneousHinderEnemyDesire();
        case GO_TO_UNKNOWN_AREA:
            return new GoToUnknownAreaDesire();
        default: {
            throw new IllegalArgumentException("Unknown type during Desire instantiation.");
        }
        }
    }
}
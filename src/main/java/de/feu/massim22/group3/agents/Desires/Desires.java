package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.BdiAgent;

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

    public Desire getDesireObj(BdiAgent agent) {
        switch (this) {
        case PROCESS_TASK:
            return new ProcessTaskDesire(agent);
        case ATTACK_ENEMY:
            return new AttackEnemyDesire(agent);
        case DETERMINE_MAP_SIZE:
            return new DetermineMapSizeDesire(agent);
        case EXPLORE_MAP:
            return new ExploreMapDesire(agent);
        case GOAL_ZONE_GUARD:
            return new GoalZoneGuardDesire(agent);
        case HELP_TASK_AGENT:
            return new HelpTaskAgentDesire(agent);
        case DIG_FREE:
            return new DigFreeDesire(agent);
        case DODGE_CLEAR:
            return new DogeClearDesire(agent);
        case DODGE_OTHER_AGENT:
            return new DogeOtherAgentDesire(agent);
        case REACT_TO_NORM:
            return new ReactToNormDesire(agent);
        case SPONTANEOUS_HINDER_ENEMY:
            return new SpontaneousHinderEnemyDesire(agent);
        case GO_TO_UNKNOWN_AREA:
            return new GoToUnknownAreaDesire(agent);
        default: {
            throw new IllegalArgumentException("Unknown type during Desire instantiation.");
        }
        }
    }
}
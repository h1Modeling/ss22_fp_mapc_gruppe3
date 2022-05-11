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
		switch(this) {
		case PROCESS_TASK:
			return new ProcessTaskDesire(PROCESS_TASK);
		case ATTACK_ENEMY:
			return new AttackEnemyDesire(ATTACK_ENEMY);
		case DETERMINE_MAP_SIZE:
			return new DetermineMapSizeDesire(DETERMINE_MAP_SIZE);
		case EXPLORE_MAP:
			return new ExploreMapDesire(EXPLORE_MAP);
		case GOAL_ZONE_GUARD:
			return new GoalZoneGuardDesire(GOAL_ZONE_GUARD);
		case HELP_TASK_AGENT:
			return new HelpTaskAgentDesire(HELP_TASK_AGENT);
		case DIG_FREE:
			return new DigFreeDesire(DIG_FREE);
		case DODGE_CLEAR:
			return new DogeClearDesire(DODGE_CLEAR);
		case DODGE_OTHER_AGENT:
			return new DogeOtherAgentDesire(DODGE_OTHER_AGENT);
		case REACT_TO_NORM:
			return new ReactToNormDesire(REACT_TO_NORM);
		case SPONTANEOUS_HINDER_ENEMY:
			return new SpontaneousHinderEnemyDesire(SPONTANEOUS_HINDER_ENEMY);
		case GO_TO_UNKNOWN_AREA:
			return new GoToUnknownAreaDesire(GO_TO_UNKNOWN_AREA);
		default:{
			throw new IllegalArgumentException("Unknown type during Desire instantiation.");
		}
		}
	}
}
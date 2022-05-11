package de.feu.massim22.group3.agents.Desires.SubDesires;

import java.util.LinkedList;

import de.feu.massim22.group3.agents.Desires.Desires;
import eis.iilang.Action;

public abstract class SubDesire {

	protected SubDesires subDesireType;
	
	SubDesire(SubDesires subDesireType) {
		this.subDesireType = subDesireType;
	}
	
	@Override
	public String toString() {
		return subDesireType.name();
	}
	
	// Whole logic of the subgoal is implemented in this method (in each subclass)
	public abstract Action getNextAction();
	
	// Check preconditions, if false next subgoal in hierarchy is checked
	public abstract boolean isExecutable();
	
	// Check post conditions, if true next subgoal in hierarchy is checked
	public abstract boolean isDone();
	
}
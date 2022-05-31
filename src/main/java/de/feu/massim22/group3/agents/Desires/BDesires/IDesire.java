package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.IIntention;

public interface IDesire extends IIntention {
    boolean isFullfilled();
    boolean isExecutable();
}

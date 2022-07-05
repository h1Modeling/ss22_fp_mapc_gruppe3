package de.feu.massim22.group3.map;

import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.utils.debugger.DebugStepListener;

import java.awt.Point;

public interface INavi {
    void setMailService(MailService mailService);
    void registerAgent(String name);
    CellType[][] getBlankCellArray(int vision); 
    void setDebugStepListener(DebugStepListener listener, boolean manualMode);
    void dispose();
    boolean isBlockAttached(String supervisor, Point p);
    Point getPosition(String name, String supervisor);
    void resetAgent(String name);
}

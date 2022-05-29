package de.feu.massim22.group3.map;

import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.utils.debugger.DebugStepListener;

public interface INavi {
    void setMailService(MailService mailService);
    void registerAgent(String name);
    CellType[][] getBlankCellArray(int vision); 
    void setDebugStepListener(DebugStepListener listener);
    void dispose();
}

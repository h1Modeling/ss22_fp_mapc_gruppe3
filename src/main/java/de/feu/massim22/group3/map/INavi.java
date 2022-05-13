package de.feu.massim22.group3.map;

import de.feu.massim22.group3.MailService;

public interface INavi {
    void setMailService(MailService mailService);
    void registerAgent(String name);
    CellType[][] getBlankCellArray(int vision);
}

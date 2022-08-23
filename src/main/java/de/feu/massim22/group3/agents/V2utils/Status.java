package de.feu.massim22.group3.agents.V2utils;

/**
 * The enum <code>Status</code> is a important part of the communication of BdiAgentV2. 
 * All these states are being checked and compared for Multi Block Tasks. 
 * 
 * @author Melinda Betz
 */
public enum Status {
    No2("no2"),
    Open("ope"),
    New("new"),
    GoGoalZone("ggz"),
    InGoalZone("igz"),
    GoMaster("gom"),
    GoTarget("got"),
    OnTarget("ont"),
    Arranging("arr"),
    ReadyToConnect("rtc"),
    Connected("rtc"),
    ReadyToDetach("rtd"),
    Detached("det"),
    ReadyToSubmit("rts"),
    Submitted("sub");

    private String type;

    private Status(String type) {
        this.type = type;
    }
}

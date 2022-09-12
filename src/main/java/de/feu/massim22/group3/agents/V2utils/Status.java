package de.feu.massim22.group3.agents.V2utils;

/**
 * The enum <code>Status</code> is a important part of the communication of BdiAgentV2.
 * The states belong to AgentCooperations. 
 * These states are being checked and compared for Multi Block Tasks and Explore Map Size. 
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
    Submitted("sub"),
    Explore("exp"),
    Wait("wai"),
    Finished("wai");

    private Status(String type) {
    }
}

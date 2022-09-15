package de.feu.massim22.group3.agents.V2utils;

/**
 * The enum <code>Status</code> is a important part of the communication of BdiAgentV2.
 * The states belong to AgentCooperations. 
 * These states are being checked and compared for Multi Block Tasks and Explore Map Size. 
 * 
 * @author Melinda Betz
 */
public enum Status {
    /** No helper2 in this cooperation.*/
    No2("no2"),
    /** New added helper.*/
    New("new"),
    /** Helper has arrived in GoalZone.*/
    InGoalZone("igz"),
    /** Helper is on his way to the master.*/
    GoMaster("gom"),
    /** Helper is on his way to the target position.*/
    GoTarget("got"),
    /** Helper has arrived at the target position.*/
    OnTarget("ont"),
    /** Helper is arranging his block.*/
    Arranging("arr"),
    /** Helper is ready to connect.*/
    ReadyToConnect("rtc"),
    /** Helper has successfully connected.*/
    Connected("rtc"),
    /** Helper is ready to detach.*/
    ReadyToDetach("rtd"),
    /** Helper has successfully detached.*/
    Detached("det"),
    /** Master is ready to submit.*/
    ReadyToSubmit("rts"),
    /** Master has successfully submitted.*/
    Submitted("sub"),
    /** Explore map size: Master is on his way around the map.*/
    Explore("exp"),
    /** Explore map size: Helper is waiting for the master.*/
    Wait("wai");

    private Status(String type) {
    }
}

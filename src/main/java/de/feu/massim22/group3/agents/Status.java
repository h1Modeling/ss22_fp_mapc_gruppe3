package de.feu.massim22.group3.agents;

public enum Status {
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

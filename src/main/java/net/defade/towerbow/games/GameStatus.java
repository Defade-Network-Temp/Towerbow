package net.defade.towerbow.games;

public enum GameStatus {
    CREATING(true, false),
    WAITING_PLAYERS_FOR_DEMO(true, false),
    WAITING_PLAYER_OPTIONAL(true, false),
    STARTING(false, true),
    PHASE_1(true, true),
    PHASE_2(false, true),
    PHASE_3(false, true),
    ;
    private final boolean acceptPlayers;
    private final boolean playing;
    GameStatus(boolean acceptPlayers, boolean playing) {
        this.acceptPlayers = acceptPlayers;
        this.playing = playing;
    }

    public boolean canAcceptPlayers() {
        return acceptPlayers;
    }
    public boolean isPlaying(){return playing;}
}

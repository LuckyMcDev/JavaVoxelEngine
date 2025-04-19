package net.fynn.javavoxelengine.challenge;

/**
 * Definitions for each challenge mode: name, apple target, and time limit.
 */
public enum ChallengeType {
    /**
     * Die verschiedenen Challenge type, einfach, medium und schwer und ein Debug modus
     */
    EASY   ("Easy",   10, 60000),
    MEDIUM ("Medium", 20, 60000),
    HARD   ("Hard",   30, 60000),
    DEBUG  ("DEBUG", 999999999,999999999);

    private final String displayName;
    private final int    targetApples;
    private final long   timeLimitMs;

    ChallengeType(String displayName, int targetApples, long timeLimitMs) {
        this.displayName   = displayName;
        this.targetApples  = targetApples;
        this.timeLimitMs   = timeLimitMs;
    }

    /** Menschlich Lesbar (für das Gui) */
    public String getDisplayName() {
        return displayName;
    }

    /** Wie viele Äpfel aufgesammelt werden müssen */
    public int getTargetApples() {
        return targetApples;
    }

    /** Die Zeit in Millisekunden */
    public long getTimeLimitMs() {
        return timeLimitMs;
    }
}

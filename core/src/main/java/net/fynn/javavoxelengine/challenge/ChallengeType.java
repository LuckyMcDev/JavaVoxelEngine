package net.fynn.javavoxelengine.challenge;

/**
 * Definitions for each challenge mode: name, apple target, and time limit.
 */
public enum ChallengeType {
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

    /** Human‑readable name (for UI). */
    public String getDisplayName() {
        return displayName;
    }

    /** How many apples the player must collect. */
    public int getTargetApples() {
        return targetApples;
    }

    /** Duration of the challenge in milliseconds. */
    public long getTimeLimitMs() {
        return timeLimitMs;
    }
}

package net.fynn.javavoxelengine.challenge;

/**
 * Definitions for each challenge mode: name, apple target, and time limit.
 */
public enum ChallengeType {
    EASY   ("Easy",   30, 60000),
    MEDIUM ("Medium", 60, 60000),
    HARD   ("Hard",   80, 60000);

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

package net.fynn.javavoxelengine.challenge;

import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import net.fynn.javavoxelengine.VoxelEngine;

public class ChallengeManager {
    private ChallengeType activeType;
    private long startTime;
    private int collected;

    /** Start a new run of the given type. */
    public void start(ChallengeType type) {
        this.activeType = type;
        this.startTime  = TimeUtils.millis();
        this.collected  = 0;
    }

    /** Call every frame to check timeouts, etc. */
    public void update() {
        if (activeType == null) return;
        long elapsed = TimeUtils.timeSinceMillis(startTime);
        System.out.println(elapsed);
        //handle game over:
        if (elapsed >= activeType.getTimeLimitMs()) {

            System.out.println("GAME OVER");
            end();
        }
    }

    /** Should be called when an apple is successfully clicked. */
    public void collectApple() {
        if (activeType == null) return;
        collected++;
        if (collected >= activeType.getTargetApples()) {
            // challenge complete — handle win
            end();
        }
    }

    public int  getCollected() {
        return collected;
    }

    public int  getTarget() {
        return activeType == null ? 0 : activeType.getTargetApples();
    }

    public float getTimeRemainingSecs() {
        if (activeType == null) return 0f;
        long elapsed = TimeUtils.timeSinceMillis(startTime);
        return Math.max(0f, (activeType.getTimeLimitMs() - elapsed) / 1000f);
    }

    public String getModeName() {
        return activeType == null ? "" : activeType.getDisplayName();
    }

    public boolean isActive() {
        return activeType != null;
    }

    /** Stops the current challenge. */
    public void end() {
        this.activeType = null;
    }
}

package net.fynn.javavoxelengine.challenge;

import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import net.fynn.javavoxelengine.VoxelEngine;
import net.fynn.javavoxelengine.gui.ThisImGui;

public class ChallengeManager {
    private ChallengeType activeType;
    private long startTime;
    private int collected;

    private boolean playerWon = false;
    private boolean playerLost = false;

    /** Start a new run of the given type. */
    public void start(ChallengeType type) {
        this.activeType = type;
        this.startTime  = TimeUtils.millis();
        this.collected  = 0;
        resetWinLossFlags();
    }

    public void reset() {
        activeType = null;
        collected = 0;
        startTime = 0;
        resetWinLossFlags();
    }

    /** Call every frame to check timeouts, etc. */
    public void update() {
        if (activeType == null) return;
        long elapsed = TimeUtils.timeSinceMillis(startTime);
        if (elapsed >= activeType.getTimeLimitMs()) {
            System.out.println("GAME OVER");
            playerLost = true;
            end();
        }
    }

    /** Should be called when an apple is successfully clicked. */
    public void addOneAppleAndCheckComplete() {
        if (activeType == null) return;
        collected++;
        if (collected >= activeType.getTargetApples()) {
            System.out.println("YOU WIN!");
            playerWon = true;
            end();
        }
    }

    public int  getCollected() {
        return collected;
    }

    public int  getTarget() {
        return activeType == null ? 0 : activeType.getTargetApples();
    }

    /** Ein einfacher reset für das win und loss, wird aufgerufen bei start einer challenge*/
    private void resetWinLossFlags() {
        playerWon = false;
        playerLost = false;
    }

    public float getTimeRemainingSecs() {
        if (activeType == null) return 0f;
        long elapsed = TimeUtils.timeSinceMillis(startTime);
        return Math.max(0f, (activeType.getTimeLimitMs() - elapsed) / 1000f);
    }

    /** Gibt den playerWon status zurück */
    public boolean hasPlayerWon() {
        return playerWon;
    }

    /** Gibt den playerLost status zurück */
    public boolean hasPlayerLost() {
        return playerLost;
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

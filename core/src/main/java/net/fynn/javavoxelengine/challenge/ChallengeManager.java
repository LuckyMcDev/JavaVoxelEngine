package net.fynn.javavoxelengine.challenge;

import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;
import net.fynn.javavoxelengine.VoxelEngine;
import net.fynn.javavoxelengine.gui.ThisImGui;

public class ChallengeManager {

    /** Der jetzt grade aktive challenge typ */
    public ChallengeType activeType;

    /** Wann die challenge angefangen hat */
    public long startTime;

    /** Wie viele Äpfel schon gesammelt sind */
    public int collected;

    /** eine boolean ob der spieler gewonnen / verloren hat*/
    public boolean playerWon = false;
    public boolean playerLost = false;

    /**
     *  Startet einen neuen Run mit dem gegebenen Typ
     */
    public void start(ChallengeType type) {
        this.activeType = type;
        this.startTime  = TimeUtils.millis();
        this.collected  = 0;
        resetWinLossFlags();
    }


    /**
     * Setzt den Challenge Manager zurück.
     */
    public void reset() {
        activeType = null;
        collected = 0;
        startTime = 0;
        resetWinLossFlags();
    }

    /**
     * Wird jeden frame aufgerufen um Loss zu prüfen
     */
    public void update() {
        if (activeType == null) return;
        long elapsed = TimeUtils.timeSinceMillis(startTime);
        if (elapsed >= activeType.getTimeLimitMs()) {
            System.out.println("GAME OVER");
            playerLost = true;
            end();
        }
    }

    /**
     * Wird aufgerufen, wenn ein Apfel aufgesammelt wird
     */
    public void addOneAppleAndCheckComplete() {
        if (activeType == null) return;
        collected++;
        if (collected >= activeType.getTargetApples()) {
            System.out.println("YOU WIN!");
            playerWon = true;
            end();
        }
    }

    /**
     * Gibt einfach nur den jetzigen apfel wert zurück
     *
     * @return - den jetzigen stand an aufgesammelten äpfeln
     */
    public int  getCollected() {
        return collected;
    }

    /**
     * Gibt die gebrauchten Äpfel für die jetzt aktive challenge zurück
     *
     * @return - die gebrauchten Äpfel
     */
    public int  getTarget() {
        return activeType == null ? 0 : activeType.getTargetApples();
    }

    /** Ein einfacher reset für das win und loss, wird aufgerufen bei start einer challenge*/
    private void resetWinLossFlags() {
        playerWon = false;
        playerLost = false;
    }

    /**
     * Konvertiert millisekunden in sekunden und gibt die noch verbleibende Zeit zurück
     *
     * @return - noch verbleibende Zeit
     */
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

    /**
     * Gibt den ausgewählten challenge typ namen zurück
     *
     * @return - der name des ausgewählten typs
     */
    public String getModeName() {
        return activeType == null ? "" : activeType.getDisplayName();
    }

    /**
     * Ist eine Challenge aktiv
     *
     * @return - ob eine challenge aktiv ist
     */
    public boolean isActive() {
        return activeType != null;
    }

    /** Beendet die jetzige challenge */
    public void end() {
        this.activeType = null;
    }
}

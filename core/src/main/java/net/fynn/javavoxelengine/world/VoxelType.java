package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.graphics.Color;

/**
 * Enthält die verschiedenen Voxeltypen mit ihren Farben und Sichtbarkeitseigenschaften.
 */
public enum VoxelType {

    /** Luft, unsichtbar. */
    AIR(null),

    /** Stein, grau. */
    STONE(new Color(0.5f, 0.5f, 0.5f, 1f)),

    /** Erde, braun. */
    DIRT(new Color(0.545f, 0.271f, 0.075f, 1f)),

    /** Gras, grün. */
    GRASS(new Color(0.133f, 0.545f, 0.133f, 1f)),

    /** Holz, braun. */
    WOOD(new Color(0.361f,0.329f,0.239f,1f)),

    /** Blätter, Hellgrün. */
    LEAVES_LIGHT(new Color(0.514f,0.851f,0.447f,1f)),

    /** Blätter, Dunkelgrün. */
    LEAVES_DARK(new Color(0.298f,0.602f,0.259f,1f));

    /** Die Farbe des Voxeltyps. */
    public final Color color;

    VoxelType(Color color) {
        this.color = color;
    }

    /**
     * Gibt an, ob der Voxeltyp sichtbar ist.
     *
     * @return True, wenn der Voxeltyp sichtbar ist, sonst false.
     */
    public boolean isVisible() {
        return this != AIR;
    }
}

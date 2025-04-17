package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.graphics.Color;

public enum VoxelType {
    AIR(null),  // AIR is invisible
    STONE(new Color(0.5f, 0.5f, 0.5f, 1f)),
    DIRT(new Color(0.545f, 0.271f, 0.075f, 1f)),
    GRASS(new Color(0.133f, 0.545f, 0.133f, 1f)),
    WOOD(new Color(0.1f,0.4f,0.1f,1f)),
    LEAVES(new Color(1f,1f,1f,1f));

    public final Color color;

    VoxelType(Color color) {
        this.color = color;
    }

    public boolean isVisible() {
        return this != AIR;
    }
}

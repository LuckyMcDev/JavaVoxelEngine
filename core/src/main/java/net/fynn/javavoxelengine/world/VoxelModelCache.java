package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.graphics.g3d.Model;

import java.util.EnumMap;

public class VoxelModelCache {
    private static final EnumMap<VoxelType, Model> modelCache = new EnumMap<>(VoxelType.class);

    public static void initialize(float width, float height, float depth) {
        for (VoxelType type : VoxelType.values()) {
            if (type.isVisible()) {
                modelCache.put(type, Voxel.createCube(width, height, depth, type));
            }
        }
    }

    public static Model getModel(VoxelType type) {
        return modelCache.get(type);
    }

    public static void dispose() {
        for (Model model : modelCache.values()) {
            if (model != null) model.dispose();
        }
        modelCache.clear();
    }
}

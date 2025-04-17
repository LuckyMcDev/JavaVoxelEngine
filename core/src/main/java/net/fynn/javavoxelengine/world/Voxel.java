package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class Voxel {
    /**
     * Creates a cube model using the specified voxel type.
     *
     * @param width     the width of the cube.
     * @param height    the height of the cube.
     * @param depth     the depth of the cube.
     * @param voxelType the voxel type to use for color and properties.
     * @return a Model representing a cube of that type, or null if AIR.
     */
    public static Model createCube(float width, float height, float depth, VoxelType voxelType) {
        if (voxelType == null || !voxelType.isVisible()) return null;

        ModelBuilder builder = new ModelBuilder();
        return builder.createBox(width, height, depth,
            new Material(ColorAttribute.createDiffuse(voxelType.color)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }
}

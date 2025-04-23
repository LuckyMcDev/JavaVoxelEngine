package net.fynn.javavoxelengine.voxel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/**
 * Utility-Klasse zur Erstellung von Voxel-Modellen.
 */
public class Voxel {
    /**
     * Erstellt ein Würfelmodell unter Verwendung des angegebenen Voxeltyps.
     *
     * @param width     Die Breite des Würfels.
     * @param height    Die Höhe des Würfels.
     * @param depth     Die Tiefe des Würfels.
     * @param voxelType Der Voxeltyp, der für Farbe und Eigenschaften verwendet wird.
     * @return Ein Modell, das einen Würfel dieses Typs darstellt, oder null, wenn es sich um Luft handelt.
     */
    public static Model createCube(float width, float height, float depth, VoxelType voxelType) {
        if (voxelType == null || !voxelType.isVisible()) return null;

        ModelBuilder builder = new ModelBuilder();
        builder.begin();

        // Slight random tint
        float tint = 0.9f + (float)Math.random() * 0.2f; // random from 0.9 to 1.1
        Material material = new Material(ColorAttribute.createDiffuse(
            voxelType.color.r * tint,
            voxelType.color.g * tint,
            voxelType.color.b * tint,
            1f
        ));

        MeshPartBuilder mpb = builder.part(
            "cube",
            com.badlogic.gdx.graphics.GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorUnpacked,
            material
        );

        mpb.setColor(voxelType.color);

        mpb.box(width, height, depth);

        return builder.end();
    }
}

package net.fynn.javavoxelengine.chunk;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBody;
import net.fynn.javavoxelengine.voxel.VoxelType;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds and caches a single merged Model per chunk, combining all visible faces into one mesh.
 */
public class ChunkMesher {
    private static final Map<String, Model> cache = new HashMap<>();

    public static Model getChunkModel(Chunk chunk) {
        String key = chunk.originX + "_" + chunk.originY + "_" + chunk.originZ;
        return cache.computeIfAbsent(key, k -> buildChunkModel(chunk));
    }

    private static Model buildChunkModel(Chunk chunk) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();

        MeshPartBuilder mpbTB = builder.part(
            "top_bottom",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal   |
                VertexAttributes.Usage.ColorUnpacked,
            new Material(ColorAttribute.createDiffuse(Color.WHITE))
        );
        MeshPartBuilder mpbEW = builder.part(
            "east_west",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal   |
                VertexAttributes.Usage.ColorUnpacked,
            new Material(ColorAttribute.createDiffuse(Color.WHITE))
        );
        MeshPartBuilder mpbNS = builder.part(
            "north_south",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal   |
                VertexAttributes.Usage.ColorUnpacked,
            new Material(ColorAttribute.createDiffuse(Color.WHITE))
        );

        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.DEPTH; z++) {
                    VoxelType type = chunk.getBlock(x, y, z);
                    if (!type.isVisible()) continue;

                    float tint = 0.9f + (float)Math.random() * 0.2f;
                    float r = type.color.r * tint;
                    float g = type.color.g * tint;
                    float b = type.color.b * tint;

                    float bx = chunk.originX + x;
                    float by = chunk.originY + y;
                    float bz = chunk.originZ + z;

                    // TOP / BOTTOM
                    if (!chunk.inBounds(x, y+1, z) || chunk.getBlock(x, y+1, z) == VoxelType.AIR) {
                        mpbTB.setColor(r, g, b, 1f);
                        addQuad(mpbTB, bx, by, bz, Face.TOP);
                    }
                    if (!chunk.inBounds(x, y-1, z) || chunk.getBlock(x, y-1, z) == VoxelType.AIR) {
                        mpbTB.setColor(r, g, b, 1f);
                        addQuad(mpbTB, bx, by, bz, Face.BOTTOM);
                    }

                    // EAST / WEST
                    if (!chunk.inBounds(x+1, y, z) || chunk.getBlock(x+1, y, z) == VoxelType.AIR) {
                        mpbEW.setColor(r, g, b, 1f);
                        addQuad(mpbEW, bx, by, bz, Face.EAST);
                    }
                    if (!chunk.inBounds(x-1, y, z) || chunk.getBlock(x-1, y, z) == VoxelType.AIR) {
                        mpbEW.setColor(r, g, b, 1f);
                        addQuad(mpbEW, bx, by, bz, Face.WEST);
                    }

                    // NORTH / SOUTH
                    if (!chunk.inBounds(x, y, z+1) || chunk.getBlock(x, y, z+1) == VoxelType.AIR) {
                        mpbNS.setColor(r, g, b, 1f);
                        addQuad(mpbNS, bx, by, bz, Face.NORTH);
                    }
                    if (!chunk.inBounds(x, y, z-1) || chunk.getBlock(x, y, z-1) == VoxelType.AIR) {
                        mpbNS.setColor(r, g, b, 1f);
                        addQuad(mpbNS, bx, by, bz, Face.SOUTH);
                    }
                }
            }
        }

        return builder.end();
    }

    private enum Face { TOP, BOTTOM, NORTH, SOUTH, EAST, WEST }

    private static void addQuad(MeshPartBuilder mpb, float x, float y, float z, Face face) {
        switch (face) {
            case TOP:
                mpb.rect(
                    x,   y+1, z,
                    x,   y+1, z+1,
                    x+1, y+1, z+1,
                    x+1, y+1, z,
                    0, 1, 0
                );
                break;
            case BOTTOM:
                mpb.rect(
                    x,   y,   z,
                    x+1, y,   z,
                    x+1, y,   z+1,
                    x,   y,   z+1,
                    0, -1, 0
                );
                break;
            case NORTH:
                mpb.rect(
                    x,   y,   z+1,
                    x+1, y,   z+1,
                    x+1, y+1, z+1,
                    x,   y+1, z+1,
                    0, 0, -1
                );
                break;
            case SOUTH:
                mpb.rect(
                    x+1, y,   z,
                    x,   y,   z,
                    x,   y+1, z,
                    x+1, y+1, z,
                    0, 0, 1
                );
                break;
            case EAST:
                mpb.rect(
                    x+1, y,   z+1,
                    x+1, y,   z,
                    x+1, y+1, z,
                    x+1, y+1, z+1,
                    -1, 0, 0
                );
                break;
            case WEST:
                mpb.rect(
                    x,   y,   z,
                    x,   y,   z+1,
                    x,   y+1, z+1,
                    x,   y+1, z,
                    1, 0, 0
                );
                break;
        }
    }
}

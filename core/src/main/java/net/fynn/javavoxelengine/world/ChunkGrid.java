package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.utils.Array;

public class ChunkGrid {
    private Array<Chunk> chunks;
    private int gridWidth, gridDepth;

    public ChunkGrid(int gridWidth, int gridDepth, int seed) {
        this.gridWidth = gridWidth;
        this.gridDepth = gridDepth;
        this.chunks = new Array<>();

        generateChunks(seed);
    }

    private void generateChunks(int seed) {
        // total size in world‑units
        int totalWidth  = gridWidth  * Chunk.WIDTH;
        int totalDepth  = gridDepth  * Chunk.DEPTH;
        // half extents
        int halfWidth   = totalWidth  / 2;
        int halfDepth   = totalDepth  / 2;

        for (int x = 0; x < gridWidth; x++) {
            for (int z = 0; z < gridDepth; z++) {
                // subtract halfWorld so that chunk (0,0) ends up at -halfExtent,
                // chunk center ends up straddling 0,0
                int originX = x * Chunk.WIDTH - halfWidth;
                int originZ = z * Chunk.DEPTH - halfDepth;

                Chunk chunk = new Chunk(seed, originX, 0, originZ);
                chunks.add(chunk);
            }
        }
    }


    public Array<Chunk> getChunks() {
        return chunks;
    }
}

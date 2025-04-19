package net.fynn.javavoxelengine.chunk;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import net.fynn.javavoxelengine.voxel.VoxelType;

/**
 * Verwaltet ein Gitter von Chunks.
 * Erstellt und speichert Chunks basierend auf einem Seed-Wert.
 */
public class ChunkGrid {
    private Array<Chunk> chunks;
    private int gridWidth, gridDepth;

    /**
     * Erstellt ein neues Chunk-Gitter mit der angegebenen Breite und Tiefe.
     *
     * @param gridWidth Die Breite des Gitters.
     * @param gridDepth Die Tiefe des Gitters.
     * @param seed      Der Seed-Wert für die zufällige Generierung.
     */
    public ChunkGrid(int gridWidth, int gridDepth, int seed) {
        this.gridWidth = gridWidth;
        this.gridDepth = gridDepth;
        this.chunks = new Array<>();

        generateChunks(seed);
    }

    /**
     * Erstellt die Chunks basierend auf dem Seed-Wert.
     *
     * @param seed Der Seed-Wert für die zufällige Generierung.
     */
    private void generateChunks(int seed) {
        for (int x = 0; x < gridWidth; x++) {
            for (int z = 0; z < gridDepth; z++) {
                int originX = x * Chunk.WIDTH;
                int originZ = z * Chunk.DEPTH;

                Chunk chunk = new Chunk(seed, originX, 0, originZ);
                chunks.add(chunk);
            }
        }
    }

    /** Returns the chunk at grid‐indices x,z, or null if out of bounds. */
    public Chunk getChunk(int gridX, int gridZ) {
        if (gridX < 0 || gridX >= gridWidth || gridZ < 0 || gridZ >= gridDepth) return null;
        return chunks.get(gridX * gridDepth + gridZ);
    }

    /**
     * Converts worldX/worldZ into grid indices, accounting
     * for the fact that chunk (0,0) is at -halfWorldWidth.
     */
    public Chunk getChunkAtWorld(float worldX, float worldZ) {

        float chunkX = worldX / Chunk.WIDTH;
        float chunkZ = worldZ / Chunk.DEPTH;

        int chunkIDX = (int)Math.floor(chunkX);
        int chunkIDZ = (int)Math.floor(chunkZ);

        return getChunk(chunkIDX, chunkIDZ);
    }

    public Vector3 getChunkLocalCoords(Vector3 worldCoords) {
        return new Vector3(worldCoords.x % Chunk.WIDTH, worldCoords.y % Chunk.HEIGHT, worldCoords.z % Chunk.DEPTH);
    }


    public VoxelType getBlockFromWorld(Vector3 worldCoords) {
        Chunk cnk = getChunkAtWorld(worldCoords.x,worldCoords.z);

        Vector3 localCoords = getChunkLocalCoords(worldCoords);

        int localX = (int)Math.floor(localCoords.x);
        int localY = (int)Math.floor(localCoords.y);
        int localZ = (int)Math.floor(localCoords.z);

        if (localX < 0 || localX >= Chunk.WIDTH
            || localY < 0 || localY >= Chunk.HEIGHT
            || localZ < 0 || localZ >= Chunk.DEPTH) {
            return VoxelType.AIR;
        }

        return cnk.getBlock(localX, localY, localZ);
    }

    public void setBlockFromWorld(Vector3 worldCoords, VoxelType type) {
        Chunk cnk = getChunkAtWorld(worldCoords.x,worldCoords.z);

        Vector3 localCoords = getChunkLocalCoords(worldCoords);

        int localX = (int)Math.floor(localCoords.x);
        int localY = (int)Math.floor(localCoords.y);
        int localZ = (int)Math.floor(localCoords.z);

        cnk.setBlock(localX, localY, localZ, type);
    }

    /**
     * Gibt die Liste der Chunks zurück.
     *
     * @return Die Liste der Chunks.
     */
    public Array<Chunk> getChunks() {
        return chunks;
    }
}

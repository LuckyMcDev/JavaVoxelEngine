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
        // Gesamtgröße in Welt-Einheiten
        int totalWidth  = gridWidth  * Chunk.WIDTH;
        int totalDepth  = gridDepth  * Chunk.DEPTH;
        // Halbe Ausdehnung
        int halfWidth   = totalWidth  / 2;
        int halfDepth   = totalDepth  / 2;

        for (int x = 0; x < gridWidth; x++) {
            for (int z = 0; z < gridDepth; z++) {
                // Subtrahiere halbe Welt, damit Chunk (0,0) bei -halfExtent landet
                int originX = x * Chunk.WIDTH - halfWidth;
                int originZ = z * Chunk.DEPTH - halfDepth;

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
    public Chunk getChunkAtWorld(int worldX, int worldZ) {


        int relativeX = worldX + Chunk.WIDTH * this.gridWidth/2;
        int relativeZ = worldZ + Chunk.DEPTH * this.gridDepth/2;


        int chunkX = relativeX / Chunk.WIDTH;
        int chunkZ = relativeZ / Chunk.DEPTH;

        int chunkIDX = (int)Math.floor(chunkX);
        int chunkIDZ = (int)Math.floor(chunkZ);

        return getChunk(chunkIDX, chunkIDZ);
    }

    public Vector3 getChunkLocalCoords(Vector3 worldCoords) {
        // 1) floor the floats
        int wx = (int)Math.floor(worldCoords.x);
        int wy = (int)Math.floor(worldCoords.y);
        int wz = (int)Math.floor(worldCoords.z);

        // 2) pick the chunk by floored x/z, not the truncated ones
        Chunk cnk = getChunkAtWorld(wx, wz);

        // 3) subtract the chunk’s origin to get local block indices
        int localX = wx - cnk.originX;
        int localY = wy - cnk.originY;
        int localZ = wz - cnk.originZ;

        return new Vector3(localX, localY, localZ);
    }


    public VoxelType getBlockFromWorld(Vector3 worldCoords) {
        int wx = (int)Math.floor(worldCoords.x);
        int wy = (int)Math.floor(worldCoords.y);
        int wz = (int)Math.floor(worldCoords.z);

        // true floor division for chunk indices
        int chunkX = Math.floorDiv(wx + Chunk.WIDTH * gridWidth/2,  Chunk.WIDTH);
        int chunkZ = Math.floorDiv(wz + Chunk.DEPTH * gridDepth/2,  Chunk.DEPTH);
        Chunk cnk = getChunk(chunkX, chunkZ);
        if (cnk == null) return VoxelType.AIR;

        int localX = wx - cnk.originX;
        int localY = wy - cnk.originY;
        int localZ = wz - cnk.originZ;
        if (localX < 0 || localX >= Chunk.WIDTH
            || localY < 0 || localY >= Chunk.HEIGHT
            || localZ < 0 || localZ >= Chunk.DEPTH) {
            return VoxelType.AIR;
        }
        return cnk.getBlock(localX, localY, localZ);
    }

    public void setBlockFromWorld(Vector3 worldCoords, VoxelType type) {
        int wx = (int)Math.floor(worldCoords.x);
        int wy = (int)Math.floor(worldCoords.y);
        int wz = (int)Math.floor(worldCoords.z);

        Chunk cnk = getChunkAtWorld(wx, wz);
        if (cnk == null) return;

        int localX = wx - cnk.originX;
        int localY = wy - cnk.originY;
        int localZ = wz - cnk.originZ;

        if (localX < 0 || localX >= Chunk.WIDTH
            || localY < 0 || localY >= Chunk.HEIGHT
            || localZ < 0 || localZ >= Chunk.DEPTH) {
            return;
        }

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

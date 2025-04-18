package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.utils.Array;

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
    public Chunk getChunkAtWorld(float worldX, float worldZ) {
        float totalW = gridWidth  * Chunk.WIDTH;
        float totalD = gridDepth  * Chunk.DEPTH;
        float halfW  = totalW  * 0.5f;
        float halfD  = totalD  * 0.5f;

        int gx = (int)Math.floor((worldX + halfW) / Chunk.WIDTH);
        int gz = (int)Math.floor((worldZ + halfD) / Chunk.DEPTH);
        return getChunk(gx, gz);
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

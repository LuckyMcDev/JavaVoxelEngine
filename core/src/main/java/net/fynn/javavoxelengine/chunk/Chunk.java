package net.fynn.javavoxelengine.chunk;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import make.some.noise.Noise;
import net.fynn.javavoxelengine.player.Player;
import net.fynn.javavoxelengine.voxel.VoxelType;

import java.util.Random;

/**
 * Repräsentiert einen Chunk in der Voxel-Welt.
 * Ein Chunk besteht aus einem 3D-Array von Voxeltypen und hat eine feste Größe.
 */
public class Chunk {
    /** Die Breite des Chunks. */
    public static final int WIDTH = 64;

    /** Die Tiefe des Chunks. */
    public static final int DEPTH = 64;

    /** Die Höhe des Chunks. */
    public static final int HEIGHT = 64;

    /** Die Höhe eines Baumstammes*/
    public static final int TRUNK_HEIGHT = 7;

    /**Der radius der Blätter*/
    public static final int LEAF_RADIUS  = 3;

    public static final float CHUNK_RENDER_DISTANCE = 150f;
    public static final float CHUNK_RENDER_DISTANCE_SQUARED = CHUNK_RENDER_DISTANCE * CHUNK_RENDER_DISTANCE;

    /** Die Voxeltypen, die diesen Chunk bilden. */
    public final VoxelType[][][] blocks;

    /** Die Ursprungskoordinaten des Chunks. */
    public final int originX, originY, originZ;

    private BoundingBox boundingBox;

    /**
     * Erstellt einen neuen Chunk und generiert dessen Gelände unter Verwendung von Rauschen.
     *
     * @param seed Der Seed für den Perlin Generator.
     * @param originX Die X-Koordinate des Ursprungs des Chunks.
     * @param originY Die Y-Koordinate des Ursprungs des Chunks.
     * @param originZ Die Z-Koordinate des Ursprungs des Chunks.
     */
    public Chunk(int seed, int originX, int originY, int originZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        blocks = new VoxelType[WIDTH][HEIGHT][DEPTH];
        generateTerrain(seed);
        generateTrees(seed);
        createBoundingBox();
    }

    /**
     * Erstellt die Begrenzungsbox für den Chunk.
     */
    public void createBoundingBox() {
        Vector3 min = new Vector3(originX, originY, originZ);
        Vector3 max = new Vector3(originX + WIDTH, originY + HEIGHT, originZ + DEPTH);
        boundingBox = new BoundingBox(min, max);
    }

    /**
     * Gibt die Begrenzungsbox des Chunks zurück.
     *
     * @return Die Begrenzungsbox des Chunks.
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Generiert das Gelände des Chunks unter Verwendung von Perlin-Rauschen.
     *
     * @param seed Der Seed für den Perlin Generator.
     */
    public void generateTerrain(int seed) {
        // Erstellen eines Generator (MakeSomeNoise-Bibliothek) mit dem angegebenen Seed.
        Noise noise = new Noise(seed);

        // Parameter: Skalieren der Rauschkoordinaten und Auswahl einer maximalen Höhe.
        int maxTerrainHeight = HEIGHT - 20; // z.B. 255/4 ~ 63 Blöcke maximale Geländehöhe

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                // Generieren eines Rauschwerts (in [-1,1]) und Normalisieren auf [0, 1].
                double n = noise.getPerlin((originX + x), (originZ + z));
                int terrainHeight = (int) (((n + 1) / 2) * maxTerrainHeight);
                for (int y = 0; y < HEIGHT; y++) {
                    if (y <= terrainHeight) {
                        // Einfache Schichtung:
                        if (y < terrainHeight - 3) {
                            blocks[x][y][z] = VoxelType.STONE;
                        } else if (y < terrainHeight) {
                            blocks[x][y][z] = VoxelType.DIRT;
                        } else {
                            blocks[x][y][z] = VoxelType.GRASS;
                        }
                    } else {
                        blocks[x][y][z] = VoxelType.AIR;
                    }
                }
            }
        }
    }

    /**
     * Generiert Bäume im Chunk.
     * Die Bäume sind entweder Hell- oder Dunkelgrün, mit variablem Stamm,
     * zusätzlichem Ast-Detail und spawn keinem an den Chunk-Rändern.
     *
     * @param seed Der Seed für die zufällige Generierung.
     */
    public void generateTrees(int seed) {
        // Deterministischer RNG pro Chunk
        long mix = ((long)originX * 73856093L) ^ ((long)originZ * 19349663L) ^ seed;
        Random rand = new Random(mix);

        // 70% Chance, einen Baum zu spawnen
        if (rand.nextDouble() >= 0.7) return;

        // Abstand zu den Chunk-Rändern in Blöcken
        final int BORDER = 4;
        // Wähle zufällige (tx,tz) mit mind. BORDER Abstand zu Rand
        int tx = BORDER + rand.nextInt(WIDTH  - 2 * BORDER);
        int tz = BORDER + rand.nextInt(DEPTH  - 2 * BORDER);

        // Finde Oberflächenhöhe
        int ty = getSurfaceHeight(tx, tz);
        if (ty < 0 || ty + TRUNK_HEIGHT + LEAF_RADIUS + 1 >= HEIGHT) return;

        // Variierende Stammhöhe und Blatt-Radius
        int trunkHeight = TRUNK_HEIGHT - 1 + rand.nextInt(3);          // z.B. TRUNK_HEIGHT±1
        int leafRadius  = LEAF_RADIUS  - 1 + rand.nextInt(3);          // z.B. LEAF_RADIUS±1

        // Blatt-Farbe
        VoxelType leafType = rand.nextBoolean()
            ? VoxelType.LEAVES_LIGHT
            : VoxelType.LEAVES_DARK;

        // 1) Stamm aufbauen
        for (int i = 1; i <= trunkHeight; i++) {
            setBlock(tx, ty + i, tz, VoxelType.WOOD);
        }

        // 2) Ein paar zufällige Seitentriebe / Äste
        for (int dir = 0; dir < 4; dir++) {
            if (rand.nextDouble() < 0.25) {
                int branchY = ty + 2 + rand.nextInt(trunkHeight - 2);
                int dx = (dir == 0 ?  1 : dir == 1 ? -1 : 0);
                int dz = (dir == 2 ?  1 : dir == 3 ? -1 : 0);
                int bx = tx + dx, bz = tz + dz;
                if (inBounds(bx, branchY, bz)) {
                    setBlock(bx, branchY, bz, VoxelType.WOOD);
                    // Spitze mit Blättern
                    for (int lx = bx - 1; lx <= bx + 1; lx++) {
                        for (int lz = bz - 1; lz <= bz + 1; lz++) {
                            int ly = branchY;
                            if (inBounds(lx, ly, lz) && getBlock(lx, ly, lz) == VoxelType.AIR) {
                                setBlock(lx, ly, lz, leafType);
                            }
                        }
                    }
                }
            }
        }

        // 3) Blattkrone als geschichtete Kreise (sphärisch)
        int canopyHeight = 2 + rand.nextInt(2); // Höhe der Krone: 2–3 Ebenen
        for (int dy = 0; dy <= canopyHeight; dy++) {
            // Radius nimmt nach oben ab
            float layerFraction = dy / (float) canopyHeight;
            float layerRadius   = leafRadius * (1f - layerFraction) + 0.5f;

            int intRadius = MathUtils.ceil(layerRadius);
            int yLevel    = ty + trunkHeight + dy;

            for (int dx = -intRadius; dx <= intRadius; dx++) {
                for (int dz = -intRadius; dz <= intRadius; dz++) {
                    // Kreisform: (dx, dz) im Radius?
                    if (dx*dx + dz*dz <= layerRadius*layerRadius) {
                        int lx = tx + dx;
                        int lz = tz + dz;
                        if (!inBounds(lx, yLevel, lz)) continue;

                        // Leichtes Zufalls-Muster, damit die Krone nicht perfekt symmetrisch wird
                        if (rand.nextFloat() < 0.9f) {
                            if (getBlock(lx, yLevel, lz) == VoxelType.AIR) {
                                setBlock(lx, yLevel, lz, leafType);
                            }
                        }
                    }
                }
            }
        }

        // 4) Äpfel unter zufälligen Blättern
        for (int dx = -leafRadius; dx <= leafRadius; dx++) {
            for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= leafRadius) {
                    int lx = tx + dx, lz = tz + dz;
                    for (int dy = trunkHeight; dy <= trunkHeight + 1; dy++) {
                        int ly = ty + dy;
                        if (inBounds(lx, ly, lz) && getBlock(lx, ly, lz) != VoxelType.AIR) {
                            if (rand.nextDouble() < 0.2) {
                                if (inBounds(lx, ly - 1, lz) && getBlock(lx, ly - 1, lz) == VoxelType.AIR) {
                                    setBlock(lx, ly - 1, lz, VoxelType.APPLE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    /**
     * Ermittelt den höchsten Y wert an einem X und Z wert
     *
     * @param x - X position
     * @param z - Z position
     * @return - Die Höhe / Y position
     */
    public int getSurfaceHeight(int x, int z) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            VoxelType t = getBlock(x, y, z);
            if (t != VoxelType.AIR) return y;
        }
        return -1;
    }

    /**
     * Prüft, ob eine Koordinate in den bounds eines Chunks ist
     *
     * @param x - Die X Position
     * @param y - Die Y Position
     * @param z - Die Z Position
     * @return - boolean, ob in bounds
     */
    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < WIDTH
            && y >= 0 && y < HEIGHT
            && z >= 0 && z < DEPTH;
    }

    /**
     * Schreibt sicher in das Block-Array.
     *
     * @param x Die X-Koordinate des Blocks.
     * @param y Die Y-Koordinate des Blocks.
     * @param z Die Z-Koordinate des Blocks.
     * @param type Der Voxeltyp des Blocks.
     */
    public void setBlock(int x, int y, int z, VoxelType type) {
        if (inBounds(x, y, z)) {
            blocks[x][y][z] = type;
        }
    }

    /**
     * Gibt den Voxeltyp an einer bestimmten Position zurück.
     *
     * @param x Die X-Koordinate des Blocks.
     * @param y Die Y-Koordinate des Blocks.
     * @param z Die Z-Koordinate des Blocks.
     * @return Der Voxeltyp an der angegebenen Position.
     */
    public VoxelType getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    /**
     * Bestimmt, ob ein Chunk basierend auf der Entfernung gerendert werden soll.
     *
     * @param chunk Der zu überprüfende Chunk.
     * @return True, wenn der Chunk gerendert werden soll, sonst false.
     */
    public boolean shouldRenderChunk(Chunk chunk, Player player) {
        float dx = player.getCamera().position.x - (chunk.originX + Chunk.WIDTH / 2f);
        float dz = player.getCamera().position.z - (chunk.originZ + Chunk.DEPTH / 2f);
        float distanceSquared = dx * dx + dz * dz;

        return distanceSquared <= CHUNK_RENDER_DISTANCE_SQUARED && isChunkVisible(chunk, player.getCamera().frustum);
    }

    /**
     * Überprüft, ob ein Chunk im Sichtfeld der Kamera liegt.
     *
     * @param chunk Der zu überprüfende Chunk.
     * @return True, wenn der Chunk sichtbar ist, sonst false.
     */
    public boolean isChunkVisible(Chunk chunk, Frustum frustum) {
        BoundingBox chunkBox = chunk.getBoundingBox();
        return frustum.boundsInFrustum(chunkBox);
    }
}

package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import make.some.noise.Noise;

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
    private static final int TRUNK_HEIGHT = 7;

    /**Der radius der Blätter*/
    private static final int LEAF_RADIUS  = 3;

    /** Die Voxeltypen, die diesen Chunk bilden. */
    private final VoxelType[][][] blocks;

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
    private void createBoundingBox() {
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
    private void generateTerrain(int seed) {
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
     * Die Bäume sind entweder Hell oder Dunkelgrün
     *
     * @param seed Der Seed für die zufällige Generierung.
     */
    private void generateTrees(int seed) {
        // Mischen des Ursprungs in den RNG, damit jeder Chunk deterministisch ist
        long mix = ((long)originX * 73856093L) ^ ((long)originZ * 19349663L) ^ seed;
        Random rand = new Random(mix);

        // 70% Chance, einen Baum zu spawnen
        if (rand.nextDouble() >= 0.7) return;

        // Wähle zufällige x, z innerhalb dieses Chunks
        int tx = rand.nextInt(WIDTH);
        int tz = rand.nextInt(DEPTH);

        // Finde die Oberflächen-Y (erster Nicht-Luft-Block von oben)
        int ty = getSurfaceHeight(tx, tz);
        if (ty < 0 || ty + TRUNK_HEIGHT + LEAF_RADIUS >= HEIGHT) return;

        // Entscheide, ob die Blätter hell oder dunkel sein sollen
        VoxelType leafType = rand.nextBoolean() ? VoxelType.LEAVES_LIGHT : VoxelType.LEAVES_DARK;

        // Baue den Stamm
        for (int i = 1; i <= TRUNK_HEIGHT; i++) {
            setBlock(tx, ty + i, tz, VoxelType.WOOD);
        }

        // Baue eine einfache Blattkrone
        for (int dx = -LEAF_RADIUS; dx <= LEAF_RADIUS; dx++) {
            for (int dz = -LEAF_RADIUS; dz <= LEAF_RADIUS; dz++) {
                // Eine kleine Kleeblattform unter Verwendung der Manhattan-Distanz
                if (Math.abs(dx) + Math.abs(dz) <= LEAF_RADIUS) {
                    int lx = tx + dx;
                    int lz = tz + dz;
                    for (int dy = TRUNK_HEIGHT; dy <= TRUNK_HEIGHT + 1; dy++) {
                        int ly = ty + dy;
                        if (inBounds(lx, ly, lz) && getBlock(lx, ly, lz) == VoxelType.AIR) {
                            setBlock(lx, ly, lz, leafType);
                        }
                    }
                }
            }
        }

        for (int dx = -LEAF_RADIUS; dx <= LEAF_RADIUS; dx++) {
            for (int dz = -LEAF_RADIUS; dz <= LEAF_RADIUS; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= LEAF_RADIUS) {
                    int lx = tx + dx;
                    int lz = tz + dz;
                    for (int dy = TRUNK_HEIGHT; dy <= TRUNK_HEIGHT + 1; dy++) {
                        int ly = ty + dy;
                        if (inBounds(lx, ly, lz) && getBlock(lx, ly, lz) != VoxelType.AIR) {
                            // 20% chance per leaf block
                            if (rand.nextDouble() < 0.2) {
                                // place apple one block below this leaf
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


    // Hilfsmethode zur Ermittlung der obersten festen Blockhöhe
    public int getSurfaceHeight(int x, int z) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            VoxelType t = getBlock(x, y, z);
            if (t != VoxelType.AIR) return y;
        }
        return -1;
    }

    // Einfache Grenzprüfung
    private boolean inBounds(int x, int y, int z) {
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
}

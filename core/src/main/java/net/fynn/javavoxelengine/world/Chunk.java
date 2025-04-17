package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import make.some.noise.Noise;

import java.util.Random;

public class Chunk {
    public static final int WIDTH = 48;
    public static final int DEPTH = 48;
    public static final int HEIGHT = 48;

    private static final int TRUNK_HEIGHT = 4;
    private static final int LEAF_RADIUS  = 2;

    private final VoxelType[][][] blocks;
    public final int originX, originY, originZ;
    private BoundingBox boundingBox;

    private void createBoundingBox() {
        Vector3 min = new Vector3(originX, originY, originZ);
        Vector3 max = new Vector3(originX + WIDTH, originY + HEIGHT, originZ + DEPTH);
        boundingBox = new BoundingBox(min, max);
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Creates a new chunk and generates its terrain using noise.
     *
     * @param seed The seed for the noise generator.
     * @param originX The x-coordinate of the chunk's origin.
     * @param originY The y-coordinate of the chunk's origin.
     * @param originZ The z-coordinate of the chunk's origin.
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

    private void generateTerrain(int seed) {
        // Create a noise generator (MakeSomeNoise library) with the given seed.
        Noise noise = new Noise(seed);

        // Parameters: Scale the noise coordinates and choose a maximum height.
        int maxTerrainHeight = HEIGHT - 20; // e.g., 255/4 ~ 63 blocks maximum terrain height

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                // Generate a noise value (in [-1,1]) and normalize it to [0, 1].
                double n = noise.getPerlin((originX + x), (originZ + z));
                int terrainHeight = (int) (((n + 1) / 2) * maxTerrainHeight);
                for (int y = 0; y < HEIGHT; y++) {
                    if (y <= terrainHeight) {
                        // Simple layering:
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


    private void generateTrees(int seed) {
        // mix origin into the RNG so each chunk is deterministic
        long mix = ((long)originX * 73856093L) ^ ((long)originZ * 19349663L) ^ seed;
        Random rand = new Random(mix);

        // 50/50 chance to spawn one tree
        if (!rand.nextBoolean()) return;

        // pick a random x,z inside this chunk
        int tx = rand.nextInt(WIDTH);
        int tz = rand.nextInt(DEPTH);

        // find the surface Y (first non‐air from the top)
        int ty = getSurfaceHeight(tx, tz);
        if (ty < 0 || ty + TRUNK_HEIGHT + LEAF_RADIUS >= HEIGHT) return;

        // build the trunk
        for (int i = 1; i <= TRUNK_HEIGHT; i++) {
            setBlock(tx, ty + i, tz, VoxelType.WOOD);
        }

        // build a simple leaf crown
        for (int dx = -LEAF_RADIUS; dx <= LEAF_RADIUS; dx++) {
            for (int dz = -LEAF_RADIUS; dz <= LEAF_RADIUS; dz++) {
                // a little clover‑shape using Manhattan distance
                if (Math.abs(dx) + Math.abs(dz) <= LEAF_RADIUS) {
                    int lx = tx + dx;
                    int lz = tz + dz;
                    for (int dy = TRUNK_HEIGHT; dy <= TRUNK_HEIGHT + 1; dy++) {
                        int ly = ty + dy;
                        if (inBounds(lx, ly, lz) && getBlock(lx, ly, lz) == VoxelType.AIR) {
                            setBlock(lx, ly, lz, VoxelType.LEAVES);
                        }
                    }
                }
            }
        }
    }

    // helper to find the topmost solid block:
    private int getSurfaceHeight(int x, int z) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            VoxelType t = getBlock(x, y, z);
            if (t != VoxelType.AIR) return y;
        }
        return -1;
    }

    // simple bounds‐check
    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < WIDTH
            && y >= 0 && y < HEIGHT
            && z >= 0 && z < DEPTH;
    }

    /**
     * Safely writes into the blocks array.
     */
    public void setBlock(int x, int y, int z, VoxelType type) {
        if (inBounds(x, y, z)) {
            blocks[x][y][z] = type;
        }
    }

    public VoxelType getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }
}

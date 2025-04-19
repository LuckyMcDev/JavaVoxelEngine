package net.fynn.javavoxelengine.chunk;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;
import net.fynn.javavoxelengine.util.Predicate3;
import net.fynn.javavoxelengine.world.VoxelModelCache;
import net.fynn.javavoxelengine.world.VoxelType;

public class GenerateVoxelInstances {
    public static void gen(Chunk chunk, Array<ModelInstance> voxelInstances) {
        final int W = Chunk.WIDTH, H = Chunk.HEIGHT, D = Chunk.DEPTH;
        boolean[][][] visited = new boolean[W][H][D];

        // Hilfsfunktion zur Überprüfung der Voxel-Eignung
        Predicate3<Integer, Integer, Integer> isEligible = (x, y, z) -> !visited[x][y][z] && chunk.getBlock(x, y, z) != null && chunk.getBlock(x, y, z).isVisible() && isBlockExposedToAir(chunk, x, y, z);

        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                for (int z = 0; z < D; z++) {
                    if (!isEligible.test(x, y, z)) continue;

                    VoxelType type = chunk.getBlock(x, y, z);

                    // 1) Wachsen entlang der x-Achse
                    int dx = 1;
                    while (x + dx < W && isEligible.test(x + dx, y, z)
                        && chunk.getBlock(x + dx, y, z) == type) {
                        dx++;
                    }

                    // 2) Wachsen entlang der y-Achse
                    int dy = 1;
                    outerY:
                    while (y + dy < H) {
                        for (int xi = 0; xi < dx; xi++) {
                            if (!isEligible.test(x + xi, y + dy, z)
                                || chunk.getBlock(x + xi, y + dy, z) != type) {
                                break outerY;
                            }
                        }
                        dy++;
                    }

                    // 3) Wachsen entlang der z-Achse
                    int dz = 1;
                    outerZ:
                    while (z + dz < D) {
                        for (int xi = 0; xi < dx; xi++) {
                            for (int yi = 0; yi < dy; yi++) {
                                if (!isEligible.test(x + xi, y + yi, z + dz)
                                    || chunk.getBlock(x + xi, y + yi, z + dz) != type) {
                                    break outerZ;
                                }
                            }
                        }
                        dz++;
                    }

                    // Als besucht markieren
                    for (int xi = 0; xi < dx; xi++) {
                        for (int yi = 0; yi < dy; yi++) {
                            for (int zi = 0; zi < dz; zi++) {
                                visited[x + xi][y + yi][z + zi] = true;
                            }
                        }
                    }

                    // Eine Instanz erstellen, die den dx×dy×dz-Bereich abdeckt
                    Model model = VoxelModelCache.getModel(type);
                    if (model != null) {
                        ModelInstance inst = new ModelInstance(model);

                        // Weltkoordinaten des Blockzentrums
                        float cx = chunk.originX + x + (dx - 1) * 0.5f;
                        float cy = chunk.originY + y + (dy - 1) * 0.5f;
                        float cz = chunk.originZ + z + (dz - 1) * 0.5f;

                        inst.transform.setToTranslation(cx, cy, cz);
                        inst.transform.scale(dx, dy, dz);

                        voxelInstances.add(inst);
                    }
                }
            }
        }
    }

    /**
     * Überprüft, ob ein Block der Luft ausgesetzt ist.
     *
     * @param chunk Der Chunk, der den Block enthält.
     * @param x Die X-Koordinate des Blocks.
     * @param y Die Y-Koordinate des Blocks.
     * @param z Die Z-Koordinate des Blocks.
     * @return True, wenn der Block der Luft ausgesetzt ist, sonst false.
     */
    public static boolean isBlockExposedToAir(Chunk chunk, int x, int y, int z) {
        if (chunk.getBlock(x, y, z) == VoxelType.AIR) return false;

        // Korrekte Nachbarprüfungen (6 Richtungen)
        if (y < Chunk.HEIGHT - 1 && chunk.getBlock(x, y + 1, z) == VoxelType.AIR) return true; // Oben
        if (y > 0 && chunk.getBlock(x, y - 1, z) == VoxelType.AIR) return true; // Unten
        if (x < Chunk.WIDTH - 1 && chunk.getBlock(x + 1, y, z) == VoxelType.AIR) return true; // Rechts
        if (x > 0 && chunk.getBlock(x - 1, y, z) == VoxelType.AIR) return true; // Links
        if (z < Chunk.DEPTH - 1 && chunk.getBlock(x, y, z + 1) == VoxelType.AIR) return true; // Vorne
        return z > 0 && chunk.getBlock(x, y, z - 1) == VoxelType.AIR; // Hinten
    }

}

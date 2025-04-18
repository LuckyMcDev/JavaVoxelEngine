package net.fynn.javavoxelengine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.fynn.javavoxelengine.challenge.AppleCollector;
import net.fynn.javavoxelengine.challenge.ChallengeManager;
import net.fynn.javavoxelengine.challenge.ChallengeType;
import net.fynn.javavoxelengine.imgui.ThisImGui;
import net.fynn.javavoxelengine.player.Crosshair;
import net.fynn.javavoxelengine.player.Player;
import net.fynn.javavoxelengine.world.Chunk;
import net.fynn.javavoxelengine.world.ChunkGrid;
import net.fynn.javavoxelengine.world.VoxelModelCache;
import net.fynn.javavoxelengine.world.VoxelType;

/**
 * Hauptklasse der Voxel-Engine.
 * Diese Klasse initialisiert und verwaltet die Voxel-Welt und die Benutzeroberfläche.
 *
 * @author Fynn
 * @version 1.0
 */
public class VoxelEngine extends ApplicationAdapter {

    private ThisImGui thisImGui;

    private ModelBatch modelBatch;
    private Environment environment;
    private ChunkGrid chunkGrid;
    private Frustum frustum;
    private Player player;
    private ChallengeManager challengeManager;
    private AppleCollector appleCollector;
    private Crosshair crosshair;

    private static final float CHUNK_RENDER_DISTANCE = 150f;
    private static final float CHUNK_RENDER_DISTANCE_SQUARED = CHUNK_RENDER_DISTANCE * CHUNK_RENDER_DISTANCE;

    /**
     * Initialisiert die Voxel-Engine und deren Komponenten.
     */
    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        VoxelModelCache.initialize(1f, 1f, 1f);

        chunkGrid = new ChunkGrid(10, 10, 1237161111); // Keine Chunk-Größe mehr nötig

        player = new Player(chunkGrid);
        frustum = player.getCamera().frustum;

        challengeManager = new ChallengeManager();

        challengeManager.start(ChallengeType.EASY);

        appleCollector = new AppleCollector();

        crosshair = new Crosshair();

        thisImGui = new ThisImGui();
    }

    /**
     * Render-Methode, die die Voxel-Welt und die Benutzeroberfläche zeichnet.
     */
    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(137f / 255f, 207f / 255f, 240f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        player.update(Gdx.graphics.getDeltaTime());

        challengeManager.update();

        modelBatch.begin(player.getCamera());
        int renderedModelCount = 0;       // Zähler zurücksetzen
        for (Chunk chunk : chunkGrid.getChunks()) {
            if (!shouldRenderChunk(chunk)) continue;

            Array<ModelInstance> voxelInstances = new Array<>();
            generateVoxelInstances(chunk, voxelInstances);

            // Rendern und zählen
            for (ModelInstance instance : voxelInstances) {
                modelBatch.render(instance, environment);
                renderedModelCount++;
            }
        }
        modelBatch.end();

        crosshair.render();

        appleCollector.tryCollectApple(player.getCamera(),)

        // Model count an ImGui übergeben
        thisImGui.render(player.getCamera(), renderedModelCount);
    }

    /**
     * Bestimmt, ob ein Chunk basierend auf der Entfernung gerendert werden soll.
     *
     * @param chunk Der zu überprüfende Chunk.
     * @return True, wenn der Chunk gerendert werden soll, sonst false.
     */
    public boolean shouldRenderChunk(Chunk chunk) {
        float dx = player.getCamera().position.x - (chunk.originX + Chunk.WIDTH / 2f);
        float dz = player.getCamera().position.z - (chunk.originZ + Chunk.DEPTH / 2f);
        float distanceSquared = dx * dx + dz * dz;

        if (distanceSquared > CHUNK_RENDER_DISTANCE_SQUARED) return false;
        return isChunkVisible(chunk);
    }

    /**
     * Überprüft, ob ein Chunk im Sichtfeld der Kamera liegt.
     *
     * @param chunk Der zu überprüfende Chunk.
     * @return True, wenn der Chunk sichtbar ist, sonst false.
     */
    public boolean isChunkVisible(Chunk chunk) {
        BoundingBox chunkBox = chunk.getBoundingBox();
        return frustum.boundsInFrustum(chunkBox);
    }

    /**
     * Generiert Voxel-Instanzen für einen gegebenen Chunk.
     *
     * @param chunk Der Chunk, für den Instanzen generiert werden sollen.
     * @param voxelInstances Liste, in die die generierten Instanzen gespeichert werden.
     */
    public void generateVoxelInstances(Chunk chunk, Array<ModelInstance> voxelInstances) {
        final int W = Chunk.WIDTH, H = Chunk.HEIGHT, D = Chunk.DEPTH;
        boolean[][][] visited = new boolean[W][H][D];

        // Hilfsfunktion zur Überprüfung der Voxel-Eignung
        Predicate3<Integer,Integer,Integer> isEligible = (x, y, z) -> !visited[x][y][z] && chunk.getBlock(x, y, z) != null && chunk.getBlock(x, y, z).isVisible();

        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                for (int z = 0; z < D; z++) {
                    if (!isEligible.test(x, y, z)) continue;

                    VoxelType type = chunk.getBlock(x, y, z);

                    // 1) Wachsen entlang der X-Achse
                    int dx = 1;
                    while (x + dx < W && isEligible.test(x + dx, y, z)
                        && chunk.getBlock(x + dx, y, z) == type) {
                        dx++;
                    }

                    // 2) Wachsen entlang der Y-Achse
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

                    // 3) Wachsen entlang der Z-Achse
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

    // Einfaches Drei-Argument-Prädikat
    @FunctionalInterface
    private interface Predicate3<A,B,C> {
        boolean test(A a, B b, C c);
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
    public boolean isBlockExposedToAir(Chunk chunk, int x, int y, int z) {
        if (chunk.getBlock(x, y, z) == VoxelType.AIR) return false;

        // Korrekte Nachbarprüfungen (6 Richtungen)
        if (y < Chunk.HEIGHT - 1 && chunk.getBlock(x, y + 10, z) == VoxelType.AIR) return true; // Oben
        if (y > 0 && chunk.getBlock(x, y - 1, z) == VoxelType.AIR) return true; // Unten
        if (x < Chunk.WIDTH - 1 && chunk.getBlock(x + 1, y, z) == VoxelType.AIR) return true; // Rechts
        if (x > 0 && chunk.getBlock(x - 1, y, z) == VoxelType.AIR) return true; // Links
        if (z < Chunk.DEPTH - 1 && chunk.getBlock(x, y, z + 1) == VoxelType.AIR) return true; // Vorne
        return z > 0 && chunk.getBlock(x, y, z - 1) == VoxelType.AIR; // Hinten
    }

    /**
     * Gibt Ressourcen frei, die von der Voxel-Engine verwendet werden.
     */
    @Override
    public void dispose() {
        modelBatch.dispose();
        VoxelModelCache.dispose();
        crosshair.dispose();
        thisImGui.dispose();
    }
}

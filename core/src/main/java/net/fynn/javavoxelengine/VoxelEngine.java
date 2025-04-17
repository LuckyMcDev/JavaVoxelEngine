package net.fynn.javavoxelengine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.fynn.javavoxelengine.imgui.ThisImGui;
import net.fynn.javavoxelengine.world.Chunk;
import net.fynn.javavoxelengine.world.ChunkGrid;
import net.fynn.javavoxelengine.world.VoxelModelCache;
import net.fynn.javavoxelengine.world.VoxelType;

public class VoxelEngine extends ApplicationAdapter {

    private ThisImGui thisImGui;

    private ModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera camera;
    private CameraInputController camController;
    private ChunkGrid chunkGrid;
    private Frustum frustum;

    public static float CHUNK_RENDER_DISTANCE = 200f;
    private static float CHUNK_RENDER_DISTANCE_SQUARED = CHUNK_RENDER_DISTANCE * CHUNK_RENDER_DISTANCE;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(50f, 50f, 50f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 500f;
        camera.update();

        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);

        frustum = camera.frustum;

        VoxelModelCache.initialize(1f, 1f, 1f);

        chunkGrid = new ChunkGrid(10, 10, 1237161111); // no chunkSize needed now

        thisImGui = new ThisImGui();
    }

    @Override
    public void render() {
        camController.update();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);

        int renderedModelCount = 0;       // ← reset counter

        for (Chunk chunk : chunkGrid.getChunks()) {
            if (!shouldRenderChunk(chunk)) continue;

            Array<ModelInstance> voxelInstances = new Array<>();
            generateVoxelInstances(chunk, voxelInstances);

            // render and count
            for (ModelInstance instance : voxelInstances) {
                modelBatch.render(instance, environment);
                renderedModelCount++;
            }
        }

        modelBatch.end();

        // now pass the count into your ImGui
        thisImGui.render(camera, renderedModelCount);
    }

    private boolean shouldRenderChunk(Chunk chunk) {
        float dx = camera.position.x - (chunk.originX + Chunk.WIDTH / 2f);
        float dz = camera.position.z - (chunk.originZ + Chunk.DEPTH / 2f);
        float distanceSquared = dx * dx + dz * dz;

        if (distanceSquared > CHUNK_RENDER_DISTANCE_SQUARED) return false;
        return isChunkVisible(chunk);
    }

    private boolean isChunkVisible(Chunk chunk) {
        BoundingBox chunkBox = chunk.getBoundingBox();
        return frustum.boundsInFrustum(chunkBox);
    }

    private void generateVoxelInstances(Chunk chunk, Array<ModelInstance> voxelInstances) {
        final int W = Chunk.WIDTH, H = Chunk.HEIGHT, D = Chunk.DEPTH;
        boolean[][][] visited = new boolean[W][H][D];

        // helper to test voxel eligibility
        Predicate3<Integer,Integer,Integer> isEligible = (x, y, z) -> !visited[x][y][z] && chunk.getBlock(x, y, z) != null && chunk.getBlock(x, y, z).isVisible() /*&& isBlockExposedToAir(chunk, x, y, z)*/;

        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                for (int z = 0; z < D; z++) {
                    if (!isEligible.test(x, y, z)) continue;

                    VoxelType type = chunk.getBlock(x, y, z);

                    // 1) Grow along X
                    int dx = 1;
                    while (x + dx < W && isEligible.test(x + dx, y, z)
                        && chunk.getBlock(x + dx, y, z) == type) {
                        dx++;
                    }

                    // 2) Grow along Y (all x in [x..x+dx) must match for each new y)
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

                    // 3) Grow along Z (all x,y in the dx×dy block must match for each new z)
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

                    // mark visited
                    for (int xi = 0; xi < dx; xi++) {
                        for (int yi = 0; yi < dy; yi++) {
                            for (int zi = 0; zi < dz; zi++) {
                                visited[x + xi][y + yi][z + zi] = true;
                            }
                        }
                    }

                    // create one instance scaled to cover dx×dy×dz
                    Model model = VoxelModelCache.getModel(type);
                    if (model != null) {
                        ModelInstance inst = new ModelInstance(model);

                        // world‐space center of the block
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

    // Simple three‐arg predicate for brevity
    @FunctionalInterface
    private interface Predicate3<A,B,C> {
        boolean test(A a, B b, C c);
    }

    private boolean isBlockExposedToAir(Chunk chunk, int x, int y, int z) {
        if (chunk.getBlock(x, y, z) == VoxelType.AIR) return false;

        // Correct neighbor checks (6 directions)
        if (y < Chunk.HEIGHT - 1 && chunk.getBlock(x, y + 10, z) == VoxelType.AIR) return true; // Up
        if (y > 0 && chunk.getBlock(x, y - 1, z) == VoxelType.AIR) return true; // Down
        if (x < Chunk.WIDTH - 1 && chunk.getBlock(x + 1, y, z) == VoxelType.AIR) return true; // Right
        if (x > 0 && chunk.getBlock(x - 1, y, z) == VoxelType.AIR) return true; // Left
        if (z < Chunk.DEPTH - 1 && chunk.getBlock(x, y, z + 1) == VoxelType.AIR) return true; // Forward
        return z > 0 && chunk.getBlock(x, y, z - 1) == VoxelType.AIR; // Backward
    }

    public float getChunkRenderDistance() {
        return CHUNK_RENDER_DISTANCE;
    }

    public void setChunkRenderDistance(float NEWchunkRenderDistance) {
        CHUNK_RENDER_DISTANCE = NEWchunkRenderDistance;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        VoxelModelCache.dispose();
        thisImGui.dispose();
    }
}

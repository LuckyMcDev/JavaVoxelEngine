package net.fynn.javavoxelengine.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import net.fynn.javavoxelengine.world.Chunk;
import net.fynn.javavoxelengine.world.ChunkGrid;

public class Player {
    private final PerspectiveCamera camera;
    private final FirstPersonCameraController controller;
    private final ChunkGrid chunkGrid;

    // physics
    private float vy = 0f;                 // current vertical velocity
    private static final float GRAVITY = -30f;  // units/sec²
    private static final float EYE_HEIGHT = 2f; // camera offset above ground

    public Player(ChunkGrid chunkGrid) {
        this.chunkGrid = chunkGrid;

        camera = new PerspectiveCamera(80,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        camera.position.set(0f, 50f, 0f);
        camera.near = 0.1f;
        camera.far  = 500f;
        camera.update();

        controller = new FirstPersonCameraController(camera);
        controller.setVelocity(10f);
        controller.setDegreesPerPixel(0.3f);
        Gdx.input.setInputProcessor(controller);
    }

    public void update(float delta) {
        // 1) horizontal look & movement
        controller.update(delta);

        // 2) apply gravity
        vy += GRAVITY * delta;
        camera.position.y += vy * delta;

        // 3) ground‐collision: find terrain height under the camera
        float groundY = sampleGroundHeight(camera.position.x, camera.position.z) + EYE_HEIGHT;
        if (camera.position.y < groundY) {
            camera.position.y = groundY;
            vy = 0f;
        }

        camera.update();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    /**
     * Finds the highest non‐air block under (worldX, worldZ).
     */
    private float sampleGroundHeight(float worldX, float worldZ) {
        // Grab the chunk the player is above
        Chunk c = chunkGrid.getChunkAtWorld(worldX, worldZ);
        if (c == null) {
            // off‑world: assume ground at y=0
            return 0f;
        }

        // Convert to local chunk coords
        int localX = (int)Math.floor(worldX - c.originX);
        int localZ = (int)Math.floor(worldZ - c.originZ);

        // clamp just in case
        localX = Math.max(0, Math.min(localX, Chunk.WIDTH  - 1));
        localZ = Math.max(0, Math.min(localZ, Chunk.DEPTH  - 1));

        // use your existing surface‐scan
        int topY = c.getSurfaceHeight(localX, localZ);
        if (topY < 0) return c.originY;        // all air => base y
        return c.originY + topY;
    }
}

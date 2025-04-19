package net.fynn.javavoxelengine.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.chunk.ChunkGrid;

public class Player {
    private final PerspectiveCamera camera;
    private final FirstPersonCameraController controller;
    private final ChunkGrid chunkGrid;

    // physics
    private float vy = 0f;                 // current vertical velocity
    private static final float GRAVITY = -30f;  // units/sec²
    private static final float EYE_HEIGHT = 2f; // camera offset above ground

    private boolean isOnGround = false; // just to check if the player is on the ground atp

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
        controller.setDegreesPerPixel(0.07f);
        Gdx.input.setInputProcessor(controller);
    }

    public void update(float delta) {
        // 1) horizontal look & movement
        controller.update(delta);

        // 2) jump input
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE) && isOnGround) {
            vy = 12f;  // Jump power
            isOnGround = false;
        }

        // 3) apply gravity
        vy += GRAVITY * delta;
        camera.position.y += vy * delta;

        // 4) ground-collision
        float groundY = sampleGroundHeight((int)camera.position.x, (int)camera.position.z) + EYE_HEIGHT;
        if (camera.position.y < groundY) {
            camera.position.y = groundY;
            vy = 0f;
            isOnGround = true;
        }

        camera.update();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    /**
     * Finds the highest non‐air block under (worldX, worldZ).
     */
    private float sampleGroundHeight(int worldX, int worldZ) {
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

        // use existing surface‐scan
        int topY = c.getSurfaceHeight(localX, localZ);
        if (topY < 0) return c.originY;                 // all air => base y
        return c.originY + topY;
    }
}

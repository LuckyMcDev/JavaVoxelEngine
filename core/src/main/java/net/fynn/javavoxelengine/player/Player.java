package net.fynn.javavoxelengine.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import net.fynn.javavoxelengine.voxel.VoxelType;

public class Player {
    private final PerspectiveCamera camera;
    private final ChunkGrid chunkGrid;

    private float yaw = 0f;
    private float pitch = 0f;
    private final float mouseSensitivity = 0.1f;

    // physics
    private float vy = 0f;
    private static final float GRAVITY     = -30f;
    private static final float EYE_HEIGHT  = 2.5f;
    private static final float JUMP_POWER  = 12f;

    private boolean isOnGround = false;

    public Player(ChunkGrid chunkGrid) {
        this.chunkGrid = chunkGrid;

        camera = new PerspectiveCamera(80,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        camera.position.set(0f, 50f, 0f);
        camera.near = 0.1f;
        camera.far = 500f;
        camera.update();

        Gdx.input.setCursorCatched(true); // lock/hide mouse for FPS style
        Gdx.input.setInputProcessor(null); // disable built-in controller
    }

    public void update(float delta) {
        Vector3 oldPos = new Vector3(camera.position);

        // === Mouse look ===
        float deltaX = -Gdx.input.getDeltaX() * mouseSensitivity;
        float deltaY = -Gdx.input.getDeltaY() * mouseSensitivity;

        yaw += deltaX;
        pitch += deltaY;

        // Clamp pitch to avoid flipping
        pitch = MathUtils.clamp(pitch, -89f, 89f);

        // Recalculate direction vector
        Vector3 direction = new Vector3(
            MathUtils.cosDeg(pitch) * MathUtils.sinDeg(yaw),
            MathUtils.sinDeg(pitch),
            MathUtils.cosDeg(pitch) * MathUtils.cosDeg(yaw)
        ).nor();
        camera.direction.set(direction);

        // === Movement ===
        Vector3 forward = new Vector3(camera.direction.x, 0f, camera.direction.z).nor();
        Vector3 right = new Vector3(forward.z, 0f, -forward.x).nor();

        float moveSpeed = 20f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.add(forward.scl(moveSpeed * delta));
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.sub(forward.scl(moveSpeed * delta));
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.add(right.scl(moveSpeed * delta));
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.sub(right.scl(moveSpeed * delta));

        // === Jumping ===
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            vy = JUMP_POWER;
            isOnGround = false;
        }

        // === Gravity & Vertical motion ===
        vy += GRAVITY * delta;
        camera.position.y += vy * delta;

        float groundY = findSolidGroundY(camera.position.x, camera.position.z) + EYE_HEIGHT;
        if (camera.position.y < groundY) {
            camera.position.y = groundY;
            vy = 0f;
            isOnGround = true;
        }

        // === Optional smoothing for XZ movement ===
        Vector3 targetPos = new Vector3(camera.position);
        float alpha = MathUtils.clamp(delta * 15f, 0f, 1f);
        camera.position.x = MathUtils.lerp(oldPos.x, targetPos.x, alpha);
        camera.position.z = MathUtils.lerp(oldPos.z, targetPos.z, alpha);

        // === Update camera ===
        camera.update();

        // === Mouse Clicks ===
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            System.out.println("Left click: break block");
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            System.out.println("Right click: place block");
        }
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    private float findSolidGroundY(float worldX, float worldZ) {
        Chunk c = chunkGrid.getChunkAtWorld((int)worldX, (int)worldZ);
        if (c == null) return 0f;

        int localX = MathUtils.clamp((int)Math.floor(worldX - c.originX), 0, Chunk.WIDTH - 1);
        int localZ = MathUtils.clamp((int)Math.floor(worldZ - c.originZ), 0, Chunk.DEPTH - 1);

        int topY = c.getSurfaceHeight(localX, localZ);
        if (topY < 0) return c.originY;

        for (int y = topY; y >= 0; y--) {
            VoxelType type = c.getBlock(localX, y, localZ);
            if (isSolid(type)) {
                return c.originY + y;
            }
        }
        return c.originY;
    }

    private boolean isSolid(VoxelType type) {
        switch (type) {
            case AIR:
            case LEAVES_LIGHT:
            case LEAVES_DARK:
            case APPLE:
                return false;
            default:
                return true;
        }
    }
}

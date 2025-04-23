package net.fynn.javavoxelengine.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import net.fynn.javavoxelengine.voxel.VoxelType;

public class Player {
    private final PerspectiveCamera camera;
    private final FirstPersonCameraController controller;
    private final ChunkGrid chunkGrid;

    // physics
    private float vy = 0f;                       // aktuelle Vertikal-Geschwindigkeit
    private static final float GRAVITY     = -30f;// Einheiten/sec²
    private static final float EYE_HEIGHT  = 2f;  // Kamera-Offset über dem Boden
    private static final float JUMP_POWER  = 12f;

    private boolean isOnGround = false;

    public Player(ChunkGrid chunkGrid) {
        this.chunkGrid = chunkGrid;

        camera = new PerspectiveCamera(80,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        camera.position.set(0f, 50f, 0f); // initial hohe Y, wird im update korrigiert
        camera.near = 0.1f;
        camera.far  = 500f;
        camera.update();

        controller = new FirstPersonCameraController(camera);
        // Geschwindigkeit hochsetzen:
        controller.setVelocity(20f);
        controller.setDegreesPerPixel(0.07f);
        Gdx.input.setInputProcessor(controller);
    }

    /**
     * Muss jede Frame aufgerufen werden.
     * @param delta Zeit seit letztem Frame in Sekunden
     */
    public void update(float delta) {
        // 0) Alte Position merken (für horizontale Glättung)
        Vector3 oldPos = new Vector3(camera.position);

        // 1) Horizontalbewegung & Look
        controller.update(delta);

        // 2) Sprung
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            vy = JUMP_POWER;
            isOnGround = false;
        }

        // 3) Schwerkraft anwenden
        vy += GRAVITY * delta;
        camera.position.y += vy * delta;

        // 4) Ground-Collision (Y sofort korrigieren)
        float groundY = findSolidGroundY(
            camera.position.x,
            camera.position.z
        ) + EYE_HEIGHT;
        if (camera.position.y < groundY) {
            camera.position.y = groundY;  // Sofortiger Snap
            vy = 0f;
            isOnGround = true;
        }

        // 5) Nur X/Z glätten, Y bleibt (keine Verzögerung beim Aufspringen)
        Vector3 targetPos = new Vector3(camera.position);
        float alpha = MathUtils.clamp(delta * 15f, 0f, 1f);  // höhere Glätt-Rate
        float smoothX = MathUtils.lerp(oldPos.x, targetPos.x, alpha);
        float smoothZ = MathUtils.lerp(oldPos.z, targetPos.z, alpha);
        camera.position.x = smoothX;
        camera.position.z = smoothZ;
        // Y bleibt unverändert (direkt gecollided)

        // 6) Kameramatrix updaten
        camera.update();
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    /**
     * Findet das höchste SOLIDE Block-Y unter (worldX, worldZ).
     * Überspringt dabei alle Laub- oder Apfel-Blöcke.
     */
    private float findSolidGroundY(float worldX, float worldZ) {
        Chunk c = chunkGrid.getChunkAtWorld((int)worldX, (int)worldZ);
        if (c == null) return 0f;

        int localX = MathUtils.clamp(
            (int)Math.floor(worldX - c.originX),
            0, Chunk.WIDTH - 1
        );
        int localZ = MathUtils.clamp(
            (int)Math.floor(worldZ - c.originZ),
            0, Chunk.DEPTH - 1
        );

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

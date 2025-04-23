package net.fynn.javavoxelengine.challenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import net.fynn.javavoxelengine.voxel.VoxelType;

public class AppleCollector {

    /**
     * Eine Klasse, die den ray trace um einen apfel aufzusammeln durchführt.
     *
     * @param camera Die perspektive camera von dem spieler
     * @param chunkGrid Das jetzt benutzte Chunk Grid
     * @param challengeManager Der challenge manager, um den apple-check durchzuführen
     */
    public void tryCollectApple(PerspectiveCamera camera,
                                ChunkGrid chunkGrid,
                                ChallengeManager challengeManager) {
        // 1) Get ray direction from center of screen
        Ray ray = camera.getPickRay(
            Gdx.graphics.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f
        );

        // 2) Use the camera's actual position as the ray origin
        Vector3 origin    = new Vector3(camera.position);
        Vector3 direction = new Vector3(ray.direction).nor(); // ensure it's normalized

        // 3) March along the ray in unit steps (or smaller if you want finer resolution)
        final float maxDistance = 20f;
        final float stepSize    = 0.1f;   // e.g. 0.5 blocks per iteration
        Vector3 point = new Vector3();

        for (float t = stepSize; t <= maxDistance; t += stepSize) {
            point.set(origin).mulAdd(direction, t);


            VoxelType block = chunkGrid.getBlockFromWorld(point);
            if (block == VoxelType.APPLE && challengeManager.isActive()) {
                chunkGrid.setBlockFromWorld(point, VoxelType.AIR);
                challengeManager.addOneAppleAndCheckComplete();
                break;
            }
        }
    }

}

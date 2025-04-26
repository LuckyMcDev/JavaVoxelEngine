

package net.fynn.javavoxelengine.challenge;

/**
 * JavaVoxelEngine
 * @Copyright (c) 2025
 * Fynn Maier
 */

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import net.fynn.javavoxelengine.chunk.ChunkMesher;
import net.fynn.javavoxelengine.voxel.VoxelType;

import java.lang.annotation.Documented;

public class AppleCollector {

    /**
     * Eine Klasse, die den ray trace um einen apfel aufzusammeln durchführt.
     *
     * @param camera Die perspektive camera von dem spieler
     * @param chunkGrid Das jetzt benutzte Chunk Grid
     * @param challengeManager Der challenge manager, um den apple-check durchzuführen
     */
    public void tryCollectApple(PerspectiveCamera camera, ChunkGrid chunkGrid, ChallengeManager challengeManager) {

        /*
        // 1) Ray von der Bilschirmmitte
        Ray ray = camera.getPickRay(
            Gdx.graphics.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f
        );
         */

        // 2) Cam Pos als Origin nicht den Ray
        Vector3 origin    = new Vector3(camera.position);
        Vector3 direction = new Vector3(camera.direction).nor();

        // 3) Mit der setpSize am Ray entlang
        final float maxDistance = 20f;
        final float stepSize    = 0.1f; // 0.1 blocks / step
        Vector3 point = new Vector3();

        for (float t = stepSize; t <= maxDistance; t += stepSize) {

            point.set(origin).mulAdd(direction, t);

            VoxelType block = chunkGrid.getBlockFromWorld(point);
            if (block == VoxelType.APPLE && challengeManager.isActive()) {
                chunkGrid.setBlockFromWorld(point, VoxelType.AIR);

                // Get the chunk at the apple's position and invalidate it
                Chunk chunk = chunkGrid.getChunkAtWorld(point.x, point.z);
                if (chunk != null) {
                    ChunkMesher.invalidateChunkModel(chunk);
                }

                challengeManager.addOneAppleAndCheckComplete();
                break;
            }
        }
    }

}

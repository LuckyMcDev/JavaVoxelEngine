package net.fynn.javavoxelengine.challenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.collision.Ray;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.world.VoxelType;

public class AppleCollector {
    /** Call this when the player clicks to try and collect an apple. */
    public void tryCollectApple(PerspectiveCamera camera, Chunk chunk, ChallengeManager challengeManager) {
        Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);

        for (int distance = 0; distance < 10; distance++) {
            float checkX = ray.origin.x + ray.direction.x * distance;
            float checkY = ray.origin.y + ray.direction.y * distance;
            float checkZ = ray.origin.z + ray.direction.z * distance;

            int localX = Math.round(checkX - chunk.originX);
            int localY = Math.round(checkY - chunk.originY);
            int localZ = Math.round(checkZ - chunk.originZ);

            if (localX < 0 || localX >= Chunk.WIDTH
                || localY < 0 || localY >= Chunk.HEIGHT
                || localZ < 0 || localZ >= Chunk.DEPTH) {
                continue; // out of bounds → skip
            }

            VoxelType block = chunk.getBlock(localX, localY, localZ);

            if (block == VoxelType.APPLE) {
                if(challengeManager.isActive()) {
                    System.out.println("You hit an apple!");
                    chunk.setBlock(localX,localY,localZ,VoxelType.AIR);
                    challengeManager.addOneAppleAndCheckComplete();
                };
                break;
            }
        }
    }
}

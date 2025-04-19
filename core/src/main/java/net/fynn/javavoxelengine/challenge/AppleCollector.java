package net.fynn.javavoxelengine.challenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import net.fynn.javavoxelengine.voxel.VoxelType;

public class AppleCollector {
    /** Call this when the player clicks to try and collect an apple. */
    public void tryCollectApple(PerspectiveCamera camera, ChunkGrid chunkGrid, ChallengeManager challengeManager) {
        Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);

        System.out.println("Start ------------------------------");
        for (int distance = 0; distance < 20; distance++) {
            float checkX = ray.origin.x + ray.direction.x * distance;
            float checkY = ray.origin.y + ray.direction.y * distance;
            float checkZ = ray.origin.z + ray.direction.z * distance;

            Vector3 endPoint = new Vector3();

            Vector3 tmp = ray.getEndPoint(endPoint, distance);
            System.out.println("Endpoint: "+endPoint);

            VoxelType block  = chunkGrid.getBlockFromWorld(endPoint);

            if (block == VoxelType.APPLE) {
                if(challengeManager.isActive()) {
                    System.out.println("You hit an apple!");
                    chunkGrid.setBlockFromWorld(endPoint,VoxelType.AIR);
                    challengeManager.addOneAppleAndCheckComplete();
                };
                break;
            }
        }
    }
}

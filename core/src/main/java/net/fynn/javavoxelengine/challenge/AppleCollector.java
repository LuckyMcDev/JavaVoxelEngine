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
    public void tryCollectApple(PerspectiveCamera camera, ChunkGrid chunkGrid, ChallengeManager challengeManager) {
        Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);

        System.out.println("Start ------------------------------");
        System.out.println(ray);
        for (int distance = 0; distance < 20; distance++) {

            Vector3 endPoint = new Vector3();

            Vector3 tmp = ray.getEndPoint(endPoint, distance);
            System.out.println("Endpoint: "+endPoint);

            chunkGrid.setBlockFromWorld(endPoint, VoxelType.DIRT);

            VoxelType block  = chunkGrid.getBlockFromWorld(endPoint);

            if (block == VoxelType.APPLE) {
                if(challengeManager.isActive()) {
                    System.out.println("You hit an apple!");
                    System.out.println("HITPOINT: "+endPoint);
                    chunkGrid.setBlockFromWorld(endPoint,VoxelType.AIR);
                    challengeManager.addOneAppleAndCheckComplete();
                };
                break;
            }
        }
    }
}

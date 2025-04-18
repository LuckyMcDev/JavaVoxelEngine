package net.fynn.javavoxelengine.challenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import net.fynn.javavoxelengine.world.Chunk;
import net.fynn.javavoxelengine.world.VoxelType;

public class AppleCollector {
    /** Call this when the player clicks to try and collect an apple. */
    public boolean tryCollectApple(PerspectiveCamera camera, Chunk chunk) {
        if(!Gdx.input.justTouched()) return false;
        Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);

        Vector3 pos = new Vector3(ray.origin);
        Vector3 step = new Vector3(ray.direction).nor().scl(0.1f);

        for (int i = 0; i < 500; i++) {
            pos.add(step);

            int x = (int)Math.floor(pos.x);
            int y = (int)Math.floor(pos.y);
            int z = (int)Math.floor(pos.z);

            VoxelType voxel = chunk.getBlock(x,y,z);

            if (voxel == VoxelType.APPLE) {
                chunk.setBlock(x, y, z, VoxelType.AIR); // remove the apple
                return true; // collected successfully
            }
        }
        return false;
    }
}

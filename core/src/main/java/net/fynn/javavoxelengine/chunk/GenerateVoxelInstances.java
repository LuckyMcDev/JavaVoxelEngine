package net.fynn.javavoxelengine.chunk;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;

public class GenerateVoxelInstances {
    public static void gen(Chunk chunk, Array<ModelInstance> out) {
        Model m = ChunkMesher.getChunkModel(chunk);
        if (m != null) out.add(new ModelInstance(m));
    }
}

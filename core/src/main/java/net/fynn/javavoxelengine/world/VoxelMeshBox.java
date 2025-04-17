package net.fynn.javavoxelengine.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class VoxelMeshBox {
    public Vector3 position; // world position (x,y,z)
    public Vector3 size;     // size of the box (width,height,depth)
    public Color color;      // color of the voxel (taken from VoxelType)

    public VoxelMeshBox(Vector3 position, Vector3 size, Color color) {
        this.position = position;
        this.size = size;
        this.color = color;
    }
}

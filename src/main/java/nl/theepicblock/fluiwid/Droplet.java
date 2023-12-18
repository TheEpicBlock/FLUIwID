package nl.theepicblock.fluiwid;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class Droplet implements SpatialStructure.SpatialItem {
    private static final float SIZE = FishyBusiness.DROPLET_SIZE;
    public Vec3d position;
    public Vec3d velocity;

    public Droplet() {
        this.position = Vec3d.ZERO;
    }

    @Override
    public void updatePosition(Vec3d pos) {
        this.position = pos;
    }

    public void adjustForCollisions(Iterable<VoxelShape> collisions) {
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;
        var selfBox = this.getBox();

        // Calculate the max distance we can move
        var xMax = VoxelShapes.calculateMaxOffset(Direction.Axis.X, selfBox, collisions, x);
        var yMax = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, selfBox, collisions, y);
        var zMax = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, selfBox, collisions, z);

        var energy = 1f; // Incur loss of energy for each collision
        if (x != xMax) {
            x *= -1;
            energy *= FishyBusiness.COLLISION_ENERGY;
        }
        if (y != yMax) {
            y *= -1;
            energy *= FishyBusiness.COLLISION_ENERGY;
        }
        if (z != zMax) {
            z *= -1;
            energy *= FishyBusiness.COLLISION_ENERGY;
        }

        this.velocity = new Vec3d(x, y, z).multiply(energy);
    }

    public Box getBox() {
        return new Box(this.position.add(-SIZE/2, 0, -SIZE/2), this.position.add(SIZE/2, SIZE, SIZE/2));
    }

    public Box getBoundsWithMovement() {
        return this.getBox().stretch(this.velocity);
    }
}

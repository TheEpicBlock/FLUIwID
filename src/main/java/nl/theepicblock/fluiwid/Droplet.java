package nl.theepicblock.fluiwid;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class Droplet implements SpatialStructure.SpatialItem {
    private static final float SIZE = FishyBusiness.DROPLET_SIZE;
    public Vec3d position;
    /**
     * Measures in blocks per tick
     */
    public Vec3d velocity;

    public Droplet() {
        this.position = Vec3d.ZERO;
    }

    @Override
    public void updatePosition(Vec3d pos) {
        this.position = pos;
    }

    public void adjustForCollisions(Vec3d attractionPos, Iterable<VoxelShape> collisions) {
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;
        var selfBox = this.getBox();

        // Calculate the max distance we can move
        var xMax = VoxelShapes.calculateMaxOffset(Direction.Axis.X, selfBox, collisions, x);

        var scaleX = 1d;
        var scaleY = 1d;
        var scaleZ = 1d;
        var boosted = false;

        if (x != xMax) {
            if (Math.abs(x-xMax) < FishyBusiness.GRAVITY*3) {
                x = xMax;
                scaleY *= FishyBusiness.DRAG;
                scaleZ *= FishyBusiness.DRAG;
                if (this.position.y-attractionPos.y < 2) {
                    y += FishyBusiness.WALL_CLIMB_BOOST;
                    boosted = true;
                }
            } else {
                x *= -FishyBusiness.COLLISION_ENERGY;
            }
        }
        selfBox = selfBox.union(selfBox.offset(x, 0, 0));

        var zMax = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, selfBox, collisions, z);

        if (z != zMax) {
            if (Math.abs(z-zMax) < FishyBusiness.GRAVITY*3) {
                scaleX *= FishyBusiness.DRAG;
                scaleY *= FishyBusiness.DRAG;
                if (!boosted && this.position.y-attractionPos.y < 2) { // Prevent boost from being applied twice
                    y += FishyBusiness.WALL_CLIMB_BOOST;
                    boosted = true;
                }
                z = zMax;
            } else {
                z *= -FishyBusiness.COLLISION_ENERGY;
            }
        }
        selfBox = selfBox.union(selfBox.offset(0, 0, z));

        var yMax = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, selfBox, collisions, y);
        if (y != yMax) {
            if (Math.abs(y-yMax) < FishyBusiness.GRAVITY*3) {
                scaleX *= FishyBusiness.DRAG;
                y = yMax;
                scaleZ *= FishyBusiness.DRAG;
            } else {
                y *= -FishyBusiness.COLLISION_ENERGY;
            }
        }

        this.velocity = new Vec3d(x, y, z).multiply(scaleX, scaleY, scaleZ);
    }

    public Box getBox() {
        return new Box(this.position.add(-SIZE/2, 0, -SIZE/2), this.position.add(SIZE/2, SIZE, SIZE/2));
    }

    public Box getBoundsWithMovement() {
        return this.getBox().stretch(this.velocity).union(this.getBox().stretch(this.velocity.add(0, FishyBusiness.WALL_CLIMB_BOOST,0))).expand(0.05);
    }
}

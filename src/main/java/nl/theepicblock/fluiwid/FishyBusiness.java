package nl.theepicblock.fluiwid;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.Nullable;

public class FishyBusiness {
    public final static float DELTA_T = 1/20f;
    public final static float DROPLET_SIZE = 1/16f;
    public final static float GRAVITY = 2f*DELTA_T;
    public final static float COLLISION_ENERGY = 0.2f;
    public final static float WALL_CLIMB_BOOST = 2.1f*DELTA_T; // Blocks/tick²
    public final static float DRAG = 0.99f;
    /**
     * Keeps track of water particles
     */
    private final SpatialStructure<Droplet> particles = new SpatialStructure<>();
    private final PlayerEntity player;
    public Vec3d movementVec = Vec3d.ZERO;
    public Vec3d canonPosition = Vec3d.ZERO;
    public Vec3d camera = Vec3d.ZERO;
    public Vec3d prevCamera = Vec3d.ZERO;
    public Vec3d center = Vec3d.ZERO;
    private final RollingAverage cameraY = new RollingAverage(25);
    public int movingTicks = 0;

    public FishyBusiness(PlayerEntity player) {
        this.player = player;
        for (int i = 0; i < 50; i++) {
            var d = new Droplet();
            particles.insert(player.getPos(), d);
        }
        this.teleport(player.getPos());
    }

    public void clientTick() {
        // Center and camera logic
        this.prevCamera = this.camera;
        double x = 0,y = 0,z = 0;
        int i = 0;
        for (var droplet : this.particles) {
            x += droplet.position.x;
            y += droplet.position.y;
            z += droplet.position.z;
            i++;
        }
        y = y/i;
        var center = new Vec3d(x/i, y, z/i);
        var maxinBox = new Box(center.subtract(1, 2, 1), center.add(1, 2, 1));
        var avBox = new Box(center.subtract(0.2, 2, 0.2), center.add(0.2, 2, 0.2));
        var xzz = new Box(center.subtract(1, 3, 1), center.add(1, 1, 1));
        var mininBox = new Box(center.subtract(5/16f, 0, 5/16f), center.add(5/16f, 0.1, 5/16f)).offset(0, .5, 0);
        mininBox = subtractAll(mininBox, player.getWorld().getCollisions(player, mininBox));
        x = 0;
        z = 0;
        i = 0;
        double y2 = maxinBox.minY;
        for (var droplet : this.particles) {
            if (maxinBox.contains(droplet.position)) {
                y2 = Math.max(y2, droplet.position.y);
            }
            if (avBox.contains(droplet.position)) {
                x += droplet.position.x;
                z += droplet.position.z;
                i++;
            }
        }
        y += VoxelShapes.calculateMaxOffset(Direction.Axis.Y, mininBox, player.getWorld().getCollisions(player, xzz), -4) + 0.5;
        if (i != 0) {
            center = new Vec3d(x/i, y, z/i);
        }

        // Resist moving the canon position if the player isn't holding any input
        this.center = center;
        if (movementVec.horizontalLengthSquared() > 0.1 || center.subtract(canonPosition).lengthSquared() > 0.2*0.2) {
            movingTicks++;
        } else {
            movingTicks = Math.max(0, Math.min(20, movingTicks - 1));
        }
        if (movingTicks > 0) {
            var soothingFactor = smoothKernel(35, movingTicks);
            var delta = center.subtract(canonPosition);
            var smoothmentPos = canonPosition.add(movementVec.normalize().multiply(movementVec.dotProduct(delta)).multiply(0.9).add(delta.multiply(0.1))).withAxis(Direction.Axis.Y, center.y);
            var a = center.multiply(soothingFactor).add(smoothmentPos.multiply(1-soothingFactor));
            var sooth2 = smoothKernel(25, movingTicks);
            canonPosition = canonPosition.multiply(sooth2).add(a.multiply(1-sooth2));
        }

        var attractionPos = canonPosition.add(movementVec.multiply(0.3));

        this.center = attractionPos;
        boolean crammingThroughGap = false;
        if (anyCollide(player.getWorld(), player, new Box(attractionPos.subtract(0.1, 0, 0.1), attractionPos.add(0.1, 0.1, 0.1)))) {
            if (Streams.of(particles).anyMatch(p -> movementVec.dotProduct(p.position.subtract(canonPosition.add(movementVec.multiply(0.15)))) > 0)) {
                // This code should only activate if the player is going through a gap
                crammingThroughGap = true;
                // Boost movement
                attractionPos = attractionPos.add(movementVec.normalize().multiply(0.3));
                // Find y again with these values
                boolean a = false;
                maxinBox = new Box(attractionPos.subtract(0.1, 2, 0.1), attractionPos.add(0.1, 2, 0.1));
                for (var droplet : this.particles) {
                    if (maxinBox.contains(droplet.position)) {
                        if (!a) {
                            y = Double.POSITIVE_INFINITY;
                            y2 = Double.NEGATIVE_INFINITY;
                            a = true;
                        }
                        y = Math.min(y, droplet.position.y);
                        y2 = Math.max(y2, droplet.position.y);
                    }
                }
                if (a) {
                    attractionPos = attractionPos.withAxis(Direction.Axis.Y, y);
                }
            }
        }
        camera = canonPosition.withAxis(Direction.Axis.Y, cameraY.add(y2));
        attractionPos = attractionPos.add(0, 0.05, 0);

        // Fluid logic:
        for (var droplet : this.particles) {
            // Repulsion force between particles
            for (var droplet2 : this.particles) {
                if (droplet.velocity.length() > 1.5) break;
                var delta = droplet.position.subtract(droplet2.position);
                var length = Math.max(0, delta.multiply(1.5, 1, 1.5).length() - 0.17);
                var direction = delta.normalize();
                var force = smoothKernel(0.6f, length) * (crammingThroughGap ? (1.2 * DELTA_T) : (4.2f * DELTA_T));
                droplet.velocity = droplet.velocity.add(direction.multiply(force));
            }

            // Attraction force
            var delta = droplet.position.subtract(attractionPos.add(0,0.01f,0));
            var length = delta.length();
            var direction = delta.normalize();
            var force = Math.max(0.05, smoothKernel(7f, length)) * -(4f*DELTA_T);
            droplet.velocity = droplet.velocity.add(clampY(direction.multiply(force).multiply(2,1,2)));

            // Gravity
            // We cheat a little by removing gravity near the player (and especially under the player)
            var grav_nearness = (1-smoothKernel(0.5f, droplet.position.subtract(attractionPos.add(0, -1, 0)).multiply(1, 0.4, 1).length()));
            var grav = GRAVITY * grav_nearness;
            droplet.velocity = droplet.velocity.add(0, -grav, 0);

            // General velocity dampening
            droplet.velocity = droplet.velocity.multiply(Math.max(0.9, 1-(smoothKernel(3f, droplet.position.subtract(attractionPos.add(0, 0.5, 0)).multiply(1, 0.5, 1).length()))*droplet.velocity.lengthSquared()));

            var dropletClimbOrigin = crammingThroughGap ? attractionPos.subtract(0, 2, 0) : attractionPos;
            droplet.adjustForCollisions(dropletClimbOrigin, player.getWorld().getCollisions(player, droplet.getBoundsWithMovement()));
        }
        for (var droplet : this.particles) {
            droplet.position = droplet.position.add(droplet.velocity.multiply(0.2));
        }

        // Sync pos
        this.player.setPos(canonPosition.x, canonPosition.y, canonPosition.z);
        this.player.setVelocity(0,0,0);
    }

    public void teleport(Vec3d pos) {
        for (var droplet : this.particles) {
            droplet.position = pos;
            droplet.velocity = new Vec3d(Math.random()*0.1f-0.05f, Math.random()*0.1f-0.05f, Math.random()*0.1f-0.05f);
        }
        this.center = pos;
        this.canonPosition = pos;
        this.prevCamera = pos;
        this.camera = pos;
        this.cameraY.setAll(pos.y);
        this.movingTicks = 0;
    }

    /**
     * Thank you Sebastian Lague xoxo
     */
    public static double smoothKernel(double radius, double dst) {
        var x = dst / radius;
        var v = Math.max(0, 1 - x*x);
        return v*v*v;
    }

    public static Vec3d clampY(Vec3d in) {
        if (in.y < 0) {
            return in.withAxis(Direction.Axis.Y, 0);
        }
        return in;
    }

    public SpatialStructure<Droplet> getDroplets() {
        return particles;
    }

    /**
     * Subtracts all voxelshape boxes from {@code a} (only horizontally)
     */
    private Box subtractAll(Box a, Iterable<VoxelShape> collisions) {
        for (var i : collisions) {
            for (var box : i.getBoundingBoxes()) {
                if (!a.intersects(box)) continue;
                if (a.maxX > box.minX && a.minX < box.minX) {
                    a = new Box(a.minX, a.minY, a.minZ, box.minX, a.maxY, a.maxY);
                } else if (a.maxZ > box.minZ && a.minZ < box.minZ) {
                    a = new Box(a.minX, a.minY, a.minZ, a.maxX, box.maxY, a.maxY);
                } else if (a.minX < box.maxX && a.maxX > box.maxX) {
                    a = new Box(box.maxX, a.minY, a.minZ, a.maxX, a.maxY, a.maxY);
                } else if (a.minZ < box.maxZ && a.maxZ > box.maxZ) {
                    a = new Box(a.minX, a.minY, box.maxZ, a.maxX, a.maxY, a.maxY);
                }
            }
        }
        return a;
    }

    static private boolean anyCollide(CollisionView view, @Nullable Entity entity, Box box) {
        BlockCollisionSpliterator<VoxelShape> blockCollisionSpliterator = new BlockCollisionSpliterator<>(view, entity, box, false, (pos, voxelShape) -> voxelShape);

        while(blockCollisionSpliterator.hasNext()) {
            if (!blockCollisionSpliterator.next().isEmpty()) {
                return true;
            }
        }

        return false;
    }
}

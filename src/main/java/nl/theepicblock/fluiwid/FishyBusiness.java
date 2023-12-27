package nl.theepicblock.fluiwid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

public class FishyBusiness {
    public final static float DELTA_T = 1/20f;
    public final static float DROPLET_SIZE = 1/16f;
    public final static float GRAVITY = 2f*DELTA_T;
    public final static float COLLISION_ENERGY = 0.2f;
    public final static float WALL_CLIMB_BOOST = 1.9f*DELTA_T; // Blocks/tick²
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
    public int movingTicks = 0;

    public FishyBusiness(PlayerEntity player) {
        this.player = player;
        for (int i = 0; i < 50; i++) {
            var d = new Droplet();
            d.velocity = new Vec3d(Math.random()*0.1f-0.05f, Math.random()*0.1f-0.05f, Math.random()*0.1f-0.05f);
            particles.insert(player.getPos(), d);
        }
    }

    public void tick() {
        // Center and camera logic
        this.prevCamera = this.camera;
        var oldCamDeltaY = camera.y - canonPosition.y;
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
        var maxinBox = new Box(center.subtract(0.5, 2, 0.5), center.add(0.5, 2, 0.5));
        var xzz = new Box(center.subtract(1, 3, 1), center.add(1, 1, 1));
        var mininBox = new Box(center.subtract(5/16f, 0, 5/16f), center.add(5/16f, 0.1, 5/16f)).offset(0, .5, 0);
        x = 0;
        z = 0;
        i = 0;
        double y2 = maxinBox.minY;
        for (var droplet : this.particles) {
            if (maxinBox.contains(droplet.position)) {
                y2 = Math.max(y2, droplet.position.y);
                x += droplet.position.x;
                z += droplet.position.z;
                i++;
            }
        }
        y += VoxelShapes.calculateMaxOffset(Direction.Axis.Y, mininBox, player.getWorld().getCollisions(player, xzz), -4) + 0.5;
        if (i != 0) {
            center = new Vec3d(x/i, y, z/i);
        }
        var newCamDeltaY = y2-y;

        // Resist moving the canon position if the player isn't holding any input
        this.center = center;
        if (movementVec.horizontalLengthSquared() > 0.1 || center.subtract(canonPosition).lengthSquared() > 1) {
            movingTicks++;
        } else {
            movingTicks = Math.max(0, Math.min(20, movingTicks - 1));
        }
        if (movingTicks > 0) {
            var soothingFactor = smoothKernel(30, movingTicks);
            canonPosition = center.multiply(1-soothingFactor).add(canonPosition.multiply(soothingFactor));
        }
        camera = canonPosition.withAxis(Direction.Axis.Y, y + newCamDeltaY * 0.01 + oldCamDeltaY * 0.99);

        var attractionPos = canonPosition.add(movementVec.normalize().multiply(0.3)).add(0, 0.05, 0);

        for (var droplet : this.particles) {
            // Repulsion force between particles
            for (var droplet2 : this.particles) {
                var delta = droplet.position.subtract(droplet2.position);
                var length = Math.max(0, delta.multiply(1, 0.9, 1).length() - 0.12);
                var direction = delta.normalize();
                var force = smoothKernel(0.7f, length) * (1f * DELTA_T);
                droplet.velocity = droplet.velocity.add(direction.multiply(force));
            }

            // Attraction force
            var delta = droplet.position.subtract(attractionPos.add(0,0.23f,0));
            var length = delta.length();
            var direction = delta.normalize();
            var force = smoothKernel(7f, length) * -(4f*DELTA_T);
            // TODO add a minimum pull to help prevent water yeeting out of orbit
            droplet.velocity = droplet.velocity.add(clampY(direction.multiply(force).multiply(2,1,2)));

            // Gravity
            // We cheat a little by removing gravity near the player (and especially under the player)
            var grav_nearness = (1-smoothKernel(0.5f, droplet.position.subtract(attractionPos.add(0, -1, 0)).multiply(1, 0.4, 1).length()));
            var grav = GRAVITY * grav_nearness;
            droplet.velocity = droplet.velocity.add(0, -grav, 0);

            // General velocity dampening
            droplet.velocity = droplet.velocity.multiply(Math.max(0.9, 1-(smoothKernel(3f, droplet.position.subtract(attractionPos.add(0, 0.5, 0)).multiply(1, 0.5, 1).length()))*droplet.velocity.lengthSquared()));

            droplet.adjustForCollisions(player.getWorld().getCollisions(player, droplet.getBoundsWithMovement()));
        }
        for (var droplet : this.particles) {
            droplet.position = droplet.position.add(droplet.velocity.multiply(0.2));
        }

        // Sync pos
        this.player.setPosition(canonPosition);
    }

    public void teleport(Vec3d pos) {
        // TODO gosh this is a lot of force
        for (var droplet : this.particles) {
            droplet.position = pos;
        }
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
}

package nl.theepicblock.fluiwid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;

public class FishyBusiness {
    public final static float DELTA_T = 1/20f;
    public final static float DROPLET_SIZE = 1/16f;
    public final static float GRAVITY = 2f*DELTA_T;
    public final static float COLLISION_ENERGY = 0.5f;
    public final static float DRAG = 0.98f;
    /**
     * Keeps track of water particles
     */
    private final SpatialStructure<Droplet> particles = new SpatialStructure<>();
    private final PlayerEntity player;

    public FishyBusiness(PlayerEntity player) {
        this.player = player;
        for (int i = 0; i < 50; i++) {
            var d = new Droplet();
            d.velocity = new Vec3d(Math.random()*0.1f-0.05f, Math.random()*0.1f-0.05f, Math.random()*0.1f-0.05f);
            particles.insert(player.getPos(), d);
        }
    }

    public void tick() {
        for (var droplet : this.particles) {
            // Repulsion force between particles
            for (var droplet2 : this.particles) {
                var delta = droplet.position.subtract(droplet2.position);
                var length = delta.length();
                var direction = delta.normalize();
                var force = smoothKernel(0.6f, length) * 0.08;
                droplet.velocity = droplet.velocity.add(direction.multiply(force));
            }

            // Attraction force
            var delta = droplet.position.subtract(player.getPos().add(0,1.7,0));
            var length = delta.length();
            var direction = delta.normalize();
            var force = smoothKernel(7f, length) * -0.12;
            droplet.velocity = droplet.velocity.add(direction.multiply(force));

            // Gravity
            // We cheat a little by removing gravity near the player
            var grav_nearness = (1-smoothKernel(0.5f, droplet.position.subtract(player.getPos().add(0, 0.5, 0)).multiply(1, 0.5, 1).length()));
            var grav = GRAVITY * grav_nearness;
            droplet.velocity = droplet.velocity.add(0, -grav, 0);

            // General velocity dampening
            droplet.velocity = droplet.velocity.multiply(Math.max(0.01, 1-(smoothKernel(3f, droplet.position.subtract(player.getPos().add(0, 0.5, 0)).multiply(1, 0.5, 1).lengthSquared()))*droplet.velocity.lengthSquared()));

            droplet.adjustForCollisions(player.getWorld().getCollisions(player, droplet.getBoundsWithMovement()));
        }
        for (var droplet : this.particles) {
            droplet.position = droplet.position.add(droplet.velocity.multiply(0.2));
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

    @Debug
    public Iterable<Droplet> getDroplets() {
        return particles;
    }
}

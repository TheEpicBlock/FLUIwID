package nl.theepicblock.fluiwid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;

public class FishyBusiness {
    public final static float DELTA_T = 1/20f;
    public final static float DROPLET_SIZE = 1/16f;
    public final static float GRAVITY = 4.2f*DELTA_T;
    public final static float COLLISION_ENERGY = 0.5f;
    public final static float DRAG = 0.95f;
    /**
     * Keeps track of water particles
     */
    private final SpatialStructure<Droplet> particles = new SpatialStructure<>();
    private final PlayerEntity player;

    public FishyBusiness(PlayerEntity player) {
        this.player = player;
        for (int i = 0; i < 50; i++) {
            var d = new Droplet();
            d.velocity = new Vec3d(Math.random()*0.1f-0.05f, 0, Math.random()*0.1f-0.05f);
            particles.insert(player.getPos(), d);
        }
    }

    public void tick() {
        var totalBox = this.particles.getBoundingBox();
        for (var droplet : this.particles) {
            droplet.velocity = droplet.velocity.add(0, -GRAVITY, 0);
            droplet.adjustForCollisions(player.getWorld().getCollisions(player, totalBox.expand(1)));
            droplet.position = droplet.position.add(droplet.velocity);
        }
    }

    @Debug
    public Iterable<Droplet> getDroplets() {
        return particles;
    }
}

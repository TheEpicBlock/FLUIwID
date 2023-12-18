package nl.theepicblock.fluiwid;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class FishyBusiness {
    /**
     * Keeps track of water particles
     */
    private final SpatialStructure<Droplet> particles = new SpatialStructure<>();

    public FishyBusiness() {
        for (int i = 0; i < 50; i++) {
            particles.insert(Vec3d.ZERO, new Droplet());
        }
    }
}

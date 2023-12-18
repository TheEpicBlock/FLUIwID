package nl.theepicblock.fluiwid;

import net.minecraft.util.math.Vec3d;

public class Droplet implements SpatialStructure.SpatialItem {
    private Vec3d position;

    public Droplet() {
        this.position = Vec3d.ZERO;
    }

    @Override
    public void updatePosition(Vec3d pos) {
        this.position = pos;
    }
}

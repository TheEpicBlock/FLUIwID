package nl.theepicblock.fluiwid;

import net.minecraft.util.math.Box;
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

    public Box getBox() {
        return new Box(this.position.add(-1/32f, 0, -1/32f), this.position.add(1/32f, 2/32f, 1/32f));
    }
}

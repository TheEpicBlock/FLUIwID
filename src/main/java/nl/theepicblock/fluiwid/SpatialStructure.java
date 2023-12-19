package nl.theepicblock.fluiwid;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

// TODO replace with an actually performant spatial structure
public class SpatialStructure<T extends SpatialStructure.SpatialItem> implements Iterable<T> {
    private final List<T> backend = new ArrayList<>();

    public SpatialStructure() {
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return backend.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return backend.spliterator();
    }

    public Box getBoundingBox() {
        var minX = Double.MAX_VALUE;
        var minY = Double.MAX_VALUE;
        var minZ = Double.MAX_VALUE;
        var maxY = Double.MIN_VALUE;
        var maxZ = Double.MIN_VALUE;
        var maxX = Double.MIN_VALUE;
        for (var v : this) {
            var b = v.getBox();
            minX = Math.min(minX, b.minX);
            minY = Math.min(minY, b.minY);
            minZ = Math.min(minZ, b.minZ);
            maxX = Math.max(maxX, b.maxX);
            maxY = Math.max(maxY, b.maxY);
            maxZ = Math.max(maxZ, b.maxZ);
        }
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void insert(Vec3d position, T item) {
        item.updatePosition(position);
        backend.add(item);
    }

    public interface SpatialItem {
        void updatePosition(Vec3d pos);
        Box getBox();
        Box getBoundsWithMovement();
    }
}

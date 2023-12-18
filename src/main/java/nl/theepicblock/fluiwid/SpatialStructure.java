package nl.theepicblock.fluiwid;

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

    public void insert(Vec3d position, T item) {
        item.updatePosition(position);
        backend.add(item);
    }

    public interface SpatialItem {
        void updatePosition(Vec3d pos);
    }
}

package nl.theepicblock.fluiwid;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class KDNode<T extends KDItem> {
    public Direction.Axis splitAxis;
    public double split;
    /**
     * Coordinates < split
     * (Typeof T | KDNode<T>)
     * :
     * import net.minecraft.util.math.Direction;
     * <p>
     * import java.util.Comparator;
     * import java.util.List;
     * import java.util.function.Consumer;
     * <p>
     * public class KDNode<T extends KDTree.Item> {
     * public Direction.Axis splitAxis;
     * public double split;
     * /**
     * Coordinates <= split
     * (Typeof T | KDNode<T>)
     */
    public Object left;
    /**
     * Coordinates >= split
     * (Typeof T | KDNode<T>)
     */
    public Object right;

    /**
     * @return Typeof T | KDNode<T>
     */
    protected static <T extends KDItem> Object construct(Direction.Axis splitAxis, List<T> list) {
        if (list.size() == 1) {
            return list.get(0);
        }

        list.sort(Comparator.comparing(node -> node.getPos().getComponentAlongAxis(splitAxis)));

        var midIndex = list.size() / 2;
        double median = list.get(midIndex).getPos().getComponentAlongAxis(splitAxis);
//        while (midIndex >= 1 && list.get(midIndex - 1).getPos().getComponentAlongAxis(splitAxis) == median) {
//            midIndex--;
//        }

        var node = new KDNode<T>();
        node.splitAxis = splitAxis;
        node.split = median;

        var nextAxis = switch (splitAxis) {
            case X -> Direction.Axis.Y;
            case Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.X;
        };
        node.left = construct(nextAxis, list.subList(0, midIndex));
        node.right = construct(nextAxis, list.subList(midIndex, list.size()));

        return node;
    }

    @SuppressWarnings("unchecked")
    protected void forEach(Consumer<? super T> func) {
        if (this.left instanceof KDNode<?> node) {
            ((KDNode<T>)node).forEach(func);
        } else {
            func.accept((T)this.left);
        }
        if (this.right instanceof KDNode<?> node) {
            ((KDNode<T>)node).forEach(func);
        } else {
            func.accept((T)this.right);
        }
    }
}
package nl.theepicblock.fluiwid;

import com.google.common.collect.Iterators;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class KDTree<T extends KDItem> implements Iterable<T> {
    public Object rootNode; // Typeof T | KDNode<T> | null

    public KDTree() {
    }

    public static <T extends KDItem> KDTree<T> construct(List<T> list) {
        var tree = new KDTree<T>();
        if (list.isEmpty()) {
            tree.rootNode = null;
        } else {
            tree.rootNode = KDNode.construct(Direction.Axis.X, list);
        }
        return tree;
    }

    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super T> func) {
        if (this.rootNode instanceof KDNode<?> node) {
            ((KDNode<T>)node).forEach(func);
        } else if (this.rootNode != null) {
            func.accept((T)this.rootNode);
        }
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

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        if (this.rootNode == null) return Collections.emptyIterator();
        if (this.rootNode instanceof KDNode<?> node) {
            return new TreeIterator<>((KDNode<T>)node);
        }
        return Iterators.singletonIterator((T)this.rootNode);
    }

    @FunctionalInterface
    public interface Object2BooleanFunction<T> {
        boolean apply(T f);
    }

    public void rangeSearch(Box range, Object2BooleanFunction<T> consumer) {
        rangeSearch(this.rootNode, range, consumer);
    }

    private static <T extends KDItem> boolean rangeSearch(Object maybeNode, Box range, Object2BooleanFunction<T> consumer) {
        if (maybeNode instanceof KDNode<?> node) {
            double min;
            double max;
            switch (node.splitAxis) {
                case X -> {
                    min = range.minX;
                    max = range.maxX;
                }
                case Y -> {
                    min = range.minY;
                    max = range.maxY;
                }
                case Z -> {
                    min = range.minZ;
                    max = range.maxZ;
                }
                default -> throw new RuntimeException("What, who added a fourth dimension to Minecraft. Wtf");
            }

            if (min <= node.split) {
                var v = rangeSearch(node.left, range, consumer);
                if (v) return true;
            }
            if (max >= node.split) {
                var v = rangeSearch(node.right, range, consumer);
                if (v) return true;
            }
        } else if (maybeNode != null) {
            return consumer.apply((T)maybeNode);
        }
        return false;
    }

    public static class TreeIterator<T extends KDItem> implements Iterator<T> {
        private final Stack<KDNode<T>> nodes = new Stack<>();
        private boolean hasDoneLeft = false;

        public TreeIterator(KDNode<T> node) {
            this.nodes.add(node);
        }

        @Override
        public boolean hasNext() {
            return !nodes.isEmpty();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            var nxt = hasDoneLeft ? nodes.peek().right : nodes.peek().left;
            if (nxt instanceof KDNode<?> node) {
                hasDoneLeft = false;
                nodes.add((KDNode<T>)node);
                return next();
            } else {
                if (hasDoneLeft) {
                    KDNode<T> n;
                    do {
                        n = nodes.pop();
                    } while (!nodes.isEmpty() && nodes.peek().right == n);
                } else {
                    hasDoneLeft = true;
                }
                return (T)nxt;
            }
        }
    }
}
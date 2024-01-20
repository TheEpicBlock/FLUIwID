package nl.theepicblock.fluiwid.client;

import com.mojang.blaze3d.platform.GlDebugInfo;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.*;
import org.joml.Vector3f;

import java.util.ArrayList;

public class FluiwidRenderer {
    private static final boolean NVIDIA = GlDebugInfo.getRenderer().toLowerCase().contains("nvidia");
    private static final FluidState FLUID = Fluids.WATER.getStill(false);
    private static final float VOXEL_SIZE = 1/8f;
    private static final double DROPLET_RADIUS = 0.15d;
    private static final double MAX_DROPLET_RADIUS = 3*DROPLET_RADIUS;
    private final SpatialStructure<Droplet> particles;

    public FluiwidRenderer(SpatialStructure<Droplet> particles) {
        this.particles = particles;
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack matrix, Camera camera, World world) {
        var clusters = new ArrayList<DropletCluster>();
        d:
        for (var droplet : particles) {
            var dropletB = droplet.getBox().expand(MAX_DROPLET_RADIUS);
            for (var cluster : clusters) {
                if (dropletB.intersects(cluster.bounds)) {
                    cluster.bounds = cluster.bounds.union(dropletB);
                    cluster.droplets.add(droplet);
                    continue d;
                }
            }
            // Can't join any cluster
            var l2 = new ArrayList<Droplet>();
            l2.add(droplet);
            clusters.add(new DropletCluster(dropletB, l2));
        }
        for (var i = 0; i < clusters.size(); i++) {
            for (var j = i+1; j < clusters.size(); j++) {
                var a = clusters.get(i);
                var b = clusters.get(j);
                if (a.bounds.intersects(b.bounds)) {
                    a.bounds = a.bounds.union(b.bounds);
                    a.droplets.addAll(b.droplets);
                    clusters.remove(j);
                    break;
                }
            }
        }
        for (var cluster : clusters) {
            render(vertexConsumerProvider, matrix, camera, world, cluster);
        }
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack matrix, Camera camera, World world, DropletCluster cluster) {
//        if (true) {return;}
        var tree = KDTree.construct(cluster.droplets);
        CACHE = new Object2BooleanOpenHashMap<>();
        if (shouldThisBitchAssPositionContainWaterYesOrNo(camera.getPos(), tree)) {
            // Don't render if the camera's inside the water, it has icky artifacts
            return;
        }
        var layer = NVIDIA ? TexturedRenderLayers.getEntitySolid() : TexturedRenderLayers.getEntityTranslucentCull();
        var buf = vertexConsumerProvider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull());
        var fluidR = (nl.theepicblock.fluiwid.client.mixin.FluidRenderer)new FluidRenderer();

        var bounds = cluster.bounds;
        var pixelBounds = multiply(bounds, 1 / VOXEL_SIZE);

        var handler = FluidRenderHandlerRegistry.INSTANCE.get(FLUID.getFluid());
        var sprite = handler.getFluidSprites(null, null, FLUID)[0];
        var u = sprite.getFrameU(0.0f);
        var v = sprite.getFrameV(0.0f);
        var lightCache = new Long2IntOpenHashMap();
        var biomeCache = new Long2IntOpenHashMap();
        var worldBrightness = world.getBrightness(Direction.UP, true);

        var blockpos = new BlockPos.Mutable();
        for (var x = (int)pixelBounds.minX; x <= (int)pixelBounds.maxX; x++) {
            for (var y = (int)pixelBounds.minY; y <= (int)pixelBounds.maxY; y++) {
                for (var z = (int)pixelBounds.minZ; z <= (int)pixelBounds.maxZ; z++) {
                    // Convert pixel coords back to world
                    var coords = new Vec3d(x*VOXEL_SIZE, y*VOXEL_SIZE, z*VOXEL_SIZE).add(VOXEL_SIZE/2, VOXEL_SIZE/2, VOXEL_SIZE/2);
                    blockpos.set(coords.x, coords.y, coords.z);

                    if (shouldThisBitchAssPositionContainWaterYesOrNo(coords, tree)) {
                        for (var direction : Direction.values()) {
                            var c2 = coords.add(Vec3d.of(direction.getVector()).multiply(VOXEL_SIZE));
                            if (!shouldThisBitchAssPositionContainWaterYesOrNo(c2, tree)) {
                                // We should render a side here!
                                int light = lightCache.computeIfAbsent(blockpos.asLong(), l ->
                                        fluidR.invokeGetLight(world, blockpos.down())
                                );
                                int biomeColour = biomeCache.computeIfAbsent(blockpos.asLong(), l ->
                                        BiomeColors.getWaterColor(world, blockpos)
                                );
                                float biomeRed = (float)(biomeColour >> 16 & 0xFF) / 255.0F;
                                float biomeGreen = (float)(biomeColour >> 8 & 0xFF) / 255.0F;
                                float biomeBlue = (float)(biomeColour & 0xFF) / 255.0F;

                                float red = worldBrightness * biomeRed;
                                float green = worldBrightness * biomeGreen;
                                float blue = worldBrightness * biomeBlue;

                                var q = direction.getRotationQuaternion();
                                var quadCenter = coords.add(Vec3d.of(direction.getVector()).multiply(VOXEL_SIZE/2)).subtract(camera.getPos()).toVector3f();
                                var corner1 = q.transform(new Vector3f(VOXEL_SIZE/2, 0, -VOXEL_SIZE/2)).add(quadCenter);
                                var corner2 = q.transform(new Vector3f(-VOXEL_SIZE/2, 0, -VOXEL_SIZE/2)).add(quadCenter);
                                var corner3 = q.transform(new Vector3f(-VOXEL_SIZE/2, 0, VOXEL_SIZE/2)).add(quadCenter);
                                var corner4 = q.transform(new Vector3f(VOXEL_SIZE/2, 0, VOXEL_SIZE/2)).add(quadCenter);
//                                DebugRenderer.drawBox(matrix, vertexConsumerProvider, new Box(new Vec3d(corner2), new Vec3d(corner4)), 1, 1, 1, 1);

                                buf.vertex(matrix.peek().getPositionMatrix(), corner1.x, corner1.y, corner1.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .overlay(OverlayTexture.DEFAULT_UV)
                                        .light(light)
                                        .normal(matrix.peek().getNormalMatrix(), direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ())
                                        .next();
                                buf.vertex(matrix.peek().getPositionMatrix(), corner2.x, corner2.y, corner2.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .overlay(OverlayTexture.DEFAULT_UV)
                                        .light(light)
                                        .normal(matrix.peek().getNormalMatrix(), direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ())
                                        .next();
                                buf.vertex(matrix.peek().getPositionMatrix(), corner3.x, corner3.y, corner3.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .overlay(OverlayTexture.DEFAULT_UV)
                                        .light(light)
                                        .normal(matrix.peek().getNormalMatrix(), direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ())
                                        .next();
                                buf.vertex(matrix.peek().getPositionMatrix(), corner4.x, corner4.y, corner4.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .overlay(OverlayTexture.DEFAULT_UV)
                                        .light(light)
                                        .normal(matrix.peek().getNormalMatrix(), direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ())
                                        .next();
                            }
                        }
                    }
                }
            }
        }
    }

    private static Object2BooleanOpenHashMap<Vec3d> CACHE = new Object2BooleanOpenHashMap<>();

    private static Double WEIGHT = 0d;
    private boolean shouldThisBitchAssPositionContainWaterYesOrNo(Vec3d pos, KDTree<Droplet> nodes) {
        return CACHE.computeIfAbsent(pos, (a) -> {
            WEIGHT = 0d;
            rangeSearch(nodes.rootNode, pos, new Box(pos, pos).expand(MAX_DROPLET_RADIUS));
            return WEIGHT >= 1;
        });
    }

    // Copy-pasted from its definition in KDTree and manually inlined for marginal speed benefit
    private static <T extends KDItem> boolean rangeSearch(Object maybeNode, Vec3d pos, Box range) {
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
                var v = rangeSearch(node.left, pos, range);
                if (v) return true;
            }
            if (max >= node.split) {
                var v = rangeSearch(node.right, pos, range);
                if (v) return true;
            }
        } else if (maybeNode != null) {
            var lengthSq = pos.subtract(((Droplet)maybeNode).position).lengthSquared();
            WEIGHT += (DROPLET_RADIUS * DROPLET_RADIUS) / (lengthSq);
            return WEIGHT >= 1;
        }
        return false;
    }

    private static Box multiply(Box in, double n) {
        return new Box(in.minX * n, in.minY * n, in.minZ * n, in.maxX * n, in.maxY * n, in.maxZ * n);
    }

    public static final class DropletCluster {
        private Box bounds;
        private final ArrayList<Droplet> droplets;

        public DropletCluster(Box bounds, ArrayList<Droplet> droplets) {
            this.bounds = bounds;
            this.droplets = droplets;
        }
    }

    static {
        if (NVIDIA) {
            Fluiwid.LOGGER.warn("Nvidia detected, water will be solid");
        } else {
            Fluiwid.LOGGER.debug("No Nvidia detected");
        }
    }
}

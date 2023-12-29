package nl.theepicblock.fluiwid.client;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.Droplet;
import nl.theepicblock.fluiwid.KDTree;
import nl.theepicblock.fluiwid.SpatialStructure;
import org.joml.Vector3f;

import java.util.ArrayList;

public class FluiwidRenderer {
    private static final FluidState FLUID = Fluids.WATER.getStill(false);
    private static final double DROPLET_RADIUS = 0.2d;
    private static final double MAX_DROPLET_RADIUS = 3*DROPLET_RADIUS;
    private final SpatialStructure<Droplet> particles;

    public FluiwidRenderer(SpatialStructure<Droplet> particles) {
        this.particles = particles;
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack matrix, Camera camera, World world) {
        var clusters = new ArrayList<DropletCluster>();
        d:
        for (var droplet : particles) {
            var dropletB = droplet.getBox().expand(MAX_DROPLET_RADIUS-DROPLET_RADIUS);
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
        for (var cluster : clusters) {
            render(vertexConsumerProvider, matrix, camera, world, cluster);
        }
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack matrix, Camera camera, World world, DropletCluster cluster) {
//        if (true) {return;}
        var tree = KDTree.construct(cluster.droplets);
        var buf = vertexConsumerProvider.getBuffer(RenderLayers.getFluidLayer(FLUID));
        var fluidR = (nl.theepicblock.fluiwid.client.mixin.FluidRenderer)new FluidRenderer();

        var bounds = cluster.bounds;
        var pixelBounds = multiply(bounds, 16);

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
                    var coords = new Vec3d(x/16f, y/16f, z/16f).add(1/32f, 1/32f, 1/32f);
                    blockpos.set(coords.x, coords.y, coords.z);

                    if (shouldThisBitchAssPositionContainWaterYesOrNo(coords, tree)) {
                        for (var direction : Direction.values()) {
                            var c2 = coords.add(Vec3d.of(direction.getVector()).multiply(1/16f));
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
                                var quadCenter = coords.add(Vec3d.of(direction.getVector()).multiply(1/32f)).subtract(camera.getPos()).toVector3f();
                                var corner1 = q.transform(new Vector3f(1/32f, 0, -1/32f)).add(quadCenter);
                                var corner2 = q.transform(new Vector3f(-1/32f, 0, -1/32f)).add(quadCenter);
                                var corner3 = q.transform(new Vector3f(-1/32f, 0, 1/32f)).add(quadCenter);
                                var corner4 = q.transform(new Vector3f(1/32f, 0, 1/32f)).add(quadCenter);
//                                DebugRenderer.drawBox(matrix, vertexConsumerProvider, new Box(new Vec3d(corner2), new Vec3d(corner4)), 1, 1, 1, 1);

                                buf.vertex(matrix.peek().getPositionMatrix(), corner1.x, corner1.y, corner1.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .light(light)
                                        .normal(0.0F, 1.0F, 0.0F)
                                        .next();
                                buf.vertex(matrix.peek().getPositionMatrix(), corner2.x, corner2.y, corner2.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .light(light)
                                        .normal(0.0F, 1.0F, 0.0F)
                                        .next();
                                buf.vertex(matrix.peek().getPositionMatrix(), corner3.x, corner3.y, corner3.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .light(light)
                                        .normal(0.0F, 1.0F, 0.0F)
                                        .next();
                                buf.vertex(matrix.peek().getPositionMatrix(), corner4.x, corner4.y, corner4.z)
                                        .color(red, green, blue, 1.0F)
                                        .texture(u, v)
                                        .light(light)
                                        .normal(0.0F, 1.0F, 0.0F)
                                        .next();
                            }
                        }
                    }
                }
            }
        }
    }

    private static final Double DROPLET_CUBED = (DROPLET_RADIUS * DROPLET_RADIUS * DROPLET_RADIUS * DROPLET_RADIUS);
    private static Double WEIGHT = 0d;
    private boolean shouldThisBitchAssPositionContainWaterYesOrNo(Vec3d pos, KDTree<Droplet> nodes) {
        WEIGHT = 0d;
        nodes.rangeSearch(new Box(pos, pos).expand(MAX_DROPLET_RADIUS), droplet -> {
            // x = 1/((|pos-droplet.pos|/DROPLET_RADIUS)²)
            // x = 1/(|pos-droplet.pos|²/DROPLET_RADIUS²)
            // x = DROPLET_RADIUS² / |pos-droplet.pos|²
            // weight += x²
            // weight += DROPLET_RADIUS⁴ / (|pos-droplet.pos|²)²
            var lengthSq = pos.subtract(droplet.position).lengthSquared();
            WEIGHT += DROPLET_CUBED / (lengthSq * lengthSq);
            return WEIGHT >= 1;
        });
        return WEIGHT >= 1;
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
}

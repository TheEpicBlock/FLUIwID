package nl.theepicblock.fluiwid.client;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.fluiwid.Droplet;
import nl.theepicblock.fluiwid.FishyBusiness;
import nl.theepicblock.fluiwid.SpatialStructure;

public class FluiwidRenderer {
    private static final FluidState FLUID = Fluids.WATER.getStill(false);
    private static final double DROPLET_RADIUS = 0.2d;
    private final SpatialStructure<Droplet> particles;

    public FluiwidRenderer(SpatialStructure<Droplet> particles) {
        this.particles = particles;
    }

    public void render(VertexConsumerProvider vertexConsumerProvider, MatrixStack matrix, Camera camera) {
        var buf = vertexConsumerProvider.getBuffer(RenderLayers.getFluidLayer(FLUID));

        var bounds = particles.getBoundingBox().expand(DROPLET_RADIUS*5);
        var pixelBounds = multiply(bounds, 16);

        for (var x = (int)pixelBounds.minX; x <= (int)pixelBounds.maxX; x++) {
            for (var y = (int)pixelBounds.minY; y <= (int)pixelBounds.maxY; y++) {
                for (var z = (int)pixelBounds.minZ; z <= (int)pixelBounds.maxZ; z++) {
                    // Convert pixel coords back to world
                    var coords = new Vec3d(x/16f, y/16f, z/16f).add(1/32f, 1/32f, 1/32f);
                    if (shouldThisBitchAssPositionContainWaterYesOrNo(coords)) {
                        DebugRenderer.drawBox(matrix, vertexConsumerProvider, new Box(coords.subtract(1/32f, 1/32f, 1/32f).subtract(camera.getPos()), coords.add(1/32f, 1/32f, 1/32f).subtract(camera.getPos())), 1, 1, 1, 1);
//                        for (var direction : Direction.values()) {
//                            var c2 = coords.add(Vec3d.of(direction.getVector()).multiply(1/16f));
//                            if (!shouldThisBitchAssPositionContainWaterYesOrNo(c2)) {
//                                // We should render a side here!
//
//                            }
//                        }
                    }
                }
            }
        }
    }

    private boolean shouldThisBitchAssPositionContainWaterYesOrNo(Vec3d pos) {
        var weight = 0d;
        for (var droplet : this.particles) {
            var dst = pos.subtract(droplet.position).length() / DROPLET_RADIUS;
            var x = (1/(dst*dst));
            weight += x*x;
        }
        return weight >= 1;
    }

    private static Box multiply(Box in, double n) {
        return new Box(in.minX * n, in.minY * n, in.minZ * n, in.maxX * n, in.maxY * n, in.maxZ * n);
    }
}

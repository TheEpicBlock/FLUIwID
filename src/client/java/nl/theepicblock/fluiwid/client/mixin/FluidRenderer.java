package nl.theepicblock.fluiwid.client.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.render.block.FluidRenderer.class)
public interface FluidRenderer {
    @Invoker
    int invokeGetLight(BlockRenderView world, BlockPos pos);
}

package nl.theepicblock.fluiwid.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import nl.theepicblock.fluiwid.client.FluiwidDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.render.debug.DebugRenderer.class)
public class DebugRenderer {
    @Unique
    private FluiwidDebugRenderer fluiwidDebugRenderer;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(MinecraftClient client, CallbackInfo ci) {
        fluiwidDebugRenderer = new FluiwidDebugRenderer(client);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            fluiwidDebugRenderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
        }
    }
}

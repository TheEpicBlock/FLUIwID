package nl.theepicblock.fluiwid.client.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MakePlayerInvisible {
    @WrapWithCondition(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    private boolean shouldRenderFeature(FeatureRenderer<?,?> instance, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, Entity t, float v, float v2, float v3, float v4, float v5, float v6) {
        if (t instanceof PlayerDuck quack && quack.fluiwid$getData() != null) {
            return instance instanceof HeldItemFeatureRenderer<?,?>;
        }
        return true;
    }

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void disableRendering(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof PlayerDuck quack && quack.fluiwid$getData() != null) {
            cir.setReturnValue(false);
        }
    }
}

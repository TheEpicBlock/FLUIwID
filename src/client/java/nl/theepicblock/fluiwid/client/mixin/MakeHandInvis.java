package nl.theepicblock.fluiwid.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(HeldItemRenderer.class)
public class MakeHandInvis {
    @WrapOperation(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isInvisible()Z"))
    private boolean changeInvis(AbstractClientPlayerEntity instance, Operation<Boolean> original) {
        if (this instanceof PlayerDuck quack && quack.fluiwid$getData() != null) {
            return true;
        }
        return original.call(instance);
    }
}

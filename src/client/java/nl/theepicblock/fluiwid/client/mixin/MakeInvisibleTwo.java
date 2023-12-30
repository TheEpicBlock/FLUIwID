package nl.theepicblock.fluiwid.client.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MakeInvisibleTwo {
    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void disableRendering(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof PlayerDuck quack && quack.fluiwid$getData() != null) {
            cir.setReturnValue(true);
        }
    }
}

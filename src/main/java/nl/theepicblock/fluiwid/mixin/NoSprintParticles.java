package nl.theepicblock.fluiwid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class NoSprintParticles {
    @Inject(method = "spawnSprintingParticles", at = @At("HEAD"), cancellable = true)
    private void yeetSprintParticles(CallbackInfo ci) {
        if (this instanceof PlayerDuck quack && quack.fluiwid$getData() != null) {
            ci.cancel();
        }
    }
}

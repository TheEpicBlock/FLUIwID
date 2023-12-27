package nl.theepicblock.fluiwid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.player.PlayerEntity;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class SetNoClip {
    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z", ordinal = 0)
    )
    private boolean modifyNoClip(boolean isSpectator) {
        var isFluid = this instanceof PlayerDuck quack && quack.fluiwid$getData() != null;
        return isSpectator || isFluid;
    }
}

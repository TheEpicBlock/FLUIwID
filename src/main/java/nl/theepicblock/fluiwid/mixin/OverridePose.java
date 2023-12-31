package nl.theepicblock.fluiwid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class OverridePose extends Entity {
    public OverridePose(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    private void overridePose(CallbackInfo ci) {
        if (this instanceof PlayerDuck quack && quack.fluiwid$getData() != null) {
            this.setPose(EntityPose.STANDING);
            ci.cancel();
        }
    }
}

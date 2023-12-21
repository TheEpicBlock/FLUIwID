package nl.theepicblock.fluiwid.client.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class AdjustCamera {
    @Shadow protected abstract void setPos(Vec3d pos);

    @Shadow private Entity focusedEntity;

    @Shadow private float cameraY;

    @Shadow private float lastCameraY;

    @Inject(method = "update", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    public void overridePos(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (focusedEntity instanceof PlayerDuck quack) {
            var data = quack.fluiwid$getData();
            if (data != null && data.camera != null) {
                this.setPos(data.camera);
            }
        }
    }

    @Inject(method = "updateEyeHeight", at = @At("HEAD"), cancellable = true)
    public void overrideEyeHeight(CallbackInfo ci) {
        if (this.focusedEntity instanceof PlayerDuck quack) {
            var data = quack.fluiwid$getData();
            if (data != null && data.camera != null) {
                this.cameraY = 0;
                this.lastCameraY = 0;
                ci.cancel();
            }
        }
    }
}

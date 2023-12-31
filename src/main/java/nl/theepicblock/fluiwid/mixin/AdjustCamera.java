package nl.theepicblock.fluiwid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class AdjustCamera {
    @Inject(method = "getClientCameraPosVec", at = @At("HEAD"), cancellable = true)
    public void injectCameraPos(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (this instanceof PlayerDuck quack) {
            var data = quack.fluiwid$getData();
            if (data != null && data.camera != null) {
                cir.setReturnValue(data.getCameraPos(tickDelta));
            }
        }
    }
}

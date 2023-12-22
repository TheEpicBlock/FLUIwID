package nl.theepicblock.fluiwid.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.FishyBusiness;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class AttachWaterData extends PlayerEntity implements PlayerDuck {
    @Shadow @Final private List<ClientPlayerTickable> tickables;
    @Shadow public Input input;
    @Unique
    private FishyBusiness waterData;

    public AttachWaterData(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // TODO only add water data if the player is actually liquid
        this.waterData = new FishyBusiness(this);
        this.tickables.add(() -> this.waterData.tick());
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void tickMovement(CallbackInfo ci) {
        var x = this.input.movementSideways;
        var z = this.input.movementForward;
        float sin = MathHelper.sin(this.getYaw() * 0.017453292F);
        float cos = MathHelper.cos(this.getYaw() * 0.017453292F);
        this.waterData.movementVec = new Vec3d(x * (double)cos - z * (double)sin, 0, z * (double)cos + x * (double)sin);
    }

    @Override
    public @Nullable FishyBusiness fluiwid$getData() {
        return this.waterData;
    }
}

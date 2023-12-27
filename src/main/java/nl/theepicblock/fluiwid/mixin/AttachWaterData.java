package nl.theepicblock.fluiwid.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
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

@Mixin(ServerPlayerEntity.class)
public abstract class AttachWaterData extends PlayerEntity implements PlayerDuck {
    @Unique
    private FishyBusiness waterData;

    public AttachWaterData(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        // TODO only add water data if the player is actually liquid
        this.waterData = new FishyBusiness(this);
    }

    @Override
    public @Nullable FishyBusiness fluiwid$getData() {
        return this.waterData;
    }
}

package nl.theepicblock.fluiwid.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class CallTick extends PlayerEntity {
    @Shadow @Final private List<ClientPlayerTickable> tickables;
    @Shadow public Input input;

    public CallTick(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.tickables.add(() -> {
            var waterData = ((PlayerDuck)this).fluiwid$getData();
            if (waterData != null) {
                var chunkManager = this.getWorld().getChunkManager();
                if (chunkManager.isChunkLoaded(ChunkSectionPos.getSectionCoord(this.getBlockX()), ChunkSectionPos.getSectionCoord(this.getBlockZ()))) {
                    waterData.clientTick();
                }
            }
        });
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void tickMovement(CallbackInfo ci) {
        var waterData = ((PlayerDuck)this).fluiwid$getData();
        if (waterData != null) {
            var x = this.input.movementSideways;
            var z = this.input.movementForward;
            float sin = MathHelper.sin(this.getYaw() * 0.017453292F);
            float cos = MathHelper.cos(this.getYaw() * 0.017453292F);
            waterData.movementVec = new Vec3d(x * (double)cos - z * (double)sin, 0, z * (double)cos + x * (double)sin).normalize();
            waterData.shifting = this.input.sneaking;
        }
    }
}

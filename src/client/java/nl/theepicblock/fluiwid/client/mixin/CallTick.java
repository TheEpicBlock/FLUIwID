package nl.theepicblock.fluiwid.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class CallTick extends PlayerEntity {
    public CallTick(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        var waterData = ((PlayerDuck)this).fluiwid$getData();
        if (waterData != null) {
            var chunkManager = this.getWorld().getChunkManager();
            if (chunkManager.isChunkLoaded(ChunkSectionPos.getSectionCoord(this.getBlockX()), ChunkSectionPos.getSectionCoord(this.getBlockZ()))) {
                waterData.clientTick();
            }
        }
    }
}

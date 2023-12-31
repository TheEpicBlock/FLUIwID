package nl.theepicblock.fluiwid.client.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.theepicblock.fluiwid.PlayerDuck;
import nl.theepicblock.fluiwid.packet.UpdateC2SDataPacket;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class UpdateMovement extends PlayerEntity {
    @Shadow public Input input;

    @Shadow private int ticksSinceLastPositionPacketSent;

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    @Unique private int ticksSinceLastFluidPacket;

    public UpdateMovement(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
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

    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void sendFluidPacket(CallbackInfo ci) {
        var waterData = ((PlayerDuck)this).fluiwid$getData();
        if (waterData != null) {
            ticksSinceLastFluidPacket++;
            if (ticksSinceLastFluidPacket >= 20 && ticksSinceLastPositionPacketSent == 0) {
                ticksSinceLastFluidPacket = 0;
                var list = new ArrayList<Vector3f>();
                for (var droplet : waterData.getDroplets()) {
                    list.add(droplet.position.subtract(this.getPos()).toVector3f());
                }
                var packet = new UpdateC2SDataPacket(waterData.camera, list);
                this.networkHandler.sendPacket(ClientPlayNetworking.createC2SPacket(packet));
            }
        }
    }
}

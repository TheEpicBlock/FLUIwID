package nl.theepicblock.fluiwid.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import nl.theepicblock.fluiwid.PlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
abstract class OnTeleportPacket extends ClientCommonNetworkHandler {
    protected OnTeleportPacket(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onPlayerPositionLook(Lnet/minecraft/network/packet/s2c/play/PlayerPositionLookS2CPacket;)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/entity/player/PlayerEntity;setPosition(DDD)V"))
    private void onTeleport(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (this.client.player instanceof PlayerDuck quack) {
            var data = quack.fluiwid$getData();
            if (data != null) {
                data.teleport(this.client.player.getPos());
            }
        }
    }
}

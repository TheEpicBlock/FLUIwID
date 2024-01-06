package nl.theepicblock.fluiwid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import nl.theepicblock.fluiwid.Fluiwid;
import nl.theepicblock.fluiwid.PlayerDuck;
import nl.theepicblock.fluiwid.packet.AddParticlePacket;
import nl.theepicblock.fluiwid.packet.UpdateS2CDataPacket;
import nl.theepicblock.fluiwid.packet.YeetParticlePacket;

import java.util.Objects;

public class FluiwidClient implements ClientModInitializer {
	public static Identifier FLUIwID_RENDER_PHASE = Fluiwid.id("render");

	@Override
	public void onInitializeClient() {
		WorldRenderEvents.BEFORE_ENTITIES.register(FLUIwID_RENDER_PHASE, (ctx) -> {
			ctx.world().getPlayers().forEach(player -> {
				var fluiwidData = ((PlayerDuck)player).fluiwid$getData();
				if (fluiwidData != null) {
					var renderer = new FluiwidRenderer(fluiwidData.getDroplets());
					renderer.render(Objects.requireNonNull(ctx.consumers()), ctx.matrixStack(), ctx.camera(), ctx.world());
				}
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(UpdateS2CDataPacket.TYPE, ((packet, player, responseSender) -> {
			if (MinecraftClient.getInstance().world == null) return;
			var e = MinecraftClient.getInstance().world.getEntityById(packet.entityId());
			if (e instanceof PlayerDuck quack) {
				var data = quack.fluiwid$getData();
				if (data != null) {
					packet.apply(data);
				}
			}
		}));
		ClientPlayNetworking.registerGlobalReceiver(YeetParticlePacket.TYPE, (packet, player, responseSender) -> {
			if (MinecraftClient.getInstance().world == null) return;
			var e = MinecraftClient.getInstance().world.getEntityById(packet.entityId());
			if (e instanceof PlayerDuck quack) {
				var data = quack.fluiwid$getData();
				if (data != null) {
					packet.apply(data);
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(AddParticlePacket.TYPE, (packet, player, responseSender) -> {
			if (MinecraftClient.getInstance().world == null) return;
			var e = MinecraftClient.getInstance().world.getEntityById(packet.entityId());
			if (e instanceof PlayerDuck quack) {
				var data = quack.fluiwid$getData();
				if (data != null) {
					packet.apply(data);
				}
			}
		});
	}
}
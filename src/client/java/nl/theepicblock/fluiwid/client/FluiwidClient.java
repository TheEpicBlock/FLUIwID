package nl.theepicblock.fluiwid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.Identifier;
import nl.theepicblock.fluiwid.Fluiwid;
import nl.theepicblock.fluiwid.PlayerDuck;

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
	}
}
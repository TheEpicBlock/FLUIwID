package nl.theepicblock.fluiwid.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import nl.theepicblock.fluiwid.PlayerDuck;

public class FluiwidDebugRenderer implements DebugRenderer.Renderer {
    private final MinecraftClient client;

    public FluiwidDebugRenderer(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        var player = client.player;
        if (player == null) return;
        var data = ((PlayerDuck)player).fluiwid$getData();
        if (data == null) return;

        for (var droplet : data.getDroplets()) {
            var box = droplet.getBox();
            DebugRenderer.drawBox(matrices, vertexConsumers, box.offset(-cameraX, -cameraY, -cameraZ), 1f, 1f, 0f, 1f);
        }
        DebugRenderer.drawBox(matrices, vertexConsumers, new Box(data.center, data.center.add(0, 1, 0)).expand(0.05).offset(-cameraX, -cameraY, -cameraZ), 0, 1, 1, 1);
        DebugRenderer.drawBox(matrices, vertexConsumers, new Box(data.camera, data.camera).expand(0.05).offset(-cameraX, -cameraY, -cameraZ), 1, 0, 0, 1);
    }
}

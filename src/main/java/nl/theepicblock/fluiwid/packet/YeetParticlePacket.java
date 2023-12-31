package nl.theepicblock.fluiwid.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.fluiwid.FishyBusiness;
import nl.theepicblock.fluiwid.Fluiwid;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;

public record YeetParticlePacket(int entityId, int index) implements FabricPacket {
    public static PacketType<YeetParticlePacket> TYPE = PacketType.create(Fluiwid.id("yeet"), YeetParticlePacket::new);

    public YeetParticlePacket(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(index);
    }

    public void apply(@NotNull FishyBusiness data) {
        data.yeetParticle(this.index);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}

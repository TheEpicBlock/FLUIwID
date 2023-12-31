package nl.theepicblock.fluiwid.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.fluiwid.Droplet;
import nl.theepicblock.fluiwid.FishyBusiness;
import nl.theepicblock.fluiwid.Fluiwid;
import org.jetbrains.annotations.NotNull;

public record AddParticlePacket(int entityId, Vec3d pos) implements FabricPacket {
    public static PacketType<AddParticlePacket> TYPE = PacketType.create(Fluiwid.id("add"), AddParticlePacket::new);

    public AddParticlePacket(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readVec3d());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVec3d(pos);
    }

    public void apply(@NotNull FishyBusiness data) {
        data.addParticle(pos);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}

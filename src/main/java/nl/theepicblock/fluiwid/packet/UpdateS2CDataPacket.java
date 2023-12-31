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

public record UpdateS2CDataPacket(int entityId, ArrayList<Vector3f> offsets) implements FabricPacket {
    public static PacketType<UpdateS2CDataPacket> TYPE = PacketType.create(Fluiwid.id("update_c"), UpdateS2CDataPacket::new);

    public UpdateS2CDataPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readCollection(ArrayList::new, PacketByteBuf::readVector3f));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeCollection(this.offsets, PacketByteBuf::writeVector3f);
    }

    public void apply(@NotNull FishyBusiness data) {
        if (data.getDroplets().backend.size() < offsets.size()) return;
        for (int i = 0; i < offsets.size(); i++) {
            data.getDroplets().backend.get(i).position = data.canonPosition.add(new Vec3d(offsets.get(i)));
        }
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}

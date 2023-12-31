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

public record UpdateC2SDataPacket(Vec3d cam, ArrayList<Vector3f> offsets) implements FabricPacket {
    public static PacketType<UpdateC2SDataPacket> TYPE = PacketType.create(Fluiwid.id("update"), UpdateC2SDataPacket::new);

    public UpdateC2SDataPacket(PacketByteBuf buf) {
        this(buf.readVec3d(), buf.readCollection(ArrayList::new, PacketByteBuf::readVector3f));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVec3d(cam);
        buf.writeCollection(this.offsets, PacketByteBuf::writeVector3f);
    }

    public void apply(@NotNull FishyBusiness data) {
        data.canonPosition = data.player.getPos();
        data.camera = cam;
        data.prevCamera = cam;
        data.center = data.canonPosition;
        if (data.getDroplets().backend.size() > offsets.size()) return;
        for (int i = 0; i < offsets.size(); i++) {
            data.getDroplets().backend.get(i).position = data.canonPosition.add(new Vec3d(offsets.get(i)));
        }
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}

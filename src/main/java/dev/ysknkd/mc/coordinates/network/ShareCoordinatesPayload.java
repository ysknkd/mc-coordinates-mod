package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record ShareCoordinatesPayload(
        UUID sender,
        UUID uuid,
        double x,
        double y,
        double z,
        String description,
        String world,
        boolean pinned,
        String icon
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShareCoordinatesPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "share_coordinates"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShareCoordinatesPayload> CODEC =
            StreamCodec.ofMember(ShareCoordinatesPayload::encode, ShareCoordinatesPayload::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeUUID(uuid);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeUtf(description != null ? description : "");
        buf.writeUtf(world != null ? world : "");
        buf.writeBoolean(pinned);
        buf.writeUtf(icon != null ? icon : "");
    }

    private static ShareCoordinatesPayload decode(RegistryFriendlyByteBuf buf) {
        UUID sender = buf.readUUID();
        UUID uuid = buf.readUUID();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String description = buf.readUtf();
        String world = buf.readUtf();
        boolean pinned = buf.readBoolean();
        String icon = buf.readUtf();
        return new ShareCoordinatesPayload(sender, uuid, x, y, z, description, world, pinned, icon);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

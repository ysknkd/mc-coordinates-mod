package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

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
) implements CustomPayload {

    public static final CustomPayload.Id<ShareCoordinatesPayload> ID = new Id<>(Identifier.of(CoordinatesApp.MOD_ID, "share_coordinates"));

    public static final PacketCodec<RegistryByteBuf, ShareCoordinatesPayload> CODEC = new PacketCodec<RegistryByteBuf, ShareCoordinatesPayload>() {
        @Override
        public void encode(RegistryByteBuf buf, ShareCoordinatesPayload payload) {
            buf.writeUuid(payload.sender());
            buf.writeUuid(payload.uuid());
            buf.writeDouble(payload.x());
            buf.writeDouble(payload.y());
            buf.writeDouble(payload.z());
            buf.writeString(payload.description() != null ? payload.description() : "");
            buf.writeString(payload.world() != null ? payload.world() : "");
            buf.writeBoolean(payload.pinned());
            buf.writeString(payload.icon() != null ? payload.icon() : "");
        }

        @Override
        public ShareCoordinatesPayload decode(RegistryByteBuf buf) {
            UUID sender = buf.readUuid();
            UUID uuid = buf.readUuid();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            String description = buf.readString();
            String world = buf.readString();
            boolean pinned = buf.readBoolean();
            String icon = buf.readString();
            return new ShareCoordinatesPayload(sender, uuid, x, y, z, description, world, pinned, icon);
        }
    };

    /**
     * ペイロードのエンコード処理
     * 書き込み順序に注意してください。
     *
     * @param buf 書き込み先の RegistryByteBuf
     * @param payload エンコードする ShareCoordinatesPayload
     */
    public static void encode(RegistryByteBuf buf, ShareCoordinatesPayload payload) {
        buf.writeUuid(payload.sender);
        buf.writeUuid(payload.uuid);
        buf.writeDouble(payload.x);
        buf.writeDouble(payload.y);
        buf.writeDouble(payload.z);
        buf.writeString(payload.description);
        buf.writeString(payload.world);
        buf.writeBoolean(payload.pinned);
        buf.writeString(payload.icon);
    }

    /**
     * ペイロードのデコード処理
     * エンコード時と同じ順序で読み出します。
     *
     * @param buf 読み込み元の RegistryByteBuf
     * @return 読み出した ShareCoordinatesPayload インスタンス
     */
    public static ShareCoordinatesPayload decode(RegistryByteBuf buf) {
        UUID sender = buf.readUuid();
        UUID uuid = buf.readUuid();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String description = buf.readString();
        String world = buf.readString();
        boolean pinned = buf.readBoolean();
        String icon = buf.readString();
        return new ShareCoordinatesPayload(sender, uuid, x, y, z, description, world, pinned, icon);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
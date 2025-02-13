package com.bungggo.mc.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * クライアントとサーバー間で送受信する位置情報ペイロード
 */
public record LocationPayload(
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

    public static final CustomPayload.Id<LocationPayload> ID = new Id<>(Identifier.of("mc-location", "location_sync"));

    // ここで独自の CODEC を定義しています
    public static final PacketCodec<RegistryByteBuf, LocationPayload> CODEC = new PacketCodec<RegistryByteBuf, LocationPayload>() {
        @Override
        public void encode(RegistryByteBuf buf, LocationPayload payload) {
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
        public LocationPayload decode(RegistryByteBuf buf) {
            UUID sender = buf.readUuid();
            UUID uuid = buf.readUuid();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            String description = buf.readString();
            String world = buf.readString();
            boolean pinned = buf.readBoolean();
            String icon = buf.readString();
            return new LocationPayload(sender, uuid, x, y, z, description, world, pinned, icon);
        }
    };

    /**
     * ペイロードのエンコード処理
     * 書き込み順序に注意してください。
     *
     * @param buf 書き込み先の RegistryByteBuf
     * @param payload エンコードする LocationPayload
     */
    public static void encode(RegistryByteBuf buf, LocationPayload payload) {
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
     * @return 読み出した LocationPayload インスタンス
     */
    public static LocationPayload decode(RegistryByteBuf buf) {
        UUID sender = buf.readUuid();
        UUID uuid = buf.readUuid();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String description = buf.readString();
        String world = buf.readString();
        boolean pinned = buf.readBoolean();
        String icon = buf.readString();
        return new LocationPayload(sender, uuid, x, y, z, description, world, pinned, icon);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
} 
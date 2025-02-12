package com.bungggo.mc.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

/**
 * クライアントとサーバー間で送受信する位置情報ペイロード
 */
public record LocationPayload(java.util.UUID sender, double x, double y, double z, String description, String world, boolean pinned, String icon) implements CustomPayload {
    public static final CustomPayload.Id<LocationPayload> ID = new Id<>(Identifier.of("mc-location", "location_sync"));
    public static final PacketCodec<RegistryByteBuf, LocationPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, LocationPayload::sender,
            PacketCodecs.DOUBLE, LocationPayload::x,
            PacketCodecs.DOUBLE, LocationPayload::y,
            PacketCodecs.DOUBLE, LocationPayload::z,
            PacketCodecs.STRING, LocationPayload::description,
            PacketCodecs.STRING, LocationPayload::world,
            PacketCodecs.BOOLEAN, LocationPayload::pinned,
            PacketCodecs.STRING, LocationPayload::icon,
            LocationPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
} 
package com.bungggo.mc.network;

import java.util.UUID;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

/**
 * サーバー側で作成し、CustomPayload で送信するプレイヤーの位置情報パケットです。<br>
 * JSON ではなく、バイナリ形式（PacketByteBuf を利用）でエンコード／デコードを行います。
 */
public record PlayerLocationPayload(
    UUID uuid,
    double x,
    double y,
    double z,
    String world
) implements CustomPayload {

    public static final CustomPayload.Id<PlayerLocationPayload> ID = new Id<>(Identifier.of("mc-location", "player_location"));

    public static final PacketCodec<RegistryByteBuf, PlayerLocationPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, PlayerLocationPayload::uuid,
        PacketCodecs.DOUBLE, PlayerLocationPayload::x,
        PacketCodecs.DOUBLE, PlayerLocationPayload::y,
        PacketCodecs.DOUBLE, PlayerLocationPayload::z,
        PacketCodecs.STRING, PlayerLocationPayload::world,
        PlayerLocationPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

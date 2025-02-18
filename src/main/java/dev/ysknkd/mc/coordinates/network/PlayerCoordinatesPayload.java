package dev.ysknkd.mc.coordinates.network;

import java.util.UUID;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public record PlayerCoordinatesPayload(
    UUID uuid,
    double x,
    double y,
    double z,
    String name,
    String world
) implements CustomPayload {

    public static final CustomPayload.Id<PlayerCoordinatesPayload> ID = new Id<>(Identifier.of(CoordinatesApp.MOD_ID, "player_coordinates"));

    public static final PacketCodec<RegistryByteBuf, PlayerCoordinatesPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, PlayerCoordinatesPayload::uuid,
        PacketCodecs.DOUBLE, PlayerCoordinatesPayload::x,
        PacketCodecs.DOUBLE, PlayerCoordinatesPayload::y,
        PacketCodecs.DOUBLE, PlayerCoordinatesPayload::z,
        PacketCodecs.STRING, PlayerCoordinatesPayload::name,
        PacketCodecs.STRING, PlayerCoordinatesPayload::world,
        PlayerCoordinatesPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

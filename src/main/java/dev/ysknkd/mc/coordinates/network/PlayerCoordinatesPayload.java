package dev.ysknkd.mc.coordinates.network;

import java.util.UUID;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayerCoordinatesPayload(
    UUID uuid,
    double x,
    double y,
    double z,
    String name,
    String world
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayerCoordinatesPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "player_coordinates"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerCoordinatesPayload> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, PlayerCoordinatesPayload::uuid,
        ByteBufCodecs.DOUBLE, PlayerCoordinatesPayload::x,
        ByteBufCodecs.DOUBLE, PlayerCoordinatesPayload::y,
        ByteBufCodecs.DOUBLE, PlayerCoordinatesPayload::z,
        ByteBufCodecs.STRING_UTF8, PlayerCoordinatesPayload::name,
        ByteBufCodecs.STRING_UTF8, PlayerCoordinatesPayload::world,
        PlayerCoordinatesPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

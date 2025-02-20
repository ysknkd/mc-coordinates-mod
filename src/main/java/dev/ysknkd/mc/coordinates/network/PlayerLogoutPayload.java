package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Payload for logout notification. Holds only the UUID of the logged-out player.
 */
public record PlayerLogoutPayload(UUID uuid) implements CustomPayload {

    public static final CustomPayload.Id<PlayerLogoutPayload> ID = new Id<>(Identifier.of(CoordinatesApp.MOD_ID, "player_logout"));

    public static final PacketCodec<RegistryByteBuf, PlayerLogoutPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, PlayerLogoutPayload::uuid,
            PlayerLogoutPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
} 
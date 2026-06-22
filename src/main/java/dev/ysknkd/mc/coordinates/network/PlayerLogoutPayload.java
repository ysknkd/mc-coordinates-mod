package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Payload for logout notification. Holds only the UUID of the logged-out player.
 */
public record PlayerLogoutPayload(UUID uuid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayerLogoutPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "player_logout"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerLogoutPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerLogoutPayload::uuid,
            PlayerLogoutPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

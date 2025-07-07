package dev.ysknkd.mc.coordinates.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShareCoordinatesHandler implements PlayPayloadHandler<ShareCoordinatesPayload> {
    
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShareCoordinatesPayload.ID, ShareCoordinatesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShareCoordinatesPayload.ID, ShareCoordinatesPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ShareCoordinatesPayload.ID, new ShareCoordinatesHandler());
    }

    @Override
    public void receive(ShareCoordinatesPayload payload, Context context) {
        ServerPlayerEntity senderPlayer = context.player();

        context.server().execute(() -> {
            ShareCoordinatesPayload outgoing = new ShareCoordinatesPayload(senderPlayer.getUuid(), payload.uuid(), payload.x(),
                    payload.y(), payload.z(), payload.description(), payload.world(), payload.pinned(), payload.icon());

            for (ServerPlayerEntity target : senderPlayer.getServer().getPlayerManager().getPlayerList()) {
                if (!target.getUuid().equals(senderPlayer.getUuid())) {
                    ServerPlayNetworking.send(target, outgoing);
                }
            }
        });
    }
}

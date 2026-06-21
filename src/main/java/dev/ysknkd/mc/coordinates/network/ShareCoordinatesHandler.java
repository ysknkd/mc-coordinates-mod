package dev.ysknkd.mc.coordinates.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.server.level.ServerPlayer;

public class ShareCoordinatesHandler implements PlayPayloadHandler<ShareCoordinatesPayload> {
    
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ShareCoordinatesPayload.ID, ShareCoordinatesPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ShareCoordinatesPayload.ID, ShareCoordinatesPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ShareCoordinatesPayload.ID, new ShareCoordinatesHandler());
    }

    @Override
    public void receive(ShareCoordinatesPayload payload, Context context) {
        ServerPlayer senderPlayer = context.player();

        context.server().execute(() -> {
            ShareCoordinatesPayload outgoing = new ShareCoordinatesPayload(senderPlayer.getUUID(), payload.uuid(), payload.x(),
                    payload.y(), payload.z(), payload.description(), payload.world(), payload.pinned(), payload.icon());

            for (ServerPlayer target : context.server().getPlayerList().getPlayers()) {
                if (!target.getUUID().equals(senderPlayer.getUUID())
                        && ServerPlayNetworking.canSend(target, ShareCoordinatesPayload.ID)) {
                    ServerPlayNetworking.send(target, outgoing);
                }
            }
        });
    }
}

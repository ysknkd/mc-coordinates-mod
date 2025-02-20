package dev.ysknkd.mc.coordinates;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import dev.ysknkd.mc.coordinates.config.Config;
import dev.ysknkd.mc.coordinates.event.CoordinatesListBinding;
import dev.ysknkd.mc.coordinates.event.CoordinatesSaveKeyBinding;
import dev.ysknkd.mc.coordinates.hud.CoordinatesRenderer;
import dev.ysknkd.mc.coordinates.hud.Notification;
import dev.ysknkd.mc.coordinates.hud.PlayerIndicatorRenderer;
import dev.ysknkd.mc.coordinates.network.PlayerCoordinatesHandler;
import dev.ysknkd.mc.coordinates.network.ShareCoordinatesClientHandler;
import dev.ysknkd.mc.coordinates.network.PlayerLogoutClientHandler;
import dev.ysknkd.mc.coordinates.hud.IndicatorRenderer;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.util.Util;

@Environment(EnvType.CLIENT)
public class CoordinatesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CoordinatesSaveKeyBinding.register();
        CoordinatesListBinding.register();

        Notification.register();
        CoordinatesRenderer.register();
        IndicatorRenderer.register();
        PlayerIndicatorRenderer.register();

        ShareCoordinatesClientHandler.register();
        PlayerCoordinatesHandler.register();
        PlayerLogoutClientHandler.register();

        // At login: clear and load storage settings as necessary
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            CoordinatesDataManager.clear();
            CoordinatesDataManager.load(Util.createWorldIdentifier(client));
            Config.reset();
            Config.load(Util.createWorldIdentifier(client));
        });

        // At logout: save data to storage
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CoordinatesDataManager.save();
            Config.save();
        });

        // On client shutdown: save data
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            CoordinatesDataManager.save();
            Config.save();
        });    
    }
} 
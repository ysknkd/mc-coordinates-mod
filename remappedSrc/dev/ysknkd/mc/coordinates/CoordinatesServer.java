package dev.ysknkd.mc.coordinates;

import net.fabricmc.api.DedicatedServerModInitializer;

import dev.ysknkd.mc.coordinates.network.PlayerCoordinatesBroadcaster;
import dev.ysknkd.mc.coordinates.network.ShareCoordinatesHandler;
import dev.ysknkd.mc.coordinates.network.PlayerLogoutBroadcaster;

public class CoordinatesServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ShareCoordinatesHandler.register();
        PlayerCoordinatesBroadcaster.register();
        PlayerLogoutBroadcaster.register();
    }

} 
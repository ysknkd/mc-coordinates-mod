package dev.ysknkd.mc.coordinates;

import net.fabricmc.api.DedicatedServerModInitializer;

import dev.ysknkd.mc.coordinates.network.PlayerCoordinatesBroadcaster;
import dev.ysknkd.mc.coordinates.network.ShareCoordinatesHandler;

public class CoordinatesServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ShareCoordinatesHandler.register();
        PlayerCoordinatesBroadcaster.register();
    }

} 
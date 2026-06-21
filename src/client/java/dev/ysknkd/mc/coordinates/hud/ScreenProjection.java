package dev.ysknkd.mc.coordinates.hud;

import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

final class ScreenProjection {
    private ScreenProjection() {
    }

    static Optional<Coordinate> projectWorldToGui(Minecraft client, Camera camera, Vec3 worldPos, int screenWidth, int screenHeight) {
        if (isBehindCamera(camera, worldPos)) {
            return Optional.empty();
        }

        Vec3 ndcPos = client.gameRenderer.projectPointToScreen(worldPos);
        if (ndcPos == null || !ndcPos.isFinite()) {
            return Optional.empty();
        }

        int screenX = clamp((int) Math.round((ndcPos.x + 1.0D) * 0.5D * screenWidth), 0, screenWidth);
        int screenY = clamp((int) Math.round((1.0D - ndcPos.y) * 0.5D * screenHeight), 0, screenHeight);
        return Optional.of(new Coordinate(screenX, screenY));
    }

    private static boolean isBehindCamera(Camera camera, Vec3 worldPos) {
        Vec3 toTarget = worldPos.subtract(camera.position());
        Vector3fc forward = camera.forwardVector();
        double forwardDistance = toTarget.x * forward.x() + toTarget.y * forward.y() + toTarget.z * forward.z();
        return forwardDistance <= 0.0D;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    static final class Coordinate {
        final int x;
        final int y;

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}

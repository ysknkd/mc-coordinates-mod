package dev.ysknkd.mc.coordinates.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.config.Config;

/**
 * Settings screen
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {

    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Component.translatable(CoordinatesApp.MOD_ID + ".settings.title"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 30; // Adjusted to make room for the new slider

        // Retrieve text resource based on the current boolean value
        Component pinStatusText = Config.getDefaultPinState() ?
                Component.translatable(CoordinatesApp.MOD_ID + ".settings.enabled") :
                Component.translatable(CoordinatesApp.MOD_ID + ".settings.disabled");

        this.addRenderableWidget(
            Button.builder(
                Component.translatable(CoordinatesApp.MOD_ID + ".settings.pin_state.title", pinStatusText),
                button -> {
                    Config.toggleDefaultPinState();
                    Component newStatus = Config.getDefaultPinState() ?
                            Component.translatable(CoordinatesApp.MOD_ID + ".settings.enabled") :
                            Component.translatable(CoordinatesApp.MOD_ID + ".settings.disabled");
                    button.setMessage(Component.translatable(CoordinatesApp.MOD_ID + ".settings.pin_state.title", newStatus));
                })
            .bounds(centerX - 100, centerY, 200, 20)
            .build()
        );

        // Player indicator minimum distance slider
        int currentDistance = Config.getPlayerIndicatorMinDistance();
        this.addRenderableWidget(new PlayerIndicatorDistanceSlider(
            centerX - 100, centerY + 30, 200, 20,
            Component.translatable(CoordinatesApp.MOD_ID + ".settings.player_indicator_distance", currentDistance),
            currentDistance
        ));

        // Back button: returns to CoordinatesListScreen
        this.addRenderableWidget(
            Button.builder(Component.translatable(CoordinatesApp.MOD_ID + ".button.back"), button -> onClose())
            .bounds(centerX - 50, centerY + 60, 100, 20)
            .build()
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    /**
     * Slider widget for adjusting the player indicator minimum distance
     */
    private static class PlayerIndicatorDistanceSlider extends AbstractSliderButton {
        private static final int MIN_DISTANCE = 0;
        private static final int MAX_DISTANCE = 50;
        
        public PlayerIndicatorDistanceSlider(int x, int y, int width, int height, Component text, int value) {
            super(x, y, width, height, text, (double)(value - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE));
        }
        
        @Override
        protected void updateMessage() {
            int value = MIN_DISTANCE + (int)(this.value * (MAX_DISTANCE - MIN_DISTANCE));
            this.setMessage(Component.translatable(CoordinatesApp.MOD_ID + ".settings.player_indicator_distance", value));
        }
        
        @Override
        protected void applyValue() {
            int value = MIN_DISTANCE + (int)(this.value * (MAX_DISTANCE - MIN_DISTANCE));
            Config.setPlayerIndicatorMinDistance(value);
        }
    }
}

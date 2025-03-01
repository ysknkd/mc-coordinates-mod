package dev.ysknkd.mc.coordinates.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.config.Config;

/**
 * Settings screen
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {

    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Text.translatable(CoordinatesApp.MOD_ID + ".settings.title"));
        this.parent = parent;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 30; // Adjusted to make room for the new slider

        // Retrieve text resource based on the current boolean value
        Text pinStatusText = Config.getDefaultPinState() ?
                Text.translatable(CoordinatesApp.MOD_ID + ".settings.enabled") :
                Text.translatable(CoordinatesApp.MOD_ID + ".settings.disabled");

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.translatable(CoordinatesApp.MOD_ID + ".settings.pin_state.title", pinStatusText),
                button -> {
                    Config.toggleDefaultPinState();
                    Text newStatus = Config.getDefaultPinState() ?
                            Text.translatable(CoordinatesApp.MOD_ID + ".settings.enabled") :
                            Text.translatable(CoordinatesApp.MOD_ID + ".settings.disabled");
                    button.setMessage(Text.translatable(CoordinatesApp.MOD_ID + ".settings.pin_state.title", newStatus));
                })
            .dimensions(centerX - 100, centerY, 200, 20)
            .build()
        );

        // Player indicator minimum distance slider
        int currentDistance = Config.getPlayerIndicatorMinDistance();
        this.addDrawableChild(new PlayerIndicatorDistanceSlider(
            centerX - 100, centerY + 30, 200, 20,
            Text.translatable(CoordinatesApp.MOD_ID + ".settings.player_indicator_distance", currentDistance),
            currentDistance
        ));

        // Back button: returns to CoordinatesListScreen
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable(CoordinatesApp.MOD_ID + ".button.back"), button -> close())
            .dimensions(centerX - 50, centerY + 60, 100, 20)
            .build()
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
    /**
     * Slider widget for adjusting the player indicator minimum distance
     */
    private static class PlayerIndicatorDistanceSlider extends SliderWidget {
        private static final int MIN_DISTANCE = 0;
        private static final int MAX_DISTANCE = 50;
        
        public PlayerIndicatorDistanceSlider(int x, int y, int width, int height, Text text, int value) {
            super(x, y, width, height, text, (double)(value - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE));
        }
        
        @Override
        protected void updateMessage() {
            int value = MIN_DISTANCE + (int)(this.value * (MAX_DISTANCE - MIN_DISTANCE));
            this.setMessage(Text.translatable(CoordinatesApp.MOD_ID + ".settings.player_indicator_distance", value));
        }
        
        @Override
        protected void applyValue() {
            int value = MIN_DISTANCE + (int)(this.value * (MAX_DISTANCE - MIN_DISTANCE));
            Config.setPlayerIndicatorMinDistance(value);
        }
    }
} 
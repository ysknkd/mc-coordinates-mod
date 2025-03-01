package dev.ysknkd.mc.coordinates.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
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
        int centerY = this.height / 2 - 30;

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

        // Add indicator distance setting
        final ButtonWidget indicatorDistanceButton = ButtonWidget.builder(
            Text.translatable(CoordinatesApp.MOD_ID + ".settings.indicator_distance.title", 
                String.format("%.0f", Config.getMinIndicatorDistance())),
            button -> {}).dimensions(centerX - 100, centerY + 25, 200, 20).build();
        this.addDrawableChild(indicatorDistanceButton);
        
        // Decrease button
        this.addDrawableChild(
            ButtonWidget.builder(
                Text.of("-"),
                button -> {
                    Config.decreaseMinIndicatorDistance();
                    indicatorDistanceButton.setMessage(
                        Text.translatable(CoordinatesApp.MOD_ID + ".settings.indicator_distance.title", 
                            String.format("%.0f", Config.getMinIndicatorDistance())));
                })
            .dimensions(centerX - 125, centerY + 25, 20, 20)
            .build()
        );
        
        // Increase button
        this.addDrawableChild(
            ButtonWidget.builder(
                Text.of("+"),
                button -> {
                    Config.increaseMinIndicatorDistance();
                    indicatorDistanceButton.setMessage(
                        Text.translatable(CoordinatesApp.MOD_ID + ".settings.indicator_distance.title", 
                            String.format("%.0f", Config.getMinIndicatorDistance())));
                })
            .dimensions(centerX + 105, centerY + 25, 20, 20)
            .build()
        );

        // Back button: returns to CoordinatesListScreen
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable(CoordinatesApp.MOD_ID + ".button.back"), button -> close())
            .dimensions(centerX - 50, centerY + 55, 100, 20)
            .build()
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
} 
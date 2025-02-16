package com.bungggo.mc.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import com.bungggo.mc.config.LocationConfig;

/**
 * 位置情報保存の設定画面です。<br>
 * 「保存時にピン状態を初期状態として有効にするか」を設定できます。
 */
@Environment(EnvType.CLIENT)
public class LocationSettingsScreen extends Screen {

    private final Screen parent;

    public LocationSettingsScreen(Screen parent) {
        super(Text.literal("設定"));
        this.parent = parent;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 10;

        // ピン状態のトグルボタン
        String pinStatusText = LocationConfig.getDefaultPinState() ? "有効" : "無効";
        this.addDrawableChild(
            ButtonWidget.builder(
                Text.literal("保存時にピン状態を: " + pinStatusText),
                button -> {
                    // ON/OFF をトグルして、ボタン表示を更新
                    LocationConfig.toggleDefaultPinState();
                    String newStatus = LocationConfig.getDefaultPinState() ? "有効" : "無効";
                    button.setMessage(Text.literal("保存時にピン状態を: " + newStatus));
                })
            .dimensions(centerX - 100, centerY, 200, 20)
            .build()
        );

        // 戻るボタン：LocationListScreen に戻る
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("戻る"), button -> {
                close();
            })
            .dimensions(centerX - 50, centerY + 30, 100, 20)
            .build()
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
} 
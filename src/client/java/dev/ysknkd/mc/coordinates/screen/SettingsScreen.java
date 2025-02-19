package dev.ysknkd.mc.coordinates.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import dev.ysknkd.mc.coordinates.config.Config;

/**
 * 位置情報保存の設定画面です。<br>
 * 「保存時にピン状態を初期状態として有効にするか」を設定できます。
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {

    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Text.translatable("modid.settings.title"));
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

        // boolean に応じてテキストリソースから取得する
        Text pinStatusText = Config.getDefaultPinState() ?
                Text.translatable("modid.settings.enabled") :
                Text.translatable("modid.settings.disabled");

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.translatable("modid.settings.pin_state.title", pinStatusText),
                button -> {
                    Config.toggleDefaultPinState();
                    Text newStatus = Config.getDefaultPinState() ?
                            Text.translatable("modid.settings.enabled") :
                            Text.translatable("modid.settings.disabled");
                    button.setMessage(Text.translatable("modid.settings.pin_state.title", newStatus));
                })
            .dimensions(centerX - 100, centerY, 200, 20)
            .build()
        );

        // 戻るボタン：CoordinatesListScreen に戻る
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("modid.button.back"), button -> close())
            .dimensions(centerX - 50, centerY + 30, 100, 20)
            .build()
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
} 
package com.bungggo.mc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * リスト表示画面。
 * 全エントリを一覧表示し、各エントリのピン留め状態を
 * 一意なIDに基づいてトグルできます。
 */
@Environment(EnvType.CLIENT)
public class LocationListScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationListScreen.class);

    public LocationListScreen() {
        super(Text.literal("保存データ一覧"));
    }

    @Override
    protected void init() {
        LOGGER.info("[LocationListScreen] init() called");
        
        // 画面下部に「閉じる」ボタン
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("閉じる"), button -> {
                MinecraftClient.getInstance().setScreen(null);
            })
            .dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
            .build()
        );

        // 定数
        final int ICON_SIZE = 20;
        final int ICON_GAP = 4;
        final int LEFT_MARGIN = 10;
        final int topMargin = 20;
        final int rowHeight = ICON_SIZE + 4; // 例：20 + 4 = 24
        // 削除ボタンは右端側に配置（右マージンも LEFT_MARGIN と同じ値）
        final int xDelete = this.width - ICON_SIZE - LEFT_MARGIN;

        // 各エントリごとにウィジェットを配置
        for (int i = 0; i < LocationDataManager.getEntries().size(); i++) {
            int rowY = topMargin + i * rowHeight;
            LocationEntry entry = LocationDataManager.getEntries().get(i);

            // お気に入りトグルボタン（左端、アイコン「★」）
            this.addDrawableChild(new ToggleIconButton(
                LEFT_MARGIN,
                rowY,
                ICON_SIZE,
                ICON_SIZE,
                Text.literal("★"),
                button -> {
                    entry.favorite = !entry.favorite;
                    MinecraftClient.getInstance().setScreen(new LocationListScreen());
                },
                entry.favorite
            ));

            // ピン留めトグルボタン（お気に入りボタンの右側、アイコン「📌」）
            this.addDrawableChild(new ToggleIconButton(
                LEFT_MARGIN + ICON_SIZE + ICON_GAP,
                rowY,
                ICON_SIZE,
                ICON_SIZE,
                Text.literal("📌"),
                button -> {
                    entry.pinned = !entry.pinned;
                    MinecraftClient.getInstance().setScreen(new LocationListScreen());
                },
                entry.pinned
            ));

            // 「説明変更」ボタンを配置
            final int DESC_BUTTON_WIDTH = 70;
            int xDesc = xDelete - DESC_BUTTON_WIDTH - ICON_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("説明変更"), button -> {
                    // 説明変更用の画面へ遷移。生成時に対象エントリを渡す。
                    MinecraftClient.getInstance().setScreen(new LocationDescriptionEditScreen(entry));
                })
                .dimensions(xDesc, rowY, DESC_BUTTON_WIDTH, ICON_SIZE)
                .build()
            );

            // 削除ボタン（右端、ゴミ箱アイコン「🗑」）
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("🗑"), button -> {
                    // お気に入り状態の場合は削除不可
                    if (entry.favorite) {
                        LOGGER.info("お気に入りのエントリは削除できません: ");
                        return;
                    }
                    LocationDataManager.removeEntry(entry);
                    MinecraftClient.getInstance().setScreen(new LocationListScreen());
                })
                .dimensions(xDelete, rowY, ICON_SIZE, ICON_SIZE)
                .build()
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        
        final int ICON_SIZE = 20;
        final int ICON_GAP = 4;
        final int LEFT_MARGIN = 10;
        final int topMargin = 20;
        final int rowHeight = ICON_SIZE + 4;
        // 位置情報のテキスト描画開始位置：アイコンの右側
        final int TEXT_START_X = LEFT_MARGIN + 2 * (ICON_SIZE + ICON_GAP);

        for (int i = 0; i < LocationDataManager.getEntries().size(); i++) {
            int rowY = topMargin + i * rowHeight;
            LocationEntry e = LocationDataManager.getEntries().get(i);
            // 位置情報は getLocationText() で取得する
            String locationText = e.getLocationText();
            context.drawText(
                this.textRenderer,
                locationText,
                TEXT_START_X,
                rowY + (ICON_SIZE / 2) - (this.textRenderer.fontHeight / 2),
                0xFFFFFF,
                true
            );

            int locationTextWidth = this.textRenderer.getWidth(locationText);
            int descriptionX = TEXT_START_X + locationTextWidth + 10; // ギャップ10px
            context.drawText(
                this.textRenderer,
                e.description,
                descriptionX,
                rowY + (ICON_SIZE / 2) - (this.textRenderer.fontHeight / 2),
                0xFFFFFF,
                true
            );
        }
        
        // 各種ウィジェットの描画
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     * トグル状態を保持するアイコンボタン。
     * 状態に応じて背景色を変え、選択時（押下状態）のビジュアルを維持して表示します。
     */
    private class ToggleIconButton extends ButtonWidget {
        private boolean toggled;

        public ToggleIconButton(int x, int y, int width, int height, Text message, PressAction onPress, boolean toggled) {
            super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
            this.toggled = toggled;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // デフォルトのボタン描画
            super.renderWidget(context, mouseX, mouseY, delta);
            // トグルONの場合、半透明のオーバーレイを追加して押下状態を表現
            if (!toggled) {
                context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);
            }
        }
    }
}
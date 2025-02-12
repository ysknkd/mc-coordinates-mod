package com.bungggo.mc.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bungggo.mc.network.LocationShare;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;

import java.util.List;

/**
 * 保存データ一覧画面。
 * ページャー機能を用いて、エントリの位置情報と説明文を表示します。
 * 画面下部の固定位置に、ページ数テキストの両側に "<" と ">" のボタンを配置します。
 */
@Environment(EnvType.CLIENT)
public class LocationListScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationListScreen.class);
    // 一度に表示するエントリ数
    private static final int ENTRIES_PER_PAGE = 6;
    // 現在のページ番号 (0オリジン)
    private int currentPage = 0;

    // 表示件数／ページなどの定数
    private static final int ICON_SIZE = 20;
    private static final int ICON_GAP = 4;
    private static final int LEFT_MARGIN = 10;
    private static final int TOP_MARGIN = 35;
    private static final int ROW_HEIGHT = ICON_SIZE + 4;
    private static final int DESC_BUTTON_WIDTH = 70;
    private static final int CLOSE_BUTTON_WIDTH = 100;
    private static final int CLOSE_BUTTON_HEIGHT = 20;
    private static final int PAGER_BUTTON_WIDTH = 20;
    private static final int PAGER_BUTTON_HEIGHT = 20;
    private static final int PAGER_GAP = 6;
    private static final int PAGINATION_AREA_OFFSET = 60; // 画面下部からのオフセット

    public LocationListScreen() {
        super(Text.literal("保存データ一覧"));
    }

    // ページ番号を指定するコンストラクタ
    public LocationListScreen(int currentPage) {
        this();
        this.currentPage = currentPage;
    }

    @Override
    protected void init() {
        LOGGER.info("[LocationListScreen] init() called");
        List<LocationEntry> entries = LocationDataManager.getEntries();
        int totalEntries = entries.size();
        int totalPages = (totalEntries + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;

        // 各ウィジェット生成処理を分割
        addSettingsButton(); // 設定画面への遷移ボタンを追加
        addCloseButton();
        addPaginationButtons(totalPages);
        addLocationEntryWidgets(entries, currentPage * ENTRIES_PER_PAGE, Math.min((currentPage + 1) * ENTRIES_PER_PAGE, totalEntries));
    }

    /**
     * 画面上部右側に設定画面へ遷移するボタンを追加する。（歯車アイコンを表示）
     */
    private void addSettingsButton() {
        int x = this.width - ICON_SIZE - LEFT_MARGIN;
        int y = 10;
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("⚙"), button ->
                MinecraftClient.getInstance().setScreen(new LocationSettingsScreen()))
            .dimensions(x, y, ICON_SIZE, ICON_SIZE)
            .build()
        );
    }

    /**
     * 下部中央に「閉じる」ボタンを追加する。
     */
    private void addCloseButton() {
        int x = this.width / 2 - (CLOSE_BUTTON_WIDTH / 2);
        int y = this.height - 30;
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("閉じる"), button -> MinecraftClient.getInstance().setScreen(null))
                .dimensions(x, y, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT)
                .build()
        );
    }

    /**
     * ページャー用の左右ボタンを追加する。
     *
     * @param totalPages 総ページ数
     */
    private void addPaginationButtons(int totalPages) {
        int paginationAreaY = this.height - PAGINATION_AREA_OFFSET;
        String pageInfo = (currentPage + 1) + " / " + totalPages;
        int pageInfoWidth = this.textRenderer.getWidth(pageInfo);
        int centerX = this.width / 2;

        // 左側の "<" ボタン（先頭ページでなければ）
        if (currentPage > 0) {
            int leftX = centerX - pageInfoWidth / 2 - PAGER_BUTTON_WIDTH - PAGER_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("<"), button -> 
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage - 1)))
                    .dimensions(leftX, paginationAreaY, PAGER_BUTTON_WIDTH, PAGER_BUTTON_HEIGHT)
                    .build()
            );
        }

        // 右側の ">" ボタン（最終ページでなければ）
        if (currentPage < totalPages - 1) {
            int rightX = centerX + pageInfoWidth / 2 + PAGER_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal(">"), button -> 
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage + 1)))
                    .dimensions(rightX, paginationAreaY, PAGER_BUTTON_WIDTH, PAGER_BUTTON_HEIGHT)
                    .build()
            );
        }
    }

    /**
     * 指定された範囲のエントリについて、各種操作ウィジェットを追加する。
     *
     * @param entries    全エントリリスト
     * @param startIndex 表示開始インデックス
     * @param endIndex   表示終了インデックス（非包括）
     */
    private void addLocationEntryWidgets(List<LocationEntry> entries, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int rowY = TOP_MARGIN + displayIndex * ROW_HEIGHT;
            LocationEntry entry = entries.get(i);

            // お気に入りトグルボタン（アイコン "★"）
            this.addDrawableChild(new ToggleIconButton(
                LEFT_MARGIN,
                rowY,
                ICON_SIZE,
                ICON_SIZE,
                Text.literal("★"),
                button -> {
                    entry.favorite = !entry.favorite;
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage));
                },
                entry.favorite
            ));

            // ピン留めトグルボタン（アイコン "📌"）
            this.addDrawableChild(new ToggleIconButton(
                LEFT_MARGIN + ICON_SIZE + ICON_GAP,
                rowY,
                ICON_SIZE,
                ICON_SIZE,
                Text.literal("📌"),
                button -> {
                    entry.pinned = !entry.pinned;
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage));
                },
                entry.pinned
            ));

            // シェアボタン（アイコン "🔗"）
            int shareX = LEFT_MARGIN + (ICON_SIZE + ICON_GAP) * 2;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("🔗"), button -> {
                    LocationShare.send(entry);
                })
                .dimensions(shareX, rowY, ICON_SIZE, ICON_SIZE)
                .build()
            );

            // 「説明変更」ボタン
            int descX = this.width - ICON_SIZE - LEFT_MARGIN - DESC_BUTTON_WIDTH - ICON_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("説明変更"), button -> 
                    MinecraftClient.getInstance().setScreen(new LocationDescriptionEditScreen(entry)))
                    .dimensions(descX, rowY, DESC_BUTTON_WIDTH, ICON_SIZE)
                    .build()
            );

            // 削除ボタン（ゴミ箱アイコン "🗑"）
            int deleteX = this.width - ICON_SIZE - LEFT_MARGIN;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("🗑"), button -> {
                    if (entry.favorite) {
                        LOGGER.info("お気に入りのエントリは削除できません");
                        return;
                    }
                    LocationDataManager.removeEntry(entry);
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage));
                })
                .dimensions(deleteX, rowY, ICON_SIZE, ICON_SIZE)
                .build()
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 背景描画＋タイトル描画
        renderBackground(context, mouseX, mouseY, delta);
        renderTitle(context);
        
        // 各エントリの位置情報＆説明テキストの描画
        renderEntriesText(context);
        
        // ページャーエリアのテキスト描画
        renderPaginationText(context);
        
        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * 画面上部にタイトルを描画する。
     */
    private void renderTitle(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    /**
     * 現在のページに該当するエントリのテキスト情報を描画する。
     */
    private void renderEntriesText(DrawContext context) {
        List<LocationEntry> entries = LocationDataManager.getEntries();
        int totalEntries = entries.size();
        int startIndex = currentPage * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, totalEntries);
        int textX = LEFT_MARGIN + ICON_SIZE * 3 + ICON_GAP * 3;

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int rowY = TOP_MARGIN + displayIndex * ROW_HEIGHT + 2;
            LocationEntry entry = entries.get(i);
            context.drawText(this.textRenderer, entry.getLocationText(), textX, rowY, 0xFFFFFF, true);
            context.drawText(this.textRenderer, entry.description, textX, rowY + this.textRenderer.fontHeight, 0xFFFFFF, true);
        }
    }

    /**
     * ページャー情報を画面下部に描画する。
     */
    private void renderPaginationText(DrawContext context) {
        int paginationAreaY = this.height - PAGINATION_AREA_OFFSET;
        List<LocationEntry> entries = LocationDataManager.getEntries();
        int totalEntries = entries.size();
        int totalPages = (totalEntries + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;
        String pageInfo = (currentPage + 1) + " / " + totalPages;
        int textY = paginationAreaY + (PAGER_BUTTON_HEIGHT - this.textRenderer.fontHeight) / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, pageInfo, this.width / 2, textY, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // 内部クラス：トグル状態を保持するアイコンボタン
    private class ToggleIconButton extends ButtonWidget {
        private boolean toggled;

        public ToggleIconButton(int x, int y, int width, int height, Text message, PressAction onPress, boolean toggled) {
            super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
            this.toggled = toggled;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            if (!toggled) {
                // トグルされていない場合、半透明オーバーレイを追加
                context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);
            }
        }
    }
}
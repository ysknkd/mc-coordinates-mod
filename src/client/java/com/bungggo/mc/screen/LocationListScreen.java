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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import com.bungggo.mc.network.LocationPayload;
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

        // 「閉じる」ボタン（下部中央よりさらに下に配置）
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("閉じる"), button -> {
                MinecraftClient.getInstance().setScreen(null);
            })
            .dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
            .build()
        );

        // 固定のページャーエリア（下からのオフセットを固定）
        final int paginationAreaY = this.height - 60;

        // ページ情報テキスト
        String pageInfo = (currentPage + 1) + " / " + totalPages;
        int pageInfoWidth = this.textRenderer.getWidth(pageInfo);
        int centerX = this.width / 2;
        final int pagerButtonWidth = 20;
        final int pagerButtonHeight = 20;
        final int gap = 6; // ページ情報テキストとボタンとの間隔（以前より広めに設定）
        
        // 左側の "<" ボタン（先頭ページでなければ）
        if (currentPage > 0) {
            int leftButtonX = centerX - pageInfoWidth / 2 - pagerButtonWidth - gap;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("<"), button -> {
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage - 1));
                })
                .dimensions(leftButtonX, paginationAreaY, pagerButtonWidth, pagerButtonHeight)
                .build()
            );
        }

        // 右側の ">" ボタン（最終ページでなければ）
        if (currentPage < totalPages - 1) {
            int rightButtonX = centerX + pageInfoWidth / 2 + gap;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal(">"), button -> {
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage + 1));
                })
                .dimensions(rightButtonX, paginationAreaY, pagerButtonWidth, pagerButtonHeight)
                .build()
            );
        }

        // 各エントリ用のウィジェット配置（アイコン部分のみ）
        final int ICON_SIZE = 20;
        final int ICON_GAP = 4;
        final int LEFT_MARGIN = 10;
        final int topMargin = 20;
        final int rowHeight = ICON_SIZE + 4;
        int startIndex = currentPage * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, totalEntries);
        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int rowY = topMargin + displayIndex * rowHeight;
            LocationEntry entry = entries.get(i);

            // お気に入りトグルボタン（左端、アイコン "★"）
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

            // ピン留めトグルボタン（お気に入りの右隣、アイコン "📌"）
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

            // シェアボタン（ピン留めボタンの横に配置）
            int xShare = LEFT_MARGIN + (ICON_SIZE + ICON_GAP) * 2;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("🔗"), button -> {
                    // クライアント側で LocationPayload を作成してサーバーへ送信
                    var client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        // 送信者は現在のクライアントプレイヤーの UUID を使用
                        LocationPayload payload = new LocationPayload(
                            client.player.getUuid(),
                            entry.x,
                            entry.y,
                            entry.z,
                            entry.description,
                            entry.world
                        );
                        // サーバーへ送信する（サーバー側で受信し、ブロードキャスト処理を行います）
                        ClientPlayNetworking.send(payload);
                    }
                })
                .dimensions(xShare, rowY, ICON_SIZE, ICON_SIZE)
                .build()
            );

            // 「説明変更」ボタンの配置
            final int DESC_BUTTON_WIDTH = 70;
            int xDesc = this.width - ICON_SIZE - LEFT_MARGIN - DESC_BUTTON_WIDTH - ICON_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("説明変更"), button -> {
                    MinecraftClient.getInstance().setScreen(new LocationDescriptionEditScreen(entry));
                })
                .dimensions(xDesc, rowY, DESC_BUTTON_WIDTH, ICON_SIZE)
                .build()
            );

            // 削除ボタン（右端、ゴミ箱アイコン "🗑"）
            int xDelete = this.width - ICON_SIZE - LEFT_MARGIN;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("🗑"), button -> {
                    if (entry.favorite) {
                        LOGGER.info("お気に入りのエントリは削除できません: ");
                        return;
                    }
                    LocationDataManager.removeEntry(entry);
                    MinecraftClient.getInstance().setScreen(new LocationListScreen(currentPage));
                })
                .dimensions(xDelete, rowY, ICON_SIZE, ICON_SIZE)
                .build()
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 背景とタイトル描画
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        // 各エントリの位置情報と説明文を描画
        List<LocationEntry> entries = LocationDataManager.getEntries();
        int totalEntries = entries.size();
        int startIndex = currentPage * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, totalEntries);

        final int LEFT_MARGIN = 10;
        final int topMargin = 20;
        final int ICON_SIZE = 20;
        final int ICON_GAP = 4;
        final int rowHeight = ICON_SIZE + 4;
        int textX = LEFT_MARGIN + ICON_SIZE * 2 + ICON_GAP * 2 + 5;

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int rowY = topMargin + displayIndex * rowHeight;
            LocationEntry entry = entries.get(i);
            context.drawText(this.textRenderer, entry.getLocationText(), textX, rowY, 0xFFFFFF, true);
            context.drawText(this.textRenderer, entry.description, textX, rowY + this.textRenderer.fontHeight, 0xFFFFFF, true);
        }

        // ページャーエリアは下部から固定（例：下から60px）
        final int paginationAreaY = this.height - 60;
        List<LocationEntry> allEntries = LocationDataManager.getEntries();
        int totalEntriesAll = allEntries.size();
        int totalPages = (totalEntriesAll + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;
        String pageInfo = (currentPage + 1) + " / " + totalPages;
        // ページテキストは、ボタンと同じ領域内で中央に配置
        int textY = paginationAreaY + (20 - this.textRenderer.fontHeight) / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, pageInfo, this.width / 2, textY, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
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
package dev.ysknkd.mc.coordinates.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.network.ShareCoordinatesClientHandler;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import net.minecraft.client.render.RenderLayer;

import java.util.List;
import java.util.ArrayList;

/**
 * Screen that displays the list of saved coordinate entries.
 * Uses a pager to display location information and descriptions.
 * Fixed-position buttons ("<" and ">") for pagination are placed on either side of the page number text at the bottom.
 */
@Environment(EnvType.CLIENT)
public class CoordinatesListScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatesListScreen.class);
    // Number of entries displayed per page
    private static final int ENTRIES_PER_PAGE = 6;
    // Current page number (zero-indexed)
    private int currentPage = 0;

    // Layout constants
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
    private static final int PAGINATION_AREA_OFFSET = 60;

    public CoordinatesListScreen() {
        super(Text.translatable(CoordinatesApp.MOD_ID + ".coordinates_list.title"));
    }

    public CoordinatesListScreen(int currentPage) {
        this();
        this.currentPage = currentPage;
    }

    @Override
    protected void init() {
        List<Coordinates> entries = new ArrayList<>(CoordinatesDataManager.getEntries());
        int totalEntries = entries.size();
        int totalPages = (totalEntries + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;

        addSettingsButton();
        addCloseButton();
        addPaginationButtons(totalPages);
        addWidgets(entries, currentPage * ENTRIES_PER_PAGE, Math.min((currentPage + 1) * ENTRIES_PER_PAGE, totalEntries));
    }

    private void addSettingsButton() {
        int x = this.width - ICON_SIZE - LEFT_MARGIN;
        int y = 10;
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("⚙"), button ->
                MinecraftClient.getInstance().setScreen(new SettingsScreen(this)))
            .dimensions(x, y, ICON_SIZE, ICON_SIZE)
            .build()
        );
    }

    private void addCloseButton() {
        int x = this.width / 2 - (CLOSE_BUTTON_WIDTH / 2);
        int y = this.height - 30;
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable(CoordinatesApp.MOD_ID + ".button.close"), button -> MinecraftClient.getInstance().setScreen(null))
                .dimensions(x, y, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT)
                .build()
        );
    }

    /**
     * Add left and right buttons for pager.
     *
     * @param totalPages Total number of pages
     */
    private void addPaginationButtons(int totalPages) {
        int paginationAreaY = this.height - PAGINATION_AREA_OFFSET;
        String pageInfo = (currentPage + 1) + " / " + totalPages;
        int pageInfoWidth = this.textRenderer.getWidth(pageInfo);
        int centerX = this.width / 2;

        // Left side "<" button (if not first page)
        if (currentPage > 0) {
            int leftX = centerX - pageInfoWidth / 2 - PAGER_BUTTON_WIDTH - PAGER_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("<"), button -> 
                    MinecraftClient.getInstance().setScreen(new CoordinatesListScreen(currentPage - 1)))
                    .dimensions(leftX, paginationAreaY, PAGER_BUTTON_WIDTH, PAGER_BUTTON_HEIGHT)
                    .build()
            );
        }

        // Right side ">" button (if not last page)
        if (currentPage < totalPages - 1) {
            int rightX = centerX + pageInfoWidth / 2 + PAGER_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal(">"), button -> 
                    MinecraftClient.getInstance().setScreen(new CoordinatesListScreen(currentPage + 1)))
                    .dimensions(rightX, paginationAreaY, PAGER_BUTTON_WIDTH, PAGER_BUTTON_HEIGHT)
                    .build()
            );
        }
    }

    /**
     * Add various operation widgets for specified range of entries.
     *
     * @param entries     All entry list
     * @param startIndex  Display start index
     * @param endIndex    Display end index (non-inclusive)
     */
    private void addWidgets(List<Coordinates> entries, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int rowY = TOP_MARGIN + displayIndex * ROW_HEIGHT;
            Coordinates entry = entries.get(i);

            // Favorite toggle button (use entry.icon)
            this.addDrawableChild(new ToggleIconButton(
                LEFT_MARGIN,
                rowY,
                ICON_SIZE,
                ICON_SIZE,
                Text.literal("☆"),
                button -> {
                    entry.favorite = !entry.favorite;
                    MinecraftClient.getInstance().setScreen(new CoordinatesListScreen(currentPage));
                },
                entry.favorite
            ));

            // Pin toggle button
            int pinX = LEFT_MARGIN + ICON_SIZE + ICON_GAP;
            this.addDrawableChild(new ToggleIconButton(
                pinX,
                rowY,
                ICON_SIZE,
                ICON_SIZE,
                Text.literal("📌"),
                button -> {
                    entry.pinned = !entry.pinned;
                    if (entry.share) {
                        // If valid, treat as shared state and always share
                        ShareCoordinatesClientHandler.send(entry);
                    }
                    MinecraftClient.getInstance().setScreen(new CoordinatesListScreen(currentPage));
                },
                entry.pinned
            ));

            // Share button to toggle
            int shareX = LEFT_MARGIN + (ICON_SIZE + ICON_GAP) * 2;
            this.addDrawableChild(new ToggleIconButton(
                shareX,
                rowY,
                ICON_SIZE,
                ICON_SIZE,
                Text.literal("🔗"),
                button -> {
                    entry.share = !entry.share;
                    if (entry.share) {
                        // If valid, treat as shared state and always share
                        ShareCoordinatesClientHandler.send(entry);
                    }
                    MinecraftClient.getInstance().setScreen(new CoordinatesListScreen(currentPage));
                },
                entry.share
            ));

            // "Edit Description" button
            int descX = this.width - ICON_SIZE - LEFT_MARGIN - DESC_BUTTON_WIDTH - ICON_GAP;
            this.addDrawableChild(
                ButtonWidget.builder(Text.translatable(CoordinatesApp.MOD_ID + ".button.edit_description"), button ->
                    MinecraftClient.getInstance().setScreen(new DescriptionEditScreen(this, entry)))
                    .dimensions(descX, rowY, DESC_BUTTON_WIDTH, ICON_SIZE)
                    .build()
            );

            // Delete button (Trash can icon "🗑")
            int deleteX = this.width - ICON_SIZE - LEFT_MARGIN;
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("🗑"), button -> {
                    if (entry.favorite) {
                        LOGGER.info("Cannot delete favorite entry");
                        return;
                    }
                    CoordinatesDataManager.removeEntry(entry);
                    MinecraftClient.getInstance().setScreen(new CoordinatesListScreen(currentPage));
                })
                .dimensions(deleteX, rowY, ICON_SIZE, ICON_SIZE)
                .build()
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        renderTitle(context);
        renderEntriesText(context);
        renderPaginationText(context);
    }

    /**
     * Render title at top.
     */
    private void renderTitle(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    /**
     * Render text information for entries corresponding to current page.
     */
    private void renderEntriesText(DrawContext context) {
        List<Coordinates> entries = new ArrayList<>(CoordinatesDataManager.getEntries());
        int totalEntries = entries.size();
        int startIndex = currentPage * ENTRIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ENTRIES_PER_PAGE, totalEntries);
        int x = LEFT_MARGIN + ICON_SIZE * 3 + ICON_GAP * 3;
        int iconX = x - 1; // -1 is adjust ...
        int iconSize = 8; // 8 x 8
        int descX = iconX + iconSize + 4;

        for (int i = startIndex; i < endIndex; i++) {
            Coordinates entry = entries.get(i);

            int displayIndex = i - startIndex;
            int row1y = TOP_MARGIN + displayIndex * ROW_HEIGHT + 2;
            context.drawText(this.textRenderer, entry.getCoordinatesText(), x, row1y, 0xFFFFFF, true);

            int row2y = row1y + this.textRenderer.fontHeight;
            context.drawTexture(RenderLayer::getGuiTextured, IconTexture.getIcon(entry.icon), iconX, row2y, 0, 0, iconSize, iconSize, iconSize, iconSize);
            context.drawText(this.textRenderer, entry.description, descX, row2y, 0xFFFFFF, true);
        }
    }

    /**
     * Render pagination information at bottom.
     */
    private void renderPaginationText(DrawContext context) {
        int paginationAreaY = this.height - PAGINATION_AREA_OFFSET;
        List<Coordinates> entries = new ArrayList<>(CoordinatesDataManager.getEntries());
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
                // If not toggled, add a semi-transparent overlay.
                context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80000000);
            }
        }
    }
}
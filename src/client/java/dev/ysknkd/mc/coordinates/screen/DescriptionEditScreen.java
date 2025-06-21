package dev.ysknkd.mc.coordinates.screen;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.store.Coordinates;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Screen for editing the description.
 * Allows editing the description of the target Coordinates entry and saving or canceling.
 */
@Environment(EnvType.CLIENT)
public class DescriptionEditScreen extends Screen {

    private final Screen parent;
    private final Coordinates entry;
    private TextFieldWidget textField;

    public DescriptionEditScreen(Screen parent, Coordinates entry) {
        super(Text.translatable(CoordinatesApp.MOD_ID + ".description.label"));
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    @Override
    protected void init() {
        // Place the text input field in the center
        int textFieldWidth = 200;
        int textFieldHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        textField = new TextFieldWidget(this.textRenderer, centerX - textFieldWidth / 2, centerY - textFieldHeight / 2, textFieldWidth, textFieldHeight, Text.translatable(CoordinatesApp.MOD_ID + ".description.label"));
        textField.setText(entry.description);
        textField.setChangedListener(text -> {});
        this.addSelectableChild(textField);
        textField.setFocused(true);

        // "Save" button
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable(CoordinatesApp.MOD_ID + ".button.save"), button -> {
                entry.description = textField.getText();
                close();
            })
            .dimensions(centerX - textFieldWidth / 2, centerY + textFieldHeight, textFieldWidth / 2 - 2, 20)
            .build()
        );

        // "Cancel" button
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable(CoordinatesApp.MOD_ID + ".button.cancel"), button -> {
                close();
            })
            .dimensions(centerX + 2, centerY + textFieldHeight, textFieldWidth / 2 - 2, 20)
            .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // renderBackground() is called automatically by super.render() in 1.21.6
        super.render(context, mouseX, mouseY, delta);
        
        // Draw the title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFFFF);

        // Render the text field
        textField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Forward key input to the text field
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textField.isFocused()) {
            textField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (textField.isFocused()) {
            textField.charTyped(chr, keyCode);
            return true;
        }
        return super.charTyped(chr, keyCode);
    }
}
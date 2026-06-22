package dev.ysknkd.mc.coordinates.screen;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.store.Coordinates;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * Screen for editing the description.
 * Allows editing the description of the target Coordinates entry and saving or canceling.
 */
@Environment(EnvType.CLIENT)
public class DescriptionEditScreen extends Screen {

    private final Screen parent;
    private final Coordinates entry;
    private EditBox textField;

    public DescriptionEditScreen(Screen parent, Coordinates entry) {
        super(Component.translatable(CoordinatesApp.MOD_ID + ".description.label"));
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    @Override
    protected void init() {
        // Place the text input field in the center
        int textFieldWidth = 200;
        int textFieldHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        textField = new EditBox(this.font, centerX - textFieldWidth / 2, centerY - textFieldHeight / 2, textFieldWidth, textFieldHeight, Component.translatable(CoordinatesApp.MOD_ID + ".description.label"));
        textField.setValue(entry.description);
        textField.setResponder(text -> {});
        this.addRenderableWidget(textField);
        textField.setFocused(true);

        // "Save" button
        this.addRenderableWidget(
            Button.builder(Component.translatable(CoordinatesApp.MOD_ID + ".button.save"), button -> {
                entry.description = textField.getValue();
                onClose();
            })
            .bounds(centerX - textFieldWidth / 2, centerY + textFieldHeight, textFieldWidth / 2 - 2, 20)
            .build()
        );

        // "Cancel" button
        this.addRenderableWidget(
            Button.builder(Component.translatable(CoordinatesApp.MOD_ID + ".button.cancel"), button -> {
                onClose();
            })
            .bounds(centerX + 2, centerY + textFieldHeight, textFieldWidth / 2 - 2, 20)
            .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        // Draw the title
        context.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Forward key input to the text field
    @Override
    public boolean keyPressed(KeyEvent input) {
        if (textField.isFocused()) {
            textField.keyPressed(input);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (textField.isFocused()) {
            textField.charTyped(input);
            return true;
        }
        return super.charTyped(input);
    }
}

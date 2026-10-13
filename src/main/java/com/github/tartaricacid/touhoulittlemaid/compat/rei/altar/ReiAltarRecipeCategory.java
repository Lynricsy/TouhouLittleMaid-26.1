package com.github.tartaricacid.touhoulittlemaid.compat.rei.altar;

import com.github.tartaricacid.touhoulittlemaid.util.GuiTools;
import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ReiAltarRecipeCategory implements DisplayCategory<ReiAltarRecipeDisplay> {
    private static final MutableComponent TITLE = Component.translatable("jei.touhou_little_maid.altar_craft.title");
    private static final Identifier ALTAR_ICON = IdentifierUtil.modLoc("textures/gui/altar_icon.png");
    private static final Identifier POWER_ICON = IdentifierUtil.modLoc("textures/entity/power_point.png");
    private static final int WIDTH = 160;
    private static final int HEIGHT = 125;
    private static final int TEXT_COLOR = 0xFF555555;

    private final Renderer icon = (graphics, bounds, mouseX, mouseY, delta) ->
            GuiTools.guiBlit(graphics, ALTAR_ICON, bounds.x, bounds.y, 0, 0, 16, 16, 16, 16);

    @Override
    public CategoryIdentifier<? extends ReiAltarRecipeDisplay> getCategoryIdentifier() {
        return ReiAltarRecipeDisplay.ALTAR;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public int getDisplayWidth(ReiAltarRecipeDisplay display) {
        return WIDTH;
    }

    @Override
    public List<Widget> setupDisplay(ReiAltarRecipeDisplay display, Rectangle bounds) {
        int startX = bounds.x;
        int startY = bounds.y + 5;
        List<EntryIngredient> inputs = display.getInputEntries();

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createSlot(new Point(startX + 40, startY + 35)).entries(getInput(inputs, 0)).markInput());
        widgets.add(Widgets.createSlot(new Point(startX + 40, startY + 55)).entries(getInput(inputs, 1)).markInput());
        widgets.add(Widgets.createSlot(new Point(startX + 60, startY + 15)).entries(getInput(inputs, 2)).markInput());
        widgets.add(Widgets.createSlot(new Point(startX + 80, startY + 15)).entries(getInput(inputs, 3)).markInput());
        widgets.add(Widgets.createSlot(new Point(startX + 100, startY + 35)).entries(getInput(inputs, 4)).markInput());
        widgets.add(Widgets.createSlot(new Point(startX + 100, startY + 55)).entries(getInput(inputs, 5)).markInput());
        widgets.add(Widgets.createSlot(new Point(startX + 135, startY + 5)).entries(display.getOutputEntries().getFirst()).markOutput());

        // 灵力点图标与消耗倍率
        widgets.add(Widgets.createTexturedWidget(POWER_ICON, startX + 72, startY + 38, 32, 0, 16, 16, 64, 64));
        widgets.add(Widgets.createLabel(new Point(startX + 65, startY + 55), Component.literal("×%.2f".formatted(display.getPowerCost())))
                .leftAligned().noShadow().color(TEXT_COLOR));

        // 合成结果描述，居中显示
        Component result = Component.translatable("jei.touhou_little_maid.altar_craft.result", Component.translatable(display.getLangKey()));
        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), startY + 85), result)
                .centered().noShadow().color(TEXT_COLOR));
        return widgets;
    }

    private EntryIngredient getInput(List<EntryIngredient> inputs, int index) {
        if (index < inputs.size()) {
            return inputs.get(index);
        }
        return EntryIngredient.empty();
    }
}

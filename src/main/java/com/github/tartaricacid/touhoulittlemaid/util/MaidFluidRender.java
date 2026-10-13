package com.github.tartaricacid.touhoulittlemaid.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * 在 GUI 中绘制流体，参考 JEI 的
 * <a href="https://github.com/mezz/JustEnoughItems/blob/26.1/Library/src/main/java/mezz/jei/library/render/FluidTankRenderer.java">FluidTankRenderer</a>
 */
public final class MaidFluidRender {
    private static final int TEXTURE_SIZE = 16;

    private MaidFluidRender() {
    }

    /**
     * 按容量比例从底部向上平铺绘制流体的静止贴图
     */
    public static void drawFluid(GuiGraphicsExtractor graphics, int x, int y, int width, int height, FluidStack fluidStack, int capacity) {
        if (fluidStack.isEmpty() || capacity <= 0) {
            return;
        }
        FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(fluidStack.getFluid().defaultFluidState());
        TextureAtlasSprite sprite = getStillSprite(fluidModel);
        if (sprite == null) {
            return;
        }
        int color = getColorTint(fluidModel, fluidStack);
        // 至少渲染一行像素，让人知道里面有东西
        int scaledAmount = Math.clamp((long) fluidStack.getAmount() * height / capacity, 1, height);
        int top = y + height - scaledAmount;

        graphics.enableScissor(x, top, x + width, top + scaledAmount);
        for (int tileY = y + height - TEXTURE_SIZE; tileY > top - TEXTURE_SIZE; tileY -= TEXTURE_SIZE) {
            for (int tileX = x; tileX < x + width; tileX += TEXTURE_SIZE) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, tileX, tileY, TEXTURE_SIZE, TEXTURE_SIZE, color);
            }
        }
        graphics.disableScissor();
    }

    private static int getColorTint(FluidModel fluidModel, FluidStack fluidStack) {
        FluidTintSource tintSource = fluidModel.fluidTintSource();
        if (tintSource == null) {
            return 0xFFFFFFFF;
        }
        return tintSource.colorAsStack(fluidStack);
    }

    @Nullable
    private static TextureAtlasSprite getStillSprite(FluidModel fluidModel) {
        TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();
        if (sprite == null || sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            return null;
        }
        return sprite;
    }
}

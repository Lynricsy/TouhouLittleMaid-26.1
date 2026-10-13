package com.github.tartaricacid.touhoulittlemaid.compat.patpat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lopymine.patpat.client.render.PatPatClientRenderer;
import net.minecraft.world.entity.LivingEntity;

/**
 * 唯一直接引用 PatPat 类的地方，只在 {@link PatPatCompat} 确认模组已加载后调用
 */
public class PatPatRenderer {
    static void scaleEntityIfPatted(LivingEntity livingEntity, PoseStack matrixStack, float tickDelta) {
        PatPatClientRenderer.scaleEntityIfPatted(livingEntity, matrixStack, tickDelta);
    }
}

package com.github.tartaricacid.touhoulittlemaid.util;

import com.github.tartaricacid.touhoulittlemaid.mixin.accessor.LivingEntityAccessor;
import com.github.tartaricacid.touhoulittlemaid.mixin.accessor.SwingStateAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * 26.3 将挥手状态私有化到 {@link LivingEntity.SwingState}，此类提供与旧字段等价的读取方式
 */
public final class SwingUtil {
    private SwingUtil() {
    }

    /**
     * 等价于旧版 {@code LivingEntity#swingTime}：未挥手时为 0，挥手后首个 tick 为 0 并逐 tick 递增
     */
    public static int swingTime(LivingEntity entity) {
        if (!entity.isSwinging()) {
            return 0;
        }
        SwingStateAccessor state = (SwingStateAccessor) ((LivingEntityAccessor) entity).tlmGetSwingState();
        // 旧 swingTime 等于新 ticks - 1
        return state.tlmGetTicks() - 1;
    }

    /**
     * 等价于旧版 {@code LivingEntity#swingingArm}：未挥手时视为主手
     */
    public static InteractionHand swingingArm(LivingEntity entity) {
        LivingEntity.SwingDescription swing = entity.getCurrentSwing();
        return swing == null ? InteractionHand.MAIN_HAND : swing.hand();
    }
}

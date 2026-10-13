package com.github.tartaricacid.touhoulittlemaid.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public final class MaidFluidUtil {
    private MaidFluidUtil() {
    }

    /**
     * 获取物品上的流体能力，没有则返回 null
     */
    @Nullable
    public static ResourceHandler<FluidResource> fluidOnItem(ItemStack stack) {
        return ItemAccess.forStack(stack).oneByOne().getCapability(Capabilities.Fluid.ITEM);
    }

    /**
     * 在容器槽内的物品与储罐之间交换流体，交换后的容器（如空桶、水桶）移入女仆背包。
     * <p>
     * 整个过程在同一事务内完成：流体转移失败或女仆背包放不下时全部回滚，物品留在原槽位。
     *
     * @param slots  容器槽
     * @param index  槽位索引
     * @param tank   储罐
     * @param toTank true 为容器倒入储罐，false 为储罐装入容器
     * @param maidInv 女仆可用背包
     * @return 是否成功交换
     */
    public static boolean exchange(ResourceHandler<ItemResource> slots, int index, ResourceHandler<FluidResource> tank,
                                   boolean toTank, ResourceHandler<ItemResource> maidInv) {
        if (slots.getResource(index).isEmpty()) {
            return false;
        }
        try (Transaction tx = Transaction.openRoot()) {
            ResourceHandler<FluidResource> itemFluid = ItemAccess.forHandlerIndexStrict(slots, index).getCapability(Capabilities.Fluid.ITEM);
            if (itemFluid == null) {
                return false;
            }
            int moved = toTank
                    ? ResourceHandlerUtil.move(itemFluid, tank, r -> true, Integer.MAX_VALUE, tx)
                    : ResourceHandlerUtil.move(tank, itemFluid, r -> true, Integer.MAX_VALUE, tx);
            if (moved <= 0) {
                return false;
            }
            // 交换后的容器从槽位取出，放入女仆背包
            ItemResource converted = slots.getResource(index);
            if (!converted.isEmpty()) {
                int count = slots.getAmountAsInt(index);
                int extracted = slots.extract(index, converted, count, tx);
                int inserted = ResourceHandlerUtil.insertStacking(maidInv, converted, extracted, tx);
                if (inserted < extracted) {
                    return false;
                }
            }
            tx.commit();
            return true;
        }
    }
}

package com.github.tartaricacid.touhoulittlemaid.compat.extracontainer;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Predicate;

public class MaidInventoryRef implements ContainerRef {
    @Override
    public boolean containing(EntityMaid maid, ItemStack itemToCheck) {
        // 女仆物品栏不存在 O(1) 复杂度的物品包含方法，只能遍历
        var inv = maid.getAvailableInv(false);
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stackInSlot = ItemUtil.getStack(inv, i);
            if (stackInSlot.isEmpty()) {
                continue;
            }
            if (ItemStack.isSameItemSameComponents(stackInSlot, itemToCheck)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack insert(EntityMaid maid, ItemStack itemstack, boolean simulate) {
        int count = itemstack.getCount();
        try (Transaction tx = Transaction.openRoot()) {
            ItemResource resource = ItemResource.of(itemstack);
            var inv = maid.getAvailableInv(false);
            int inserted = inv.insert(resource, count, tx);
            if (!simulate) {
                tx.commit();
            }
            // 调用方按“剩余物品”处理返回值
            return inserted >= count ? ItemStack.EMPTY : itemstack.copyWithCount(count - inserted);
        }
    }

    @Override
    public ItemStack extract(EntityMaid maid, Predicate<ItemStack> filter, int maxCount) {
        return ItemStack.EMPTY;
    }
}

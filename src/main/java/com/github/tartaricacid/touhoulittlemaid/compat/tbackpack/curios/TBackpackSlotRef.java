package com.github.tartaricacid.touhoulittlemaid.compat.tbackpack.curios;

import com.github.tartaricacid.touhoulittlemaid.compat.extracontainer.curios.CuriosSlotRef;
import com.github.tartaricacid.touhoulittlemaid.compat.tbackpack.TBackpackCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * 女仆饰品栏中旅行者背包的容器引用
 * <p>
 * 旅行者背包为背包物品注册了 {@link Capabilities.Item#ITEM}，这里只通过 NeoForge 的 transfer API 访问，不直接引用其类
 */
public class TBackpackSlotRef extends CuriosSlotRef {
    public TBackpackSlotRef(String slotType, int slotIndex) {
        super(slotType, slotIndex);
    }

    @Nullable
    private ResourceHandler<ItemResource> getBackpackHandler(EntityMaid maid) {
        ItemStack stack = getCuriosStack(maid);
        if (!TBackpackCompat.isBackpack(stack)) {
            return null;
        }
        // 直接修改饰品栏里的背包物品
        return ItemAccess.forStack(stack).getCapability(Capabilities.Item.ITEM);
    }

    @Override
    public boolean containing(EntityMaid maid, ItemStack itemToCheck) {
        ResourceHandler<ItemResource> handler = getBackpackHandler(maid);
        if (handler == null || itemToCheck.isEmpty()) {
            return false;
        }
        return ResourceHandlerUtil.contains(handler, ItemResource.of(itemToCheck));
    }

    @Override
    public ItemStack insert(EntityMaid maid, ItemStack itemStack, boolean simulate) {
        ResourceHandler<ItemResource> handler = getBackpackHandler(maid);
        if (handler == null || itemStack.isEmpty()) {
            return itemStack;
        }
        int count = itemStack.getCount();
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(itemStack), count, tx);
            if (!simulate) {
                tx.commit();
            }
            // 返回未能放入的剩余物品
            return inserted >= count ? ItemStack.EMPTY : itemStack.copyWithCount(count - inserted);
        }
    }

    @Override
    public ItemStack extract(EntityMaid maid, Predicate<ItemStack> filter, int maxCount) {
        ResourceHandler<ItemResource> handler = getBackpackHandler(maid);
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemStack stackInSlot = ItemUtil.getStack(handler, slot);
            if (stackInSlot.isEmpty() || !filter.test(stackInSlot)) {
                continue;
            }
            int itemMaxStack = stackInSlot.getMaxStackSize();
            int effectiveMaxCount = (maxCount == -1) ? itemMaxStack : Math.min(maxCount, itemMaxStack);
            int extractCount = Math.min(effectiveMaxCount, stackInSlot.getCount());
            try (Transaction tx = Transaction.openRoot()) {
                ItemResource resource = handler.getResource(slot);
                int extracted = handler.extract(slot, resource, extractCount, tx);
                tx.commit();
                return resource.toStack(extracted);
            }
        }
        return ItemStack.EMPTY;
    }
}

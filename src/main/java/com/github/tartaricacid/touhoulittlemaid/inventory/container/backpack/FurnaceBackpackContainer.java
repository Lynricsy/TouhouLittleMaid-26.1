package com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack;

import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.FurnaceBackpackData;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class FurnaceBackpackContainer extends MaidMainContainer {
    public static final MenuType<FurnaceBackpackContainer> TYPE = IMenuTypeExtension.create((windowId, inv, data) -> new FurnaceBackpackContainer(windowId, inv, data.readInt()));
    private final ContainerData data;

    public FurnaceBackpackContainer(int id, Inventory inventory, int entityId) {
        super(TYPE, id, inventory, entityId);
        if (this.maid != null) {
            // 客户端背包类型尚未同步时也要保证槽位数量与服务端一致
            FurnaceBackpackData furnaceData = this.maid.getBackpackData() instanceof FurnaceBackpackData d ? d : new FurnaceBackpackData(this.maid);
            this.data = furnaceData.getDataAccess();
            this.addSlot(new ResourceHandlerSlot(furnaceData, furnaceData::set, FurnaceBackpackData.INPUT_INDEX, 161, 101));
            this.addSlot(new FuelSlot(furnaceData, FurnaceBackpackData.FUEL_INDEX, 161, 142));
            this.addSlot(new ResultSlot(inventory.player, furnaceData, FurnaceBackpackData.OUTPUT_INDEX, 221, 121));
        } else {
            this.data = new SimpleContainerData(4);
        }
        this.addDataSlots(this.data);
    }

    @Override
    protected void addBackpackInv(Inventory inventory) {
        for (int i = 0; i < 6; i++) {
            addSlot(BackpackSlot.create(maid, 6 + i, 143 + 18 * i, 57));
        }
        for (int i = 0; i < 6; i++) {
            addSlot(BackpackSlot.create(maid, 12 + i, 143 + 18 * i, 75));
        }
    }

    public int getBurnProgress() {
        int cookingProgress = this.data.get(2);
        int cookingTotalTime = this.data.get(3);
        return cookingTotalTime != 0 && cookingProgress != 0 ? cookingProgress * 24 / cookingTotalTime : 0;
    }

    public int getLitProgress() {
        int litDuration = this.data.get(1);
        if (litDuration == 0) {
            litDuration = 200;
        }
        return this.data.get(0) * 13 / litDuration;
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    /**
     * 燃料槽：空桶只能放一个
     */
    private static class FuelSlot extends ResourceHandlerSlot {
        private FuelSlot(FurnaceBackpackData data, int index, int x, int y) {
            super(data, data::set, index, x, y);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return stack.is(Items.BUCKET) ? 1 : super.getMaxStackSize(stack);
        }
    }

    /**
     * 产物槽：不可放入，取出时触发原版熔炉产物的统计与事件
     */
    private static class ResultSlot extends ResourceHandlerSlot {
        private final Player player;
        private int removeCount;

        private ResultSlot(Player player, FurnaceBackpackData data, int index, int x, int y) {
            super(data, data::set, index, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            if (this.hasItem()) {
                this.removeCount += Math.min(amount, this.getItem().getCount());
            }
            return super.remove(amount);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            this.checkTakeAchievements(stack);
            super.onTake(player, stack);
        }

        @Override
        protected void onQuickCraft(ItemStack stack, int amount) {
            this.removeCount += amount;
            this.checkTakeAchievements(stack);
        }

        @Override
        protected void checkTakeAchievements(ItemStack stack) {
            stack.onCraftedBy(this.player, this.removeCount);
            EventHooks.firePlayerSmeltedEvent(this.player, stack, this.removeCount);
            this.removeCount = 0;
        }
    }
}

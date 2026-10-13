package com.github.tartaricacid.touhoulittlemaid.entity.backpack;

import com.github.tartaricacid.touhoulittlemaid.api.backpack.IBackpackData;
import com.github.tartaricacid.touhoulittlemaid.api.backpack.IMaidBackpack;
import com.github.tartaricacid.touhoulittlemaid.api.backpack.MaidBackpackRenderData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import com.github.tartaricacid.touhoulittlemaid.item.BackpackLevel;
import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.backpack.FurnaceBackpackRenderData;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.FurnaceBackpackData;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityTombstone;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.FurnaceBackpackContainer;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import javax.annotation.Nullable;

public class FurnaceBackpack extends IMaidBackpack {
    public static final Identifier ID = IdentifierUtil.modLoc("furnace_backpack");

    @Override
    public void onTakeOff(ItemStack stack, Player player, EntityMaid maid) {
        // 卸下时先把熔炉三格内的物品丢出
        if (maid.getBackpackData() instanceof FurnaceBackpackData furnaceData) {
            ItemsUtil.dropEntityItems(maid, furnaceData, null);
        }
        dropRelativeItems(stack, maid);
    }

    @Override
    public void onSpawnTombstone(EntityMaid maid, EntityTombstone tombstone) {
        if (maid.getBackpackData() instanceof FurnaceBackpackData furnaceData) {
            for (int i = 0; i < furnaceData.size(); i++) {
                tombstone.insertItem(ItemUtil.getStack(furnaceData, i));
            }
        }
    }

    @Override
    public MenuProvider getGuiProvider(int entityId) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Maid Furnace Container");
            }

            @Override
            public AbstractMaidContainer createMenu(int index, Inventory playerInventory, Player player) {
                return new FurnaceBackpackContainer(index, playerInventory, entityId);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        };
    }

    @Override
    public boolean hasBackpackData() {
        return true;
    }

    @Nullable
    @Override
    public IBackpackData getBackpackData(EntityMaid maid) {
        return new FurnaceBackpackData(maid);
    }

    @Override
    public int getAvailableMaxContainerIndex() {
        return BackpackLevel.FURNACE_CAPACITY;
    }

    @Override
    public MaidBackpackRenderData getRenderData() {
        return new FurnaceBackpackRenderData();
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Item getItem() {
        return InitItems.FURNACE_BACKPACK.get();
    }
}

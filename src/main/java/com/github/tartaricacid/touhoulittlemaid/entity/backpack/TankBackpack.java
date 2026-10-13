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
import com.github.tartaricacid.touhoulittlemaid.client.renderer.backpack.TankBackpackRenderData;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.TankBackpackData;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityTombstone;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.TankBackpackContainer;
import com.github.tartaricacid.touhoulittlemaid.item.ItemTankBackpack;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import javax.annotation.Nullable;

public class TankBackpack extends IMaidBackpack {
    public static final Identifier ID = IdentifierUtil.modLoc("tank");

    @Override
    public void onPutOn(ItemStack stack, Player player, EntityMaid maid) {
        // 穿上时把物品上记录的流体读入储罐
        if (maid.getBackpackData() instanceof TankBackpackData tankData) {
            ItemTankBackpack.loadTank(tankData, stack);
        }
    }

    @Override
    public void onTakeOff(ItemStack stack, Player player, EntityMaid maid) {
        if (maid.getBackpackData() instanceof TankBackpackData tankData) {
            ItemsUtil.dropEntityItems(maid, tankData.getItems(), null);
        }
        dropRelativeItems(stack, maid);
    }

    @Override
    public ItemStack getTakeOffItemStack(ItemStack stack, @Nullable Player player, EntityMaid maid) {
        // 卸下的背包物品保留储罐中的流体
        if (maid.getBackpackData() instanceof TankBackpackData tankData) {
            return ItemTankBackpack.createWithTank(tankData);
        }
        return super.getTakeOffItemStack(stack, player, maid);
    }

    @Override
    public void onSpawnTombstone(EntityMaid maid, EntityTombstone tombstone) {
        if (maid.getBackpackData() instanceof TankBackpackData tankData) {
            for (int i = 0; i < tankData.getItems().size(); i++) {
                tombstone.insertItem(ItemUtil.getStack(tankData.getItems(), i));
            }
        }
    }

    @Override
    public MenuProvider getGuiProvider(int entityId) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Maid Tank Container");
            }

            @Override
            public AbstractMaidContainer createMenu(int index, Inventory playerInventory, Player player) {
                return new TankBackpackContainer(index, playerInventory, entityId);
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
        return new TankBackpackData(maid);
    }

    @Override
    public int getAvailableMaxContainerIndex() {
        return BackpackLevel.TANK_CAPACITY;
    }

    @Override
    public MaidBackpackRenderData getRenderData() {
        return new TankBackpackRenderData();
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Item getItem() {
        return InitItems.TANK_BACKPACK.get();
    }
}

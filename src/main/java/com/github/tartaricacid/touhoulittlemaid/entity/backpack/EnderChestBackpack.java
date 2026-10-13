package com.github.tartaricacid.touhoulittlemaid.entity.backpack;

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
import com.github.tartaricacid.touhoulittlemaid.client.renderer.backpack.EnderChestBackpackRenderData;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.EnderChestBackpackContainer;

public class EnderChestBackpack extends IMaidBackpack {
    public static final Identifier ID = IdentifierUtil.modLoc("ender_chest_backpack");

    @Override
    public void onTakeOff(ItemStack stack, Player player, EntityMaid maid) {
        dropRelativeItems(stack, maid);
    }

    @Override
    public MenuProvider getGuiProvider(int entityId) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Maid Ender Chest Container");
            }

            @Override
            public AbstractMaidContainer createMenu(int index, Inventory playerInventory, Player player) {
                return new EnderChestBackpackContainer(index, playerInventory, entityId);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        };
    }

    @Override
    public int getAvailableMaxContainerIndex() {
        return BackpackLevel.EMPTY_CAPACITY;
    }

    @Override
    public MaidBackpackRenderData getRenderData() {
        return new EnderChestBackpackRenderData();
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Item getItem() {
        return InitItems.ENDER_CHEST_BACKPACK.get();
    }
}

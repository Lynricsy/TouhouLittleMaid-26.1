package com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack;

import com.github.tartaricacid.touhoulittlemaid.api.backpack.ITriggerSlotChange;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import java.util.Optional;

public class CraftingTableBackpackContainer extends MaidMainContainer {
    public static final MenuType<CraftingTableBackpackContainer> TYPE = IMenuTypeExtension.create((windowId, inv, data) -> new CraftingTableBackpackContainer(windowId, inv, data.readInt()));
    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final ResultSlot resultSlot;
    private final Player player;

    public CraftingTableBackpackContainer(int id, Inventory inventory, int entityId) {
        super(TYPE, id, inventory, entityId);
        this.player = inventory.player;
        this.access = ContainerLevelAccess.create(this.getMaid().level, this.getMaid().blockPosition());
        this.resultSlot = new ResultSlot(this.player, this.craftSlots, this.resultSlots, 0, 229, 119);
        this.addSlot(this.resultSlot);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 3, 152 + j * 18, 101 + i * 18));
            }
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.craftSlots) {
            this.slotChangedCraftingGrid();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.craftSlots));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack1 = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack2 = slot.getItem();
            stack1 = stack2.copy();
            if (index == resultSlot.index) {
                this.access.execute((level, blockPos) -> stack2.getItem().onCraftedBy(stack2, player));
                if (!this.moveItemStackTo(stack2, 0, PLAYER_INVENTORY_SIZE, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack2, stack1);
            } else if (index < resultSlot.index) {
                if (index < PLAYER_INVENTORY_SIZE) {
                    if (!this.moveItemStackTo(stack2, PLAYER_INVENTORY_SIZE, resultSlot.index, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack2, 0, PLAYER_INVENTORY_SIZE, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack2, 0, PLAYER_INVENTORY_SIZE, false)) {
                return ItemStack.EMPTY;
            }
            if (stack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack2.getCount() == stack1.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack2);
            // 触发 Shift 点击取出事件
            if (slot instanceof ITriggerSlotChange slotChange) {
                slotChange.onShiftTakeoff(player, stack1);
            }
            if (index == resultSlot.index) {
                player.drop(stack2, false, Prediction.PREDICTED);
            }
        }
        return stack1;
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

    /**
     * 26.3 的 CraftingMenu.slotChangedCraftingGrid 为 protected static 且签名已变，这里自行实现合成结果刷新
     */
    private void slotChangedCraftingGrid() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ServerLevel level = serverPlayer.level();
        ItemStack result = ItemStack.EMPTY;
        CraftingInput craftInput = this.craftSlots.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> optional = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, craftInput, level);
        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> recipe = optional.get();
            if (this.resultSlots.setRecipeUsed(serverPlayer, recipe)) {
                ItemStack assembled = recipe.value().assemble(craftInput);
                if (assembled.isItemEnabled(level.enabledFeatures())) {
                    result = assembled;
                }
            }
        }
        this.resultSlots.setItem(0, result);
        this.setRemoteSlot(this.resultSlot.index, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), this.resultSlot.index, result));
    }
}

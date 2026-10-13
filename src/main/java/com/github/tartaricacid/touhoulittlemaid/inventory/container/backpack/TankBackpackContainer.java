package com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack;

import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.TankBackpackData;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.MaidMainContainer;
import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class TankBackpackContainer extends MaidMainContainer {
    public static final MenuType<TankBackpackContainer> TYPE = IMenuTypeExtension.create((windowId, inv, data) -> new TankBackpackContainer(windowId, inv, data.readInt()));
    private static final Identifier INPUT_SLOT = IdentifierUtil.modLoc("container/slot/tank_input");
    private static final Identifier OUTPUT_SLOT = IdentifierUtil.modLoc("container/slot/tank_output");
    private final ContainerData data;

    public TankBackpackContainer(int id, Inventory inventory, int entityId) {
        super(TYPE, id, inventory, entityId);
        if (this.maid != null) {
            // 客户端背包类型尚未同步时也要保证槽位数量与服务端一致
            TankBackpackData tankData = this.maid.getBackpackData() instanceof TankBackpackData d ? d : new TankBackpackData(this.maid);
            this.data = tankData.getDataAccess();
            this.addSlot(new TankSlot(tankData.getItems(), TankBackpackData.INPUT_INDEX, 161, 101, INPUT_SLOT));
            this.addSlot(new TankSlot(tankData.getItems(), TankBackpackData.OUTPUT_INDEX, 161, 140, OUTPUT_SLOT));
        } else {
            this.data = new SimpleContainerData(2);
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

    /**
     * 由菜单同步的数据还原储罐流体，供客户端界面显示
     */
    public FluidStack getFluid() {
        int amount = this.data.get(0);
        Fluid fluid = BuiltInRegistries.FLUID.byId(this.data.get(1));
        if (amount <= 0 || fluid == null || fluid.isSame(Fluids.EMPTY)) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    private static class TankSlot extends ResourceHandlerSlot {
        private final Identifier icon;

        private TankSlot(ItemStacksResourceHandler handler, int index, int x, int y, Identifier icon) {
            super(handler, handler::set, index, x, y);
            this.icon = icon;
        }

        @Override
        public Identifier getNoItemIcon() {
            return this.icon;
        }
    }
}

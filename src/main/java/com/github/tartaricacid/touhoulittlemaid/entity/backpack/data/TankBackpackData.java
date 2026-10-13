package com.github.tartaricacid.touhoulittlemaid.entity.backpack.data;

import com.github.tartaricacid.touhoulittlemaid.api.backpack.IBackpackData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.MaidFluidUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * 储罐背包数据：一个 10 桶容量的流体储罐，以及输入（倒入储罐）、输出（从储罐装出）两个容器槽
 */
public class TankBackpackData implements IBackpackData {
    public static final int CAPACITY = 10 * FluidType.BUCKET_VOLUME;
    public static final int INPUT_INDEX = 0;
    public static final int OUTPUT_INDEX = 1;
    private static final String TANK_TAG = "Tank";
    private static final String ITEMS_TAG = "Items";

    private final EntityMaid maid;
    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, CAPACITY) {
        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            TankBackpackData.this.updateSyncData();
        }
    };
    private final ItemStacksResourceHandler items = new ItemStacksResourceHandler(2) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.isEmpty() || MaidFluidUtil.fluidOnItem(resource.toStack()) != null;
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            // 每格只放一个容器，保证流体交换时能原地替换物品
            return 1;
        }
    };
    /**
     * 同步给客户端的流体数量与流体注册 ID
     */
    private int syncAmount = 0;
    private int syncFluidId = 0;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> TankBackpackData.this.syncAmount;
                case 1 -> TankBackpackData.this.syncFluidId;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> TankBackpackData.this.syncAmount = value;
                case 1 -> TankBackpackData.this.syncFluidId = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public TankBackpackData(EntityMaid maid) {
        this.maid = maid;
    }

    public FluidStacksResourceHandler getTank() {
        return this.tank;
    }

    public ItemStacksResourceHandler getItems() {
        return this.items;
    }

    public FluidStack getFluid() {
        return FluidUtil.getStack(this.tank, 0);
    }

    /**
     * 直接设置储罐内容，用于从背包物品读取流体
     */
    public void setFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            this.tank.set(0, FluidResource.EMPTY, 0);
        } else {
            this.tank.set(0, FluidResource.of(stack), Math.min(stack.getAmount(), CAPACITY));
        }
        this.updateSyncData();
    }

    @Override
    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    @Override
    public void load(ValueInput input, EntityMaid maid) {
        this.tank.deserialize(input.childOrEmpty(TANK_TAG));
        this.items.deserialize(input.childOrEmpty(ITEMS_TAG));
        this.updateSyncData();
    }

    @Override
    public void save(ValueOutput output, EntityMaid maid) {
        this.tank.serialize(output.child(TANK_TAG));
        this.items.serialize(output.child(ITEMS_TAG));
    }

    @Override
    public void serverTick(EntityMaid maid) {
        // 输入槽：容器中的流体倒入储罐；输出槽：储罐中的流体装入容器
        MaidFluidUtil.exchange(this.items, INPUT_INDEX, this.tank, true, maid.getAvailableInv(false));
        MaidFluidUtil.exchange(this.items, OUTPUT_INDEX, this.tank, false, maid.getAvailableInv(false));
    }

    private void updateSyncData() {
        FluidStack stack = this.getFluid();
        this.syncAmount = stack.getAmount();
        this.syncFluidId = stack.isEmpty() ? 0 : BuiltInRegistries.FLUID.getId(stack.getFluid());
    }
}

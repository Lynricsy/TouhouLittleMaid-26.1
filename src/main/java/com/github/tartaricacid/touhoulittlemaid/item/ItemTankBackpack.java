package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.TankBackpackData;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.function.Consumer;

import static com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent.TANK_BACKPACK_FLUID;

public class ItemTankBackpack extends ItemMaidBackpack {
    public ItemTankBackpack(Identifier id) {
        super(id);
    }

    /**
     * 根据女仆身上的储罐数据创建背包物品，流体记录在 {@code TANK_BACKPACK_FLUID} 数据组件中
     */
    public static ItemStack createWithTank(TankBackpackData data) {
        ItemStack backpack = InitItems.TANK_BACKPACK.get().getDefaultInstance();
        FluidStack fluid = data.getFluid();
        if (!fluid.isEmpty()) {
            backpack.set(TANK_BACKPACK_FLUID, SimpleFluidContent.copyOf(fluid));
        }
        return backpack;
    }

    /**
     * 将背包物品上记录的流体读入女仆的储罐数据
     */
    public static void loadTank(TankBackpackData data, ItemStack backpack) {
        SimpleFluidContent content = backpack.getOrDefault(TANK_BACKPACK_FLUID, SimpleFluidContent.EMPTY);
        data.setFluid(content.copy());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flagIn) {
        SimpleFluidContent content = stack.getOrDefault(TANK_BACKPACK_FLUID, SimpleFluidContent.EMPTY);
        if (content.isEmpty()) {
            tooltip.accept(Component.translatable("tooltips.touhou_little_maid.tank_backpack.empty_fluid").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("tooltips.touhou_little_maid.tank_backpack.fluid",
                    content.getFluidType().getDescription(), content.getAmount()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.translatable("tooltips.touhou_little_maid.tank_backpack.capacity",
                TankBackpackData.CAPACITY).withStyle(ChatFormatting.GRAY));
    }
}

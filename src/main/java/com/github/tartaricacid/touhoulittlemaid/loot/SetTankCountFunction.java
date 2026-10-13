package com.github.tartaricacid.touhoulittlemaid.loot;

import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.TankBackpackData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.Optional;

import static com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent.TANK_BACKPACK_FLUID;

/**
 * 为储罐背包设置初始流体
 */
public class SetTankCountFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetTankCountFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
            .and(instance.group(
                    BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fluid_id").forGetter(f -> f.fluid),
                    Codec.INT.fieldOf("count").forGetter(f -> f.count)
            )).apply(instance, SetTankCountFunction::new));

    private final Holder<Fluid> fluid;
    private final int count;

    protected SetTankCountFunction(Optional<Holder<LootItemCondition>> predicates, Holder<Fluid> fluid, int count) {
        super(predicates);
        this.fluid = fluid;
        this.count = count;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (this.fluid.value().isSame(Fluids.EMPTY) || this.count <= 0) {
            stack.remove(TANK_BACKPACK_FLUID);
            return stack;
        }
        FluidStack fluidStack = new FluidStack(this.fluid, Math.min(this.count, TankBackpackData.CAPACITY));
        stack.set(TANK_BACKPACK_FLUID, SimpleFluidContent.copyOf(fluidStack));
        return stack;
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetTankCountFunction.Builder> {
        private final Fluid fluid;
        private final int bucketCount;

        public Builder(Fluid fluid, int bucketCount) {
            this.fluid = fluid;
            this.bucketCount = bucketCount;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetTankCountFunction(this.getCondition(), this.fluid.builtInRegistryHolder(), this.bucketCount * FluidType.BUCKET_VOLUME);
        }
    }
}

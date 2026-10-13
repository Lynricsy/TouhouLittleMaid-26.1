package com.github.tartaricacid.touhoulittlemaid.entity.backpack.data;

import com.github.tartaricacid.touhoulittlemaid.api.backpack.IBackpackData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidItemManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 熔炉背包数据：输入、燃料、产物三格，以及燃烧与烧制进度
 */
public class FurnaceBackpackData extends ItemStacksResourceHandler implements IBackpackData {
    public static final int INPUT_INDEX = 0;
    public static final int FUEL_INDEX = 1;
    public static final int OUTPUT_INDEX = 2;
    private static final String ITEMS_TAG = "Items";
    private static final int DEFAULT_COOK_TIME = 200;

    private final EntityMaid maid;
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);
    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FurnaceBackpackData.this.litTime;
                case 1 -> FurnaceBackpackData.this.litDuration;
                case 2 -> FurnaceBackpackData.this.cookingProgress;
                case 3 -> FurnaceBackpackData.this.cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> FurnaceBackpackData.this.litTime = value;
                case 1 -> FurnaceBackpackData.this.litDuration = value;
                case 2 -> FurnaceBackpackData.this.cookingProgress = value;
                case 3 -> FurnaceBackpackData.this.cookingTotalTime = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public FurnaceBackpackData(EntityMaid maid) {
        super(3);
        this.maid = maid;
    }

    /**
     * 判断物品能否作为熔炉背包的燃料（桶用于湿海绵烘干后接水）
     */
    public static boolean isFuel(ItemStack stack) {
        return stack.has(DataComponents.COOKING_FUEL) || stack.is(Items.BUCKET);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (resource.isEmpty()) {
            return true;
        }
        return switch (index) {
            case INPUT_INDEX -> MaidItemManager.canInsertItem(resource.toStack());
            case FUEL_INDEX -> isFuel(resource.toStack());
            default -> false;
        };
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        // 输入物品变化时重置烧制进度
        if (index == INPUT_INDEX && this.maid.level() instanceof ServerLevel serverLevel) {
            ItemStack current = this.getStackInSlot(INPUT_INDEX);
            boolean isSameItem = !current.isEmpty() && ItemStack.isSameItemSameComponents(previousContents, current);
            if (!isSameItem) {
                this.cookingTotalTime = this.getTotalCookTime(serverLevel);
                this.cookingProgress = 0;
            }
        }
    }

    public ItemStack getStackInSlot(int index) {
        return this.stacks.get(index);
    }

    public void setStackInSlot(int index, ItemStack stack) {
        if (stack.isEmpty()) {
            this.set(index, ItemResource.EMPTY, 0);
        } else {
            this.set(index, ItemResource.of(stack), stack.getCount());
        }
    }

    @Override
    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    @Override
    public void load(ValueInput input, EntityMaid maid) {
        this.litTime = input.getIntOr("BurnTime", 0);
        this.cookingProgress = input.getIntOr("CookTime", 0);
        this.cookingTotalTime = input.getIntOr("CookTimeTotal", 0);
        this.deserialize(input.childOrEmpty(ITEMS_TAG));
        if (maid.level() instanceof ServerLevel serverLevel) {
            this.litDuration = this.getBurnDuration(serverLevel, this.getStackInSlot(FUEL_INDEX));
        }
    }

    @Override
    public void save(ValueOutput output, EntityMaid maid) {
        output.putInt("BurnTime", this.litTime);
        output.putInt("CookTime", this.cookingProgress);
        output.putInt("CookTimeTotal", this.cookingTotalTime);
        this.serialize(output.child(ITEMS_TAG));
    }

    @Override
    public void serverTick(EntityMaid maid) {
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }
        // 如果是燃烧状态，继续燃烧
        if (this.isLit()) {
            --this.litTime;
        }
        ItemStack fuelItem = this.getStackInSlot(FUEL_INDEX);
        ItemStack inputItem = this.getStackInSlot(INPUT_INDEX);
        boolean inputNotEmpty = !inputItem.isEmpty();
        boolean fuelNotEmpty = !fuelItem.isEmpty();
        // 要么正在燃烧，要么具备燃烧条件
        if (this.isLit() || (inputNotEmpty && fuelNotEmpty)) {
            // 从缓存中获取配方
            SmeltingRecipe recipe = null;
            if (inputNotEmpty) {
                recipe = this.quickCheck.getRecipeFor(new SingleRecipeInput(inputItem), level).map(RecipeHolder::value).orElse(null);
            }

            // 没有燃烧，但是可以燃！
            if (!this.isLit() && this.canBurn(recipe)) {
                this.litTime = this.getBurnDuration(level, fuelItem);
                this.litDuration = this.litTime;
                // 如果此时点燃了，消耗燃料
                if (this.isLit() && fuelNotEmpty) {
                    // 燃料减一；用尽后若有残留物（如熔岩桶残留空桶）则放回燃料槽
                    ItemStackTemplate remainder = fuelItem.getCraftingRemainder();
                    ItemStack rest = fuelItem.copyWithCount(fuelItem.getCount() - 1);
                    if (rest.isEmpty() && remainder != null) {
                        rest = remainder.create();
                    }
                    this.setStackInSlot(FUEL_INDEX, rest);
                }
            }

            // 点燃了，而且也能燃！
            if (this.isLit() && this.canBurn(recipe)) {
                ++this.cookingProgress;
                // 如果进度满了，重置，并给出产物
                if (this.cookingProgress >= this.cookingTotalTime) {
                    this.cookingProgress = 0;
                    this.cookingTotalTime = this.getTotalCookTime(level);
                    // 如果烧制成功，把经验给女仆
                    if (this.burn(recipe)) {
                        int exp = this.createExperience(recipe.experience());
                        maid.setExperience(maid.getExperience() + exp);
                    }
                }
            } else {
                // 否则直接重置烧制进度
                this.cookingProgress = 0;
            }
        } else if (this.cookingProgress > 0) {
            // 燃料不足，逐 tick 递减烧制进度
            this.cookingProgress = Mth.clamp(this.cookingProgress - 2, 0, this.cookingTotalTime);
        }
    }

    private int createExperience(float recipeExp) {
        int integer = Mth.floor(recipeExp);
        float decimal = Mth.frac(recipeExp);
        if (decimal != 0 && this.maid.getRandom().nextFloat() < decimal) {
            ++integer;
        }
        return integer;
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    /**
     * 燃料燃烧时长由 {@link DataComponents#COOKING_FUEL} 数据组件给出，需要构建熔炉同款战利品上下文求值。
     * <p>
     * CONTAINER_PROCESS 参数集强制要求方块状态与方块实体，女仆背包没有真实方块，
     * 因此以位于女仆位置、未放置到世界中的原版熔炉方块实体作为替身
     */
    private int getBurnDuration(ServerLevel level, ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        BlockState furnaceState = Blocks.FURNACE.defaultBlockState();
        FurnaceBlockEntity furnace = new FurnaceBlockEntity(this.maid.blockPosition(), furnaceState);
        furnace.setLevel(level);
        LootContext context = new LootContext.Builder(new LootParams.Builder(level)
                .withParameter(LootContextParams.BLOCK_STATE, furnaceState)
                .withParameter(LootContextParams.BLOCK_ENTITY, furnace)
                .withParameter(LootContextParams.ORIGIN, this.maid.position())
                .withParameter(LootContextParams.CONTAINER, this.maid)
                .withOptionalParameter(NeoForgeLootContextParams.QUERIED_STACK, fuel)
                .create(LootContextParamSets.CONTAINER_PROCESS)).create(Optional.empty());
        return ResolvableInt.getFromItem(fuel, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
    }

    private boolean canBurn(@Nullable SmeltingRecipe recipe) {
        ItemStack input = this.getStackInSlot(INPUT_INDEX);
        if (input.isEmpty() || recipe == null) {
            return false;
        }
        ItemStack result = recipe.assemble(new SingleRecipeInput(input));
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = this.getStackInSlot(OUTPUT_INDEX);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private boolean burn(@Nullable SmeltingRecipe recipe) {
        if (recipe == null || !this.canBurn(recipe)) {
            return false;
        }
        ItemStack input = this.getStackInSlot(INPUT_INDEX);
        ItemStack result = recipe.assemble(new SingleRecipeInput(input));
        ItemStack output = this.getStackInSlot(OUTPUT_INDEX);
        if (output.isEmpty()) {
            this.setStackInSlot(OUTPUT_INDEX, result.copy());
        } else {
            this.setStackInSlot(OUTPUT_INDEX, output.copyWithCount(output.getCount() + result.getCount()));
        }
        // 湿海绵烘干时，燃料槽的空桶会接到水
        ItemStack fuel = this.getStackInSlot(FUEL_INDEX);
        if (input.is(Blocks.WET_SPONGE.asItem()) && fuel.is(Items.BUCKET)) {
            this.setStackInSlot(FUEL_INDEX, new ItemStack(Items.WATER_BUCKET));
        }
        this.setStackInSlot(INPUT_INDEX, input.copyWithCount(input.getCount() - 1));
        return true;
    }

    private int getTotalCookTime(ServerLevel level) {
        ItemStack input = this.getStackInSlot(INPUT_INDEX);
        if (input.isEmpty()) {
            return DEFAULT_COOK_TIME;
        }
        return this.quickCheck.getRecipeFor(new SingleRecipeInput(input), level)
                .map(holder -> holder.value().cookingTime())
                .orElse(DEFAULT_COOK_TIME);
    }
}

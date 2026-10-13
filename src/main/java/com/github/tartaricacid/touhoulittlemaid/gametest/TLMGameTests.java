package com.github.tartaricacid.touhoulittlemaid.gametest;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidStack;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBoardState;
import com.github.tartaricacid.touhoulittlemaid.blockentity.BlockEntityGomoku;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.TankBackpackData;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.TankBackpack;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidSchedule;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.backpack.IBackpackData;
import com.github.tartaricacid.touhoulittlemaid.block.BlockMaidBed;
import com.github.tartaricacid.touhoulittlemaid.block.multiblock.MultiBlockAltar;
import com.github.tartaricacid.touhoulittlemaid.blockentity.BlockEntityAltar;
import com.github.tartaricacid.touhoulittlemaid.data.PowerAttachment;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.BackpackManager;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.BigBackpack;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.FurnaceBackpack;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.data.FurnaceBackpackData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitBlocks;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataAttachment;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.item.BackpackLevel;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.github.tartaricacid.touhoulittlemaid.util.MaidItemStorageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Consumer;

/**
 * 服务端 GameTest 用例。测试实例定义在 data/touhou_little_maid/test_instance 下，
 * 结构统一使用 16x16x16 的 touhou_little_maid:game_test（y=0 为草方块地面）。
 */
public final class TLMGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> TESTS =
            DeferredRegister.create(Registries.TEST_FUNCTION, TouhouLittleMaid.MOD_ID);

    static {
        TESTS.register("smart_slab_spawn", () -> TLMGameTests::smartSlabSpawn);
        TESTS.register("maid_sleep_in_bed", () -> TLMGameTests::maidSleepInBed);
        TESTS.register("backpack_equip_and_capacity", () -> TLMGameTests::backpackEquipAndCapacity);
        TESTS.register("furnace_backpack_smelt", () -> TLMGameTests::furnaceBackpackSmelt);
        TESTS.register("altar_craft", () -> TLMGameTests::altarCraft);
        TESTS.register("maid_store_restore", () -> TLMGameTests::maidStoreRestore);
        TESTS.register("tank_backpack_fill", () -> TLMGameTests::tankBackpackFill);
        TESTS.register("gomoku_board_state_load", () -> TLMGameTests::gomokuBoardStateLoad);
    }

    private TLMGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        TESTS.register(modEventBus);
    }

    /**
     * 魂符对地面使用后生成归属于玩家的女仆
     */
    private static void smartSlabSpawn(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack slab = InitItems.SMART_SLAB_INIT.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, slab);

        BlockPos ground = helper.absolutePos(new BlockPos(4, 0, 4));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false);
        slab.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, slab, hit));

        helper.succeedWhen(() -> {
            List<EntityMaid> maids = helper.getEntities(InitEntities.MAID.get());
            helper.assertTrue(maids.size() == 1, Component.literal("Expected exactly one maid, found " + maids.size()));
            EntityMaid maid = maids.getFirst();
            helper.assertTrue(maid.getOwnerReference() != null && maid.getOwnerReference().getUUID().equals(player.getUUID()),
                    Component.literal("Maid is not owned by the player"));
        });
    }

    /**
     * 女仆床继承 AbstractBedBlock 后，原版睡眠逻辑认可女仆床，女仆入睡后不会因 checkBedExists 失败而立刻醒来。
     * 测试环境为夜晚，女仆设为白班日程，先等大脑切换到 REST，避免被非休息活动的清醒任务叫醒
     */
    private static void maidSleepInBed(GameTestHelper helper) {
        BlockPos footRel = new BlockPos(4, 1, 5);
        BlockPos headRel = footRel.north();
        BlockState foot = InitBlocks.PINK_MAID_BED.get().defaultBlockState()
                .setValue(BlockMaidBed.FACING, Direction.NORTH)
                .setValue(BlockMaidBed.PART, BedPart.FOOT);
        helper.setBlock(footRel, foot);
        helper.setBlock(headRel, foot.setValue(BlockMaidBed.PART, BedPart.HEAD));

        EntityMaid maid = helper.spawn(InitEntities.MAID.get(), new BlockPos(4, 1, 7));
        // 新女仆默认全天工作，切换为白班日程才会在夜晚休息
        maid.setSchedule(MaidSchedule.DAY);
        BlockPos headPos = helper.absolutePos(headRel);

        helper.runAfterDelay(40, () -> {
            helper.assertTrue(maid.getBrain().isActive(Activity.REST), Component.literal("Maid brain is not resting at night: " + maid.getBrain().getActiveActivities()
                    + ", schedule=" + maid.getScheduleDetail() + ", dayTime=" + helper.getLevel().getOverworldClockTime()));
            helper.assertTrue(maid.startSleeping(headPos), Component.literal("startSleeping rejected the maid bed"));
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(maid.isSleeping(), Component.literal("Maid woke up: maid bed not recognized as a bed"));
                helper.assertTrue(helper.getLevel().getBlockState(headPos).getValue(BlockMaidBed.OCCUPIED),
                        Component.literal("Maid bed is not marked as occupied"));
                helper.succeed();
            });
        });
    }

    /**
     * 装备大背包后可用容量变为大背包容量
     */
    private static void backpackEquipAndCapacity(GameTestHelper helper) {
        EntityMaid maid = helper.spawn(InitEntities.MAID.get(), new BlockPos(4, 1, 4));
        helper.assertTrue(maid.getMaidBackpackType().getAvailableMaxContainerIndex() == BackpackLevel.EMPTY_CAPACITY,
                Component.literal("New maid should have the empty backpack capacity"));

        maid.setMaidBackpackType(BackpackManager.findBackpack(BigBackpack.ID).orElseThrow());
        helper.assertTrue(maid.getMaidBackpackType().getAvailableMaxContainerIndex() == BackpackLevel.BIG_CAPACITY,
                Component.literal("Big backpack capacity was not applied"));
        helper.succeed();
    }

    /**
     * 熔炉背包使用煤炭冶炼粗铁，覆盖 COOKING_FUEL 数据组件的燃料解析与熔炼配方查询
     */
    private static void furnaceBackpackSmelt(GameTestHelper helper) {
        EntityMaid maid = helper.spawn(InitEntities.MAID.get(), new BlockPos(4, 1, 4));
        maid.setMaidBackpackType(BackpackManager.findBackpack(FurnaceBackpack.ID).orElseThrow());
        FurnaceBackpackData furnace = getFurnaceData(helper, maid);
        furnace.setStackInSlot(FurnaceBackpackData.INPUT_INDEX, new ItemStack(Items.RAW_IRON));
        furnace.setStackInSlot(FurnaceBackpackData.FUEL_INDEX, new ItemStack(Items.COAL));

        helper.succeedWhen(() -> {
            ItemStack output = furnace.getStackInSlot(FurnaceBackpackData.OUTPUT_INDEX);
            helper.assertTrue(output.is(Items.IRON_INGOT), Component.literal("Furnace backpack output is " + output));
        });
    }

    /**
     * 构建祭坛多方块结构并合成扫帚，覆盖 26.3 配方查询与产物生成
     */
    private static void altarCraft(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MultiBlockAltar altar = new MultiBlockAltar();
        StructureTemplate template = altar.getTemplate(level, Direction.SOUTH);
        BlockPos start = helper.absolutePos(new BlockPos(1, 1, 1));
        template.placeInWorld(level, start, start, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_ALL);
        altar.build(level, start, Direction.SOUTH, template);

        BlockEntityAltar anyAltar = findAltar(helper, level, start, template);
        List<BlockPos> slots = anyAltar.getCanPlaceItemPosList().getData();
        helper.assertTrue(slots.size() == 6, Component.literal("Altar should have 6 item slots, found " + slots.size()));

        List<ItemStack> inputs = List.of(new ItemStack(Items.HAY_BLOCK), new ItemStack(Items.HAY_BLOCK),
                new ItemStack(Items.HAY_BLOCK), new ItemStack(Items.STICK), new ItemStack(Items.STICK));
        for (int i = 0; i < inputs.size(); i++) {
            if (level.getBlockEntity(slots.get(i)) instanceof BlockEntityAltar slot) {
                ItemsUtil.setStackInSlot(slot.handler, 0, inputs.get(i));
                slot.refresh();
            }
        }

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setData(InitDataAttachment.POWER_NUM, new PowerAttachment(PowerAttachment.MAX_POWER));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ENDER_EYE));
        helper.useBlock(helper.relativePos(slots.get(inputs.size())), player);

        helper.succeedWhen(() -> {
            boolean hasBroom = helper.getEntities(EntityTypes.ITEM).stream()
                    .map(ItemEntity::getItem)
                    .anyMatch(stack -> stack.is(InitItems.BROOM.get()));
            helper.assertTrue(hasBroom, Component.literal("Altar did not craft a broom"));
        });
    }

    /**
     * 女仆存入物品再放出，模型、名称与功能背包数据经 ValueIO 往返后保持一致
     */
    private static void maidStoreRestore(GameTestHelper helper) {
        EntityMaid maid = helper.spawn(InitEntities.MAID.get(), new BlockPos(4, 1, 4));
        maid.setModelId("touhou_little_maid:hakurei_reimu");
        maid.setCustomName(Component.literal("TLM GameTest"));
        maid.setMaidBackpackType(BackpackManager.findBackpack(FurnaceBackpack.ID).orElseThrow());
        getFurnaceData(helper, maid).setStackInSlot(FurnaceBackpackData.INPUT_INDEX, new ItemStack(Items.RAW_GOLD, 3));

        ItemStack photo = InitItems.PHOTO.get().getDefaultInstance();
        MaidItemStorageHelper.saveMaid(photo, maid, tag -> {
        });
        CompoundTag maidTag = photo.getOrDefault(InitDataComponent.MAID_INFO, CustomData.EMPTY).copyTag();

        EntityMaid restored = InitEntities.MAID.get().create(helper.getLevel(), EntitySpawnReason.SPAWN_ITEM_USE);
        helper.assertTrue(restored != null, Component.literal("Failed to create maid"));
        MaidItemStorageHelper.loadMaid(photo, restored, maidTag);

        helper.assertTrue(maid.getModelId().equals(restored.getModelId()), Component.literal("Model id changed after restore"));
        helper.assertTrue(restored.getCustomName() != null && "TLM GameTest".equals(restored.getCustomName().getString()),
                Component.literal("Custom name changed after restore"));
        helper.assertTrue(!restored.isYsmModel(), Component.literal("YSM data should stay default without YSM"));
        ItemStack restoredInput = getFurnaceData(helper, restored).getStackInSlot(FurnaceBackpackData.INPUT_INDEX);
        helper.assertTrue(restoredInput.is(Items.RAW_GOLD) && restoredInput.getCount() == 3,
                Component.literal("Furnace backpack data lost after restore: " + restoredInput));
        helper.succeed();
    }

    /**
     * 储罐背包输入槽放入水桶后，水倒入储罐，空桶进入女仆背包
     */
    private static void tankBackpackFill(GameTestHelper helper) {
        EntityMaid maid = helper.spawn(InitEntities.MAID.get(), new BlockPos(4, 1, 4));
        maid.setMaidBackpackType(BackpackManager.findBackpack(TankBackpack.ID).orElseThrow());
        IBackpackData data = maid.getBackpackData();
        helper.assertTrue(data instanceof TankBackpackData, Component.literal("Tank backpack data missing: " + data));
        TankBackpackData tank = (TankBackpackData) data;
        ItemsUtil.setStackInSlot(tank.getItems(), TankBackpackData.INPUT_INDEX, new ItemStack(Items.WATER_BUCKET));

        helper.succeedWhen(() -> {
            FluidStack fluid = tank.getFluid();
            helper.assertTrue(fluid.is(Fluids.WATER) && fluid.getAmount() == FluidType.BUCKET_VOLUME,
                    Component.literal("Tank should hold one bucket of water, got " + fluid.getAmount()));
            boolean hasBucket = false;
            var inv = maid.getAvailableInv(false);
            for (int i = 0; i < inv.size(); i++) {
                hasBucket |= ItemUtil.getStack(inv, i).is(Items.BUCKET);
            }
            helper.assertTrue(hasBucket, Component.literal("Empty bucket was not moved into the maid inventory"));
        });
    }

    /**
     * 手持五子棋残局道具右键棋盘，棋盘载入残局棋子
     */
    private static void gomokuBoardStateLoad(GameTestHelper helper) {
        BlockPos centerRel = new BlockPos(7, 1, 7);
        helper.setBlock(centerRel, InitBlocks.GOMOKU.get().defaultBlockState());
        BlockEntityGomoku gomoku = helper.getBlockEntity(centerRel, BlockEntityGomoku.class);
        helper.assertTrue(countStones(gomoku) == 0, Component.literal("New gomoku board should be empty"));

        ItemStack boardState = InitItems.GOMOKU_BOARD_STATE.get().getDefaultInstance();
        // 数据取自 data/touhou_little_maid/board_states/gomoku.json 的“月亮阵”
        ItemBoardState.setState(boardState, "eNpjYKAvYMTPZ8SreAgAdP8w0twTAAhFAAk, 8, 8_9", "moon", "test");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, boardState);
        helper.useBlock(centerRel, player);

        helper.assertTrue(countStones(gomoku) > 0, Component.literal("Board state was not loaded into the gomoku board"));
        helper.succeed();
    }

    private static int countStones(BlockEntityGomoku gomoku) {
        int count = 0;
        for (byte[] row : gomoku.getChessData()) {
            for (byte b : row) {
                if (b != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private static FurnaceBackpackData getFurnaceData(GameTestHelper helper, EntityMaid maid) {
        IBackpackData data = maid.getBackpackData();
        helper.assertTrue(data instanceof FurnaceBackpackData, Component.literal("Furnace backpack data missing: " + data));
        return (FurnaceBackpackData) data;
    }

    private static BlockEntityAltar findAltar(GameTestHelper helper, ServerLevel level, BlockPos start, StructureTemplate template) {
        for (StructureTemplate.StructureBlockInfo info : template.palettes.getFirst().blocks()) {
            if (level.getBlockEntity(start.offset(info.pos())) instanceof BlockEntityAltar altar) {
                return altar;
            }
        }
        helper.fail("Altar structure was not built");
        throw new IllegalStateException("unreachable");
    }
}

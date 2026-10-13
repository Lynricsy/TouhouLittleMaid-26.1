package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.blockentity.BlockEntityMaidBed;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * 女仆床：继承原版 {@link AbstractBedBlock}，使原版睡眠逻辑（startSleeping/checkBedExists/setPosToBed）
 * 将其识别为床；玩家无法在其上睡觉。
 */
public class BlockMaidBed extends AbstractBedBlock implements EntityBlock {
    public static final Map<DyeColor, BlockMaidBed> COLOR_TO_BLOCK = new EnumMap<>(DyeColor.class);

    private static final VoxelShape BASE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

    private final DyeColor color;

    public BlockMaidBed(Identifier id, DyeColor color) {
        super(BlockBehaviour.Properties.of()
                .mapColor(color)
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .sound(SoundType.WOOD)
                .strength(0.2F)
                .bounceRestitution(0.75F)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, BedPart.FOOT)
                .setValue(OCCUPIED, false));
        this.color = color;
        COLOR_TO_BLOCK.put(color, this);
    }

    /**
     * 根据颜色获取对应的方块实例
     */
    @Nullable
    public static BlockMaidBed getByColor(DyeColor color) {
        return COLOR_TO_BLOCK.get(color);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return BASE;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, LevelReader level, ScheduledTickAccess ticks,
                                  BlockPos pos, Direction facing, BlockPos neighbourPos,
                                  BlockState neighbourState, RandomSource random) {
        if (facing == getNeighbourDirection(stateIn.getValue(PART), stateIn.getValue(FACING))) {
            return neighbourState.is(this) && neighbourState.getValue(PART) != stateIn.getValue(PART)
                    ? stateIn.setValue(OCCUPIED, neighbourState.getValue(OCCUPIED))
                    : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(stateIn, level, ticks, pos, facing, neighbourPos, neighbourState, random);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.isClientSide()) {
            return super.playerWillDestroy(worldIn, pos, state, player);
        }
        if (!player.isCreative()) {
            return super.playerWillDestroy(worldIn, pos, state, player);
        }
        BedPart bedpart = state.getValue(PART);
        if (bedpart != BedPart.FOOT) {
            return super.playerWillDestroy(worldIn, pos, state, player);
        }
        BlockPos blockpos = pos.relative(getNeighbourDirection(bedpart, state.getValue(FACING)));
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (blockstate.getBlock() == this && blockstate.getValue(PART) == BedPart.HEAD) {
            worldIn.setBlock(blockpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            worldIn.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, blockpos, Block.getId(blockstate));
        }
        return super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        BlockPos relativePos = context.getClickedPos().relative(direction);
        BlockState blockState = context.getLevel().getBlockState(relativePos);
        return blockState.canBeReplaced(context) ? this.defaultBlockState().setValue(FACING, direction) : null;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isClientSide()) {
            return;
        }

        BlockPos headPos = pos.relative(state.getValue(FACING));
        worldIn.setBlock(headPos, state.setValue(PART, BedPart.HEAD), Block.UPDATE_ALL);
        worldIn.updateNeighborsAt(pos, Blocks.AIR);
        state.updateNeighbourShapes(worldIn, pos, Block.UPDATE_ALL);

        if (worldIn.getBlockEntity(headPos) instanceof BlockEntityMaidBed bed) {
            bed.setColor(this.color);
        }
    }

    @Override
    public void fallOn(Level worldIn, BlockState blockState, BlockPos pos, Entity entityIn, double fallDistance) {
        super.fallOn(worldIn, blockState, pos, entityIn, fallDistance * 0.5f);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.POPPED;
    }

    @Override
    protected long getSeed(BlockState state, BlockPos pos) {
        BlockPos sourcePos = pos.relative(state.getValue(FACING), state.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(sourcePos.getX(), pos.getY(), sourcePos.getZ());
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }


    private Direction getNeighbourDirection(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // 女仆床不供玩家睡觉
        return InteractionResult.PASS;
    }

    @Override
    protected EnvironmentAttribute<BedRule> getBedEnvironmentAttribute() {
        return EnvironmentAttributes.BED_RULE;
    }

    @Override
    protected InteractionResult destroyOnUse(BlockState state, Level level, BlockPos pos, Player player) {
        return InteractionResult.PASS;
    }

    @Override
    protected void destroyOnLeave(Level level, BlockPos pos) {
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == BedPart.HEAD) {
            return new BlockEntityMaidBed(pos, state);
        }
        return null;
    }

    public DyeColor getColor() {
        return color;
    }
}

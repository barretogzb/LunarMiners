package com.lunarminers.block;

import com.lunarminers.energy.CableBlockEntity;
import com.lunarminers.registry.ModBlockEntities;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/** Cabo de energia arredondado que se conecta sozinho a cabos e máquinas encostadas. */
public class CableBlock extends BaseEntityBlock {
    private static final Map<Direction, BooleanProperty> LADOS = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final VoxelShape NUCLEO = Block.box(5.5, 5.5, 5.5, 10.5, 10.5, 10.5);
    private static final Map<Direction, VoxelShape> BRACOS = new HashMap<>();
    private final Map<BlockState, VoxelShape> cache = new java.util.concurrent.ConcurrentHashMap<>();

    static {
        BRACOS.put(Direction.DOWN, Block.box(6, 0, 6, 10, 6, 10));
        BRACOS.put(Direction.UP, Block.box(6, 10, 6, 10, 16, 10));
        BRACOS.put(Direction.NORTH, Block.box(6, 6, 0, 10, 10, 6));
        BRACOS.put(Direction.SOUTH, Block.box(6, 6, 10, 10, 10, 16));
        BRACOS.put(Direction.WEST, Block.box(0, 6, 6, 6, 10, 10));
        BRACOS.put(Direction.EAST, Block.box(10, 6, 6, 16, 10, 10));
    }

    public CableBlock(Properties properties) {
        super(properties);
        BlockState s = stateDefinition.any();
        for (BooleanProperty p : LADOS.values()) {
            s = s.setValue(p, false);
        }
        registerDefaultState(s);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        LADOS.values().forEach(b::add);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static boolean conecta(BlockGetter level, BlockPos pos, Direction d) {
        BlockPos viz = pos.relative(d);
        if (level.getBlockState(viz).getBlock() instanceof CableBlock) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(viz);
        return be != null && be.getCapability(ForgeCapabilities.ENERGY, d.getOpposite()).isPresent();
    }

    private BlockState conexoes(BlockGetter level, BlockPos pos, BlockState state) {
        for (Direction d : Direction.values()) {
            state = state.setValue(LADOS.get(d), conecta(level, pos, d));
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return conexoes(ctx.getLevel(), ctx.getClickedPos(), defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState vizinho, LevelAccessor level,
                                  BlockPos pos, BlockPos vizPos) {
        return state.setValue(LADOS.get(dir), conecta(level, pos, dir));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return cache.computeIfAbsent(state, s -> {
            VoxelShape forma = NUCLEO;
            for (Direction d : Direction.values()) {
                if (s.getValue(LADOS.get(d))) {
                    forma = Shapes.or(forma, BRACOS.get(d));
                }
            }
            return forma;
        });
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CABLE.get(), CableBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CableBlockEntity be) {
            player.displayClientMessage(Component.translatable("message.lunarminers.cable",
                    EnergyMachineBlock.formatar(be.energia().getEnergyStored())), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

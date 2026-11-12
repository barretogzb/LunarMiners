package com.lunarminers.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Porta de Eclusa: antes de abrir, pressuriza/despressuriza (chiado, vapor, 2,5 s).
 * Nunca abre se outra porta de eclusa por perto estiver aberta — você precisa passar
 * pela câmara: entra, fecha a primeira, cicla, e só então abre a segunda.
 * Fecha sozinha depois de alguns segundos.
 */
public class AirlockDoorBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty CYCLING = BooleanProperty.create("cycling");
    private static final int TEMPO_CICLO = 50;
    private static final int TEMPO_ABERTA = 100;
    private static final int RAIO_TRAVA = 10;

    private static final VoxelShape NS = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape EW = Block.box(6, 0, 0, 10, 16, 16);
    private static final VoxelShape NS_ABERTA = Shapes.or(Block.box(0, 0, 6, 2, 16, 10), Block.box(14, 0, 6, 16, 16, 10));
    private static final VoxelShape EW_ABERTA = Shapes.or(Block.box(6, 0, 0, 10, 16, 2), Block.box(6, 0, 14, 10, 16, 16));

    public AirlockDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER).setValue(OPEN, false).setValue(CYCLING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, HALF, OPEN, CYCLING);
    }

    private static boolean eixoZ(BlockState s) {
        return s.getValue(FACING).getAxis() == Direction.Axis.Z;
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (s.getValue(OPEN)) {
            return eixoZ(s) ? NS_ABERTA : EW_ABERTA;
        }
        return eixoZ(s) ? NS : EW;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(ctx)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState vizinho, LevelAccessor level,
                                  BlockPos pos, BlockPos vizPos) {
        DoubleBlockHalf metade = state.getValue(HALF);
        boolean parceiro = (metade == DoubleBlockHalf.LOWER && dir == Direction.UP)
                || (metade == DoubleBlockHalf.UPPER && dir == Direction.DOWN);
        if (parceiro && !(vizinho.is(this) && vizinho.getValue(HALF) != metade)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    private static BlockPos base(BlockState s, BlockPos pos) {
        return s.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }

    private void definir(Level level, BlockPos baixo, boolean aberta, boolean ciclando) {
        BlockState s = level.getBlockState(baixo);
        if (!s.is(this)) {
            return;
        }
        BlockState novo = s.setValue(OPEN, aberta).setValue(CYCLING, ciclando);
        level.setBlock(baixo, novo, 10);
        BlockState cima = level.getBlockState(baixo.above());
        if (cima.is(this)) {
            level.setBlock(baixo.above(), cima.setValue(OPEN, aberta).setValue(CYCLING, ciclando), 10);
        }
    }

    private boolean outraAberta(Level level, BlockPos baixo) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -RAIO_TRAVA; dx <= RAIO_TRAVA; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -RAIO_TRAVA; dz <= RAIO_TRAVA; dz++) {
                    p.set(baixo.getX() + dx, baixo.getY() + dy, baixo.getZ() + dz);
                    if (p.equals(baixo) || p.equals(baixo.above())) {
                        continue;
                    }
                    BlockState s = level.getBlockState(p);
                    if (s.is(this) && (s.getValue(OPEN) || s.getValue(CYCLING))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos baixo = base(state, pos);
        if (state.getValue(CYCLING)) {
            player.displayClientMessage(Component.translatable("message.lunarminers.airlock_cycling"), true);
        } else if (state.getValue(OPEN)) {
            definir(level, baixo, false, false);
            level.playSound(null, baixo, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.6F);
        } else if (outraAberta(level, baixo)) {
            player.displayClientMessage(Component.translatable("message.lunarminers.airlock_locked"), true);
            level.playSound(null, baixo, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
        } else {
            definir(level, baixo, false, true);
            level.playSound(null, baixo, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.5F);
            level.playSound(null, baixo, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.6F, 1.6F);
            player.displayClientMessage(Component.translatable("message.lunarminers.airlock_start"), true);
            level.scheduleTick(baixo, this, TEMPO_CICLO);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return;
        }
        if (state.getValue(CYCLING)) {
            definir(level, pos, true, false);
            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 0.7F);
            level.scheduleTick(pos, this, TEMPO_ABERTA);
        } else if (state.getValue(OPEN)) {
            definir(level, pos, false, false);
            level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 1.0F, 0.7F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(CYCLING)) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.CLOUD, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                        pos.getZ() + random.nextDouble(), 0, 0.02, 0);
            }
        }
    }
}

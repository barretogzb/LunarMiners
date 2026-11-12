package com.lunarminers.block;

import com.lunarminers.blockentity.DistributorBlockEntity;
import com.lunarminers.blockentity.ElectrolyzerBlockEntity;
import com.lunarminers.blockentity.OxygenTankBlockEntity;
import com.lunarminers.energy.BatteryBlockEntity;
import com.lunarminers.energy.He3GeneratorBlockEntity;
import com.lunarminers.energy.SolarPanelBlockEntity;
import com.lunarminers.registry.ModBlockEntities;
import com.lunarminers.registry.ModItems;
import com.lunarminers.world.OxygenData;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Todas as máquinas de energia e oxigênio da base. */
public final class Machines {
    private Machines() {}

    // ------------------------------------------------------------------ painel solar
    public static class SolarPanel extends EnergyMachineBlock {
        public SolarPanel(Properties p, VoxelShape s) {
            super(p, s);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new SolarPanelBlockEntity(pos, state);
        }

        @Nullable
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SOLAR_PANEL.get(), SolarPanelBlockEntity::serverTick);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof SolarPanelBlockEntity be) {
                boolean sol = SolarPanelBlockEntity.temSol(level, pos);
                status(player, Component.translatable(sol ? "message.lunarminers.solar_on" : "message.lunarminers.solar_off",
                        SolarPanelBlockEntity.GERACAO, fe(be)));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    // ------------------------------------------------------------------ bateria
    public static class Battery extends EnergyMachineBlock {
        public Battery(Properties p, VoxelShape s) {
            super(p, s);
            registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(LEVEL);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new BatteryBlockEntity(pos, state);
        }

        @Nullable
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.BATTERY.get(), BatteryBlockEntity::serverTick);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof BatteryBlockEntity be) {
                status(player, Component.translatable("message.lunarminers.battery",
                        Math.round(be.energia().fracao() * 100), fe(be)));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    // ------------------------------------------------------------------ gerador de hélio-3
    public static class He3Generator extends EnergyMachineBlock {
        public He3Generator(Properties p, VoxelShape s) {
            super(p, s);
            registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LIT, false)
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(BlockStateProperties.LIT, BlockStateProperties.HORIZONTAL_FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new He3GeneratorBlockEntity(pos, state);
        }

        @Nullable
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.HE3_GENERATOR.get(), He3GeneratorBlockEntity::serverTick);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (level.isClientSide || !(level.getBlockEntity(pos) instanceof He3GeneratorBlockEntity be)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            ItemStack mao = player.getItemInHand(hand);
            if (mao.is(ModItems.HELIUM3_CRYSTAL.get())) {
                if (be.adicionarCristal()) {
                    if (!player.getAbilities().instabuild) {
                        mao.shrink(1);
                    }
                }
            }
            status(player, Component.translatable("message.lunarminers.he3",
                    be.combustivelTicks() / 20, fe(be)));
            return InteractionResult.CONSUME;
        }
    }

    // ------------------------------------------------------------------ eletrolisador
    public static class Electrolyzer extends EnergyMachineBlock {
        public Electrolyzer(Properties p, VoxelShape s) {
            super(p, s);
            registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LIT, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(BlockStateProperties.LIT);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new ElectrolyzerBlockEntity(pos, state);
        }

        @Nullable
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ELECTROLYZER.get(), ElectrolyzerBlockEntity::serverTick);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (level.isClientSide || !(level.getBlockEntity(pos) instanceof ElectrolyzerBlockEntity be)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            ItemStack mao = player.getItemInHand(hand);
            int valor = mao.is(ModItems.ICE_SHARD.get()) ? 1
                    : mao.is(com.lunarminers.registry.ModBlocks.LUNAR_ICE.get().asItem()) ? 4
                    : mao.is(Items.WATER_BUCKET) ? 4 : 0;
            if (valor > 0) {
                if (mao.is(Items.WATER_BUCKET)) {
                    if (be.adicionarGelo(4) > 0 && !player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                } else {
                    int unidades = player.isShiftKeyDown() ? mao.getCount() : 1;
                    int aceito = 0;
                    for (int i = 0; i < unidades; i++) {
                        if (be.adicionarGelo(valor) < valor) {
                            break;
                        }
                        aceito++;
                    }
                    if (!player.getAbilities().instabuild) {
                        mao.shrink(aceito);
                    }
                }
            }
            int o2 = OxygenData.oxigenioPerto((ServerLevel) level, pos);
            status(player, Component.translatable("message.lunarminers.electrolyzer", be.gelo(), fe(be),
                    EnergyMachineBlock.formatar(o2)));
            return InteractionResult.CONSUME;
        }
    }

    // ------------------------------------------------------------------ distribuidor de oxigênio
    public static class Distributor extends EnergyMachineBlock {
        public Distributor(Properties p, VoxelShape s) {
            super(p, s);
            registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LIT, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(BlockStateProperties.LIT);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new DistributorBlockEntity(pos, state);
        }

        @Nullable
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.DISTRIBUTOR.get(), DistributorBlockEntity::serverTick);
        }

        @Override
        public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
            super.onPlace(state, level, pos, old, moving);
            if (level instanceof ServerLevel server) {
                OxygenData.get(server).adicionar(pos);
            }
        }

        @Override
        public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
            if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
                OxygenData.get(server).remover(pos);
            }
            super.onRemove(state, level, pos, newState, moving);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof DistributorBlockEntity be) {
                OxygenData.get(server).adicionar(pos);
                String chave = be.ativo() ? "message.lunarminers.distributor_on"
                        : "energy".equals(be.motivo()) ? "message.lunarminers.distributor_no_energy"
                        : "message.lunarminers.distributor_no_oxygen";
                status(player, Component.translatable(chave, EnergyMachineBlock.formatar(be.oxigenioDisponivel()),
                        fe(be), OxygenData.RAIO_AR));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    // ------------------------------------------------------------------ válvula do tanque
    public static class TankValve extends EnergyMachineBlock {
        public TankValve(Properties p, VoxelShape s) {
            super(p, s);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new OxygenTankBlockEntity(pos, state);
        }

        @Override
        public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
            super.onPlace(state, level, pos, old, moving);
            if (level instanceof ServerLevel server) {
                OxygenData.get(server).adicionarTanque(pos);
            }
        }

        @Override
        public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
            if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
                OxygenData.get(server).removerTanque(pos);
            }
            super.onRemove(state, level, pos, newState, moving);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof OxygenTankBlockEntity be) {
                OxygenData.get(server).adicionarTanque(pos);
                status(player, Component.translatable("message.lunarminers.tank",
                        EnergyMachineBlock.formatar(be.oxigenio()), EnergyMachineBlock.formatar(be.capacidade())));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }
}

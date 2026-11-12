package com.lunarminers.block;

import com.lunarminers.world.OxygenData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Distribuidor de Oxigênio: espalha ar respirável em volta, puxando dos tanques próximos. */
public class OxygenDistributorBlock extends ShapedBlock {

    public OxygenDistributorBlock(Properties properties, VoxelShape shape) {
        super(properties, shape);
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            OxygenData.get(server).adicionar(pos);
            int tanques = OxygenData.contarTanques(server, pos);
            player.displayClientMessage(tanques > 0
                    ? Component.translatable("message.lunarminers.distributor_on", tanques, OxygenData.RAIO_AR)
                    : Component.translatable("message.lunarminers.distributor_off", OxygenData.RAIO_TANQUE), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

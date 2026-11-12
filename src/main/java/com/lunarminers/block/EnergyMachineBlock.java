package com.lunarminers.block;

import com.lunarminers.energy.EnergyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Base das máquinas com energia: formato próprio, modelo 3D e mensagem de status. */
public abstract class EnergyMachineBlock extends BaseEntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("charge", 0, 4);
    private final VoxelShape shape;

    protected EnergyMachineBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    protected static void status(Player player, Component msg) {
        player.displayClientMessage(msg, true);
    }

    protected static String fe(EnergyBlockEntity be) {
        return formatar(be.energia().getEnergyStored()) + " / " + formatar(be.energia().getMaxEnergyStored()) + " FE";
    }

    public static String formatar(int v) {
        if (v >= 1_000_000) {
            return String.format("%.1fM", v / 1_000_000.0);
        }
        if (v >= 1000) {
            return String.format("%.1fk", v / 1000.0);
        }
        return Integer.toString(v);
    }
}

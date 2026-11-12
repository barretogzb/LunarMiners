package com.lunarminers.block;

import com.lunarminers.menu.AssemblyBenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Bancada de Montagem: a mesa de fabricação da base (grade 3x3). */
public class AssemblyBenchBlock extends ShapedBlock {
    private static final Component TITLE = Component.translatable("container.lunarminers.assembly_bench");

    public AssemblyBenchBlock(Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new AssemblyBenchMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                TITLE));
        return InteractionResult.CONSUME;
    }
}

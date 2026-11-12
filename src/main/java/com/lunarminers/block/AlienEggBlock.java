package com.lunarminers.block;

import com.lunarminers.entity.AlienEntity;
import com.lunarminers.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ovo alienígena: quando um jogador chega perto, ele racha e um alienígena sai. */
public class AlienEggBlock extends ShapedBlock {
    private static final double RAIO = 5.0D;

    public AlienEggBlock(Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, RAIO, false);
        if (p != null && !p.isCreative() && !p.isSpectator()) {
            chocar(level, pos);
        } else {
            level.scheduleTick(pos, this, 20 + random.nextInt(10));
        }
    }

    public static void chocar(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.HOSTILE, 1.0F, 0.5F);
        level.sendParticles(ParticleTypes.ITEM_SLIME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.1);
        AlienEntity a = ModEntities.ALIEN.get().create(level);
        if (a != null) {
            a.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);
            level.addFreshEntity(a);
        }
    }

}

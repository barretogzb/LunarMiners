package com.lunarminers.world;

import com.lunarminers.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Cava uma cratera em forma de tigela com uma borda elevada de regolito. */
public class CraterFeature extends Feature<NoneFeatureConfiguration> {

    public CraterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();

        int radius = 4 + random.nextInt(9); // 4 a 12 blocos
        double depthScale = 0.4D + random.nextDouble() * 0.2D;
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState dust = ModBlocks.MOON_REGOLITH.get().defaultBlockState();
        BlockState darkDust = ModBlocks.MOON_MARE_REGOLITH.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int rim = 2;
        for (int dx = -radius - rim; dx <= radius + rim; dx++) {
            for (int dz = -radius - rim; dz <= radius + rim; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius + rim) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (top <= level.getMinBuildHeight() + 8) {
                    continue;
                }

                if (dist < radius) {
                    int depth = (int) Math.round(Math.sqrt(radius * radius - dist * dist) * depthScale);
                    for (int y = top; y > top - depth; y--) {
                        level.setBlock(pos.set(x, y, z), air, 2);
                    }
                    pos.set(x, top - depth, z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, dist < radius * 0.5D ? darkDust : dust, 2);
                    }
                } else {
                    int height = (int) Math.round(radius + rim - dist);
                    for (int y = 1; y <= height; y++) {
                        level.setBlock(pos.set(x, top + y, z), dust, 2);
                    }
                }
            }
        }
        return true;
    }
}

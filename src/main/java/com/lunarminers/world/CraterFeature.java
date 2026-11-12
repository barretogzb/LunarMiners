package com.lunarminers.world;

import com.lunarminers.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
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
        if (artificialPerto(level, origin, radius + rim)) {
            return false;
        }
        BlockState ice = ModBlocks.LUNAR_ICE.get().defaultBlockState();
        boolean temGelo = radius >= 9 && random.nextFloat() < 0.35F;
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
                        boolean centro = dist < radius * 0.5D;
                        level.setBlock(pos, centro && temGelo ? ice : (centro ? darkDust : dust), 2);
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

    /** Não cava crateras em cima de construções (bases, postos, bandeiras). */
    private static boolean artificialPerto(WorldGenLevel level, BlockPos origin, int raio) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -raio; dx <= raio; dx += 2) {
            for (int dz = -raio; dz <= raio; dz += 2) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int y = top - 3; y <= top + 1; y++) {
                    BlockState s = level.getBlockState(p.set(x, y, z));
                    if (!natural(s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean natural(BlockState s) {
        if (s.isAir() || s.is(Blocks.BEDROCK) || s.is(Blocks.LAVA)) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(s.getBlock());
        return id != null && id.getNamespace().equals("lunarminers")
                && (id.getPath().startsWith("moon_") || id.getPath().equals("lunar_ice"));
    }
}

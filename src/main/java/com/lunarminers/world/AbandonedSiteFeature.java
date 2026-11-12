package com.lunarminers.world;

import com.lunarminers.LunarMiners;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Locais abandonados por outras missões: módulos de pouso com baú de suprimentos,
 * ou só uma bandeira fincada com um marco. Cada um com a bandeira de um país.
 */
public class AbandonedSiteFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation LOOT = new ResourceLocation(LunarMiners.MOD_ID, "chests/abandoned_site");

    public AbandonedSiteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();
        BlockPos o = ctx.origin();
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, o.getX(), o.getZ());
        BlockPos base = new BlockPos(o.getX(), y, o.getZ());
        if (y <= level.getMinBuildHeight() + 10) {
            return false;
        }
        if (random.nextFloat() < 0.65F) {
            modulo(level, base, random);
        } else {
            marco(level, base, random);
        }
        return true;
    }

    private static void apoiar(WorldGenLevel level, BlockPos pos, BlockState bloco) {
        for (int d = 1; d <= 6; d++) {
            BlockPos p = pos.below(d);
            if (!level.getBlockState(p).canBeReplaced()) {
                break;
            }
            level.setBlock(p, bloco, 2);
        }
    }

    private static void modulo(WorldGenLevel level, BlockPos b, RandomSource random) {
        BlockState casco = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState perna = Blocks.IRON_BARS.defaultBlockState();
        BlockState ar = Blocks.AIR.defaultBlockState();
        BlockState rocha = com.lunarminers.registry.ModBlocks.MOON_ROCK.get().defaultBlockState();
        boolean tombado = random.nextFloat() < 0.3F;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int h = 0; h <= 4; h++) {
                    level.setBlock(b.offset(dx, h, dz), ar, 2);
                }
            }
        }
        // pernas nos cantos
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            BlockPos p = b.offset(c[0], 0, c[1]);
            apoiar(level, p, rocha);
            int altura = tombado && c[0] > 0 ? 1 : 2;
            for (int h = 0; h < altura; h++) {
                level.setBlock(p.above(h), perna, 2);
            }
        }
        // corpo 3x3x2 com cúpula de vidro
        int yCorpo = tombado ? 1 : 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(b.offset(dx, yCorpo, dz), casco, 2);
                boolean meio = dx == 0 && dz == 0;
                level.setBlock(b.offset(dx, yCorpo + 1, dz), meio ? Blocks.GLASS.defaultBlockState() : casco, 2);
            }
        }
        level.setBlock(b.offset(0, yCorpo + 2, 0), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        // baú de suprimentos embaixo
        BlockPos bau = b.offset(0, 0, 0);
        apoiar(level, bau, rocha);
        level.setBlock(bau, Blocks.CHEST.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.from2DDataValue(random.nextInt(4))), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, bau, LOOT);
        // bandeira ao lado
        BlockPos flag = b.offset(3, 0, random.nextInt(3) - 1);
        apoiar(level, flag, rocha);
        Bandeiras.aleatoria(random).colocar(level, flag, random.nextInt(16));
    }

    private static void marco(WorldGenLevel level, BlockPos b, RandomSource random) {
        BlockState rocha = com.lunarminers.registry.ModBlocks.MOON_ROCK.get().defaultBlockState();
        apoiar(level, b, rocha);
        Bandeiras.aleatoria(random).colocar(level, b, random.nextInt(16));
        // pegadas de regolito escuro e um marco de pedra
        BlockPos marco = b.offset(2, 0, 1);
        apoiar(level, marco, rocha);
        level.setBlock(marco, Blocks.POLISHED_ANDESITE_WALL.defaultBlockState(), 2);
        level.setBlock(marco.above(), Blocks.LANTERN.defaultBlockState(), 2);
        BlockState pegada = com.lunarminers.registry.ModBlocks.MOON_MARE_REGOLITH.get().defaultBlockState();
        for (int i = 1; i <= 6; i++) {
            BlockPos p = b.offset(-i, -1, (i % 2));
            if (!level.getBlockState(p).isAir()) {
                level.setBlock(p, pegada, 2);
            }
        }
    }
}

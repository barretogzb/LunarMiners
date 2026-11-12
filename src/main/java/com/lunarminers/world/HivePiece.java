package com.lunarminers.world;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.AlienQueenEntity;
import com.lunarminers.registry.ModBlocks;
import com.lunarminers.registry.ModEntities;
import com.lunarminers.registry.ModStructures;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Colmeia: grande cúpula orgânica de resina, meio enterrada, com veios brilhantes,
 * buracos de entrada, túneis, ovos pelo chão e a câmara central onde mora a Rainha.
 */
public class HivePiece extends StructurePiece {
    private static final int RAIO = 15;
    private static final ResourceLocation LOOT = new ResourceLocation(LunarMiners.MOD_ID, "chests/hive");

    private final BlockPos centro;
    private final int variante;

    public HivePiece(BlockPos centro, int variante) {
        super(ModStructures.HIVE_PIECE.get(), 0, new BoundingBox(
                centro.getX() - RAIO - 8, centro.getY() - 2, centro.getZ() - RAIO - 8,
                centro.getX() + RAIO + 8, centro.getY() + RAIO + 2, centro.getZ() + RAIO + 8));
        this.centro = centro;
        this.variante = variante;
        setOrientation(null);
    }

    public HivePiece(CompoundTag tag) {
        super(ModStructures.HIVE_PIECE.get(), tag);
        this.centro = new BlockPos(tag.getInt("cx"), tag.getInt("cy"), tag.getInt("cz"));
        this.variante = tag.getInt("var");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("cx", centro.getX());
        tag.putInt("cy", centro.getY());
        tag.putInt("cz", centro.getZ());
        tag.putInt("var", variante);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                            RandomSource random, BoundingBox caixa, ChunkPos chunkPos, BlockPos pivot) {
        Construtor b = new Construtor(level, caixa::isInside);
        Random rnd = new Random(variante);
        BlockState resina = ModBlocks.ALIEN_RESIN.get().defaultBlockState();
        BlockState brilho = ModBlocks.ALIEN_RESIN_GLOW.get().defaultBlockState();
        BlockState ar = Blocks.AIR.defaultBlockState();
        BlockState ovo = ModBlocks.ALIEN_EGG.get().defaultBlockState();

        // cúpula orgânica: casca grossa e irregular
        for (int dx = -RAIO - 2; dx <= RAIO + 2; dx++) {
            for (int dz = -RAIO - 2; dz <= RAIO + 2; dz++) {
                for (int h = -1; h <= RAIO + 2; h++) {
                    BlockPos p = centro.offset(dx, h, dz);
                    if (!b.pode(p)) {
                        continue;
                    }
                    double ruido = Math.sin(dx * 0.7 + variante) * 0.8 + Math.cos(dz * 0.6 + h * 0.4) * 0.8;
                    double raio = RAIO + ruido;
                    double dist = Math.sqrt(dx * dx + dz * dz + (h * 1.25) * (h * 1.25));
                    if (h < 0) {
                        if (dx * dx + dz * dz <= raio * raio) {
                            b.set(p, resina);
                        }
                    } else if (dist < raio - 2) {
                        b.set(p, ar);
                    } else if (dist <= raio) {
                        long semente = p.asLong() * 31 + variante;
                        boolean veio = Math.floorMod(semente, 9) == 0;
                        b.set(p, veio ? brilho : resina);
                    }
                }
            }
        }
        // entradas: túneis largos nos quatro lados
        for (int lado = 0; lado < 4; lado++) {
            double ang = lado * Math.PI / 2 + rnd.nextDouble() * 0.4;
            for (int t = RAIO - 4; t <= RAIO + 6; t++) {
                int ex = (int) Math.round(Math.cos(ang) * t);
                int ez = (int) Math.round(Math.sin(ang) * t);
                for (int a = -2; a <= 2; a++) {
                    for (int h = 0; h <= 3; h++) {
                        int subida = (int) ((t - (RAIO - 4)) * 0.6);
                        BlockPos p = centro.offset(ex + (int) Math.round(-Math.sin(ang) * a), h + subida,
                                ez + (int) Math.round(Math.cos(ang) * a));
                        b.set(p, ar);
                    }
                }
            }
        }
        // pilares orgânicos internos e ovos pelo chão
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3;
            BlockPos base = centro.offset((int) (Math.cos(ang) * 8), 0, (int) (Math.sin(ang) * 8));
            for (int h = 0; h < 9; h++) {
                b.set(base.above(h), h % 3 == 1 ? brilho : resina);
            }
        }
        for (int i = 0; i < 26; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            double r = 3 + rnd.nextDouble() * (RAIO - 6);
            BlockPos p = centro.offset((int) (Math.cos(ang) * r), 0, (int) (Math.sin(ang) * r));
            if (b.pode(p) && level.getBlockState(p).isAir()) {
                b.set(p, ovo);
                level.scheduleTick(p, ModBlocks.ALIEN_EGG.get(), 20);
            }
        }
        // tesouro: restos de exploradores que não voltaram
        BlockPos bau = centro.offset(-3, 0, -3);
        if (b.pode(bau)) {
            b.set(bau, ModBlocks.SUPPLY_CRATE.get().defaultBlockState());
            RandomizableContainerBlockEntity.setLootTable(level, random, bau, LOOT);
        }
        // a Rainha nasce no centro (só uma vez, quando a chunk do centro é gerada)
        if (caixa.isInside(centro)) {
            AlienQueenEntity rainha = ModEntities.ALIEN_QUEEN.get().create(level.getLevel());
            if (rainha != null) {
                rainha.moveTo(centro.getX() + 0.5, centro.getY(), centro.getZ() + 0.5, rnd.nextFloat() * 360, 0);
                rainha.finalizeSpawn(level, level.getCurrentDifficultyAt(centro), MobSpawnType.STRUCTURE, null, null);
                level.addFreshEntityWithPassengers(rainha);
            }
        }
    }
}

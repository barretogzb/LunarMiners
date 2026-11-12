package com.lunarminers.world;

import com.lunarminers.registry.ModStructures;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Colmeia alienígena: meio enterrada, longe da base (no mínimo 700 blocos). */
public class HiveStructure extends Structure {
    public static final Codec<HiveStructure> CODEC = simpleCodec(HiveStructure::new);
    private static final long DISTANCIA_MINIMA = 700L;

    public HiveStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos cp = ctx.chunkPos();
        int x = cp.getMiddleBlockX();
        int z = cp.getMiddleBlockZ();
        if ((long) x * x + (long) z * z < DISTANCIA_MINIMA * DISTANCIA_MINIMA) {
            return Optional.empty();
        }
        int y = ctx.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                ctx.heightAccessor(), ctx.randomState());
        if (y <= ctx.heightAccessor().getMinBuildHeight() + 30) {
            return Optional.empty();
        }
        BlockPos pos = new BlockPos(x, y - 5, z);
        int variante = ctx.random().nextInt(1 << 20);
        return Optional.of(new GenerationStub(pos, b -> b.addPiece(new HivePiece(pos, variante))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.HIVE.get();
    }
}

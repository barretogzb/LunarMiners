package com.lunarminers.world;

import com.lunarminers.LunarMiners;
import com.lunarminers.registry.ModBlocks;
import com.lunarminers.registry.ModStructures;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Um posto abandonado redondo: cúpula danificada, plataforma de pouso com módulo,
 * tanques de oxigênio, contêineres com suprimentos e a bandeira do país fincada.
 */
public class OutpostPiece extends StructurePiece {
    private static final int RAIO = 22;
    private static final ResourceLocation LOOT = new ResourceLocation(LunarMiners.MOD_ID, "chests/abandoned_site");

    private final BlockPos centro;
    private final int variante;

    public OutpostPiece(BlockPos centro, int variante) {
        super(ModStructures.OUTPOST_PIECE.get(), 0, new BoundingBox(
                centro.getX() - RAIO, centro.getY() - 1, centro.getZ() - RAIO,
                centro.getX() + RAIO, centro.getY() + 14, centro.getZ() + RAIO));
        this.centro = centro;
        this.variante = variante;
        setOrientation(null);
    }

    public OutpostPiece(CompoundTag tag) {
        super(ModStructures.OUTPOST_PIECE.get(), tag);
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
        Bandeiras pais = Bandeiras.values()[1 + rnd.nextInt(Bandeiras.values().length - 1)];
        int raioCupula = 6 + rnd.nextInt(3);

        b.aplainar(centro, RAIO - 2, 12);
        b.cupula(centro, raioCupula, 0.25, new Random(variante + 1));

        // interior: contêineres e um distribuidor desligado (sem tanque)
        crate(b, centro.offset(-2, 0, -raioCupula + 3), random);
        crate(b, centro.offset(2, 0, -raioCupula + 3), random);
        b.set(centro.offset(0, 0, 2), ModBlocks.ROUND_TABLE.get().defaultBlockState());
        b.set(centro.offset(1, 0, 2), ModBlocks.STOOL.get().defaultBlockState());
        b.set(centro.offset(0, 0, -1), ModBlocks.OXYGEN_DISTRIBUTOR.get().defaultBlockState());

        // plataforma de pouso com o módulo que trouxe a missão
        BlockPos pouso = centro.offset(raioCupula + 9, 0, 0);
        b.pisoCircular(pouso, 5, ModBlocks.HABITAT_FLOOR.get().defaultBlockState());
        b.pisoCircular(pouso, 3, ModBlocks.LANDING_PAD.get().defaultBlockState());
        modulo(b, pouso);

        // tanques vazios e a bandeira
        for (int i = 0; i < 3; i++) {
            b.tanque(centro.offset(-raioCupula - 4, 0, -2 + i * 2), 2);
        }
        Direction frente = Direction.from2DDataValue(rnd.nextInt(4));
        pais.fincar(b.colocador(), centro.offset(-2, 0, raioCupula + 5), frente, 6);
        crate(b, pouso.offset(0, 0, 4), random);
    }

    private static void crate(Construtor b, BlockPos p, RandomSource random) {
        if (!b.pode(p)) {
            return;
        }
        b.set(p, ModBlocks.SUPPLY_CRATE.get().defaultBlockState());
        RandomizableContainerBlockEntity.setLootTable(b.level, random, p, LOOT);
    }

    private static void modulo(Construtor b, BlockPos p) {
        var perna = ModBlocks.FLAG_POLE.get().defaultBlockState();
        var casco = ModBlocks.HABITAT_WALL.get().defaultBlockState();
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            b.set(p.offset(c[0], 0, c[1]), perna);
            b.set(p.offset(c[0], 1, c[1]), perna);
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                b.set(p.offset(dx, 2, dz), casco);
                boolean meio = dx == 0 && dz == 0;
                b.set(p.offset(dx, 3, dz), meio ? ModBlocks.HABITAT_GLASS.get().defaultBlockState() : casco);
            }
        }
        b.set(p.offset(0, 4, 0), ModBlocks.SOLAR_PANEL.get().defaultBlockState());
    }
}

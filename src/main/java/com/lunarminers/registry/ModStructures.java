package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.world.OutpostPiece;
import com.lunarminers.world.OutpostStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, LunarMiners.MOD_ID);
    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, LunarMiners.MOD_ID);

    public static final RegistryObject<StructureType<OutpostStructure>> OUTPOST =
            TYPES.register("abandoned_outpost", () -> () -> OutpostStructure.CODEC);

    public static final RegistryObject<StructurePieceType> OUTPOST_PIECE =
            PIECES.register("abandoned_outpost", () -> (StructurePieceType.ContextlessType) OutpostPiece::new);

    public static final RegistryObject<StructureType<com.lunarminers.world.HiveStructure>> HIVE =
            TYPES.register("alien_hive", () -> () -> com.lunarminers.world.HiveStructure.CODEC);

    public static final RegistryObject<StructurePieceType> HIVE_PIECE =
            PIECES.register("alien_hive", () -> (StructurePieceType.ContextlessType) com.lunarminers.world.HivePiece::new);

    private ModStructures() {}
}

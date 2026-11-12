package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.blockentity.SolarFurnaceBlockEntity;
import com.lunarminers.blockentity.SupplyCrateBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LunarMiners.MOD_ID);

    public static final RegistryObject<BlockEntityType<SolarFurnaceBlockEntity>> SOLAR_FURNACE =
            BLOCK_ENTITIES.register("solar_furnace", () -> BlockEntityType.Builder
                    .of(SolarFurnaceBlockEntity::new, ModBlocks.SOLAR_FURNACE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SupplyCrateBlockEntity>> SUPPLY_CRATE =
            BLOCK_ENTITIES.register("supply_crate", () -> BlockEntityType.Builder
                    .of(SupplyCrateBlockEntity::new, ModBlocks.SUPPLY_CRATE.get()).build(null));

    private ModBlockEntities() {}
}

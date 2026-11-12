package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.blockentity.DistributorBlockEntity;
import com.lunarminers.blockentity.ElectrolyzerBlockEntity;
import com.lunarminers.blockentity.OxygenTankBlockEntity;
import com.lunarminers.blockentity.SolarFurnaceBlockEntity;
import com.lunarminers.energy.BatteryBlockEntity;
import com.lunarminers.energy.CableBlockEntity;
import com.lunarminers.energy.He3GeneratorBlockEntity;
import com.lunarminers.energy.SolarPanelBlockEntity;
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

    public static final RegistryObject<BlockEntityType<CableBlockEntity>> CABLE =
            BLOCK_ENTITIES.register("cable", () -> BlockEntityType.Builder
                    .of(CableBlockEntity::new, ModBlocks.CABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BatteryBlockEntity>> BATTERY =
            BLOCK_ENTITIES.register("battery", () -> BlockEntityType.Builder
                    .of(BatteryBlockEntity::new, ModBlocks.BATTERY.get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL =
            BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder
                    .of(SolarPanelBlockEntity::new, ModBlocks.SOLAR_PANEL.get(), ModBlocks.SOLAR_ARRAY.get()).build(null));
    public static final RegistryObject<BlockEntityType<He3GeneratorBlockEntity>> HE3_GENERATOR =
            BLOCK_ENTITIES.register("he3_generator", () -> BlockEntityType.Builder
                    .of(He3GeneratorBlockEntity::new, ModBlocks.HE3_GENERATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<ElectrolyzerBlockEntity>> ELECTROLYZER =
            BLOCK_ENTITIES.register("electrolyzer", () -> BlockEntityType.Builder
                    .of(ElectrolyzerBlockEntity::new, ModBlocks.ELECTROLYZER.get()).build(null));
    public static final RegistryObject<BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR =
            BLOCK_ENTITIES.register("oxygen_distributor", () -> BlockEntityType.Builder
                    .of(DistributorBlockEntity::new, ModBlocks.OXYGEN_DISTRIBUTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<OxygenTankBlockEntity>> OXYGEN_TANK =
            BLOCK_ENTITIES.register("oxygen_tank", () -> BlockEntityType.Builder
                    .of(OxygenTankBlockEntity::new, ModBlocks.OXYGEN_TANK_CAP.get()).build(null));

    public static final RegistryObject<BlockEntityType<com.lunarminers.blockentity.CommsTowerBlockEntity>> COMMS_TOWER =
            BLOCK_ENTITIES.register("comms_tower", () -> BlockEntityType.Builder
                    .of(com.lunarminers.blockentity.CommsTowerBlockEntity::new, ModBlocks.COMMS_TOWER.get()).build(null));

    private ModBlockEntities() {}
}

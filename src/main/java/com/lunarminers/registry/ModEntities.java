package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.AlienEntity;
import com.lunarminers.entity.RoverEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LunarMiners.MOD_ID);

    public static final RegistryObject<EntityType<AlienEntity>> ALIEN = ENTITIES.register("alien",
            () -> EntityType.Builder.<AlienEntity>of(AlienEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("alien"));

    public static final RegistryObject<EntityType<RoverEntity>> ROVER = ENTITIES.register("rover",
            () -> EntityType.Builder.<RoverEntity>of(RoverEntity::new, MobCategory.MISC)
                    .sized(3.0F, 1.6F)
                    .clientTrackingRange(10)
                    .build("rover"));

    public static final RegistryObject<EntityType<com.lunarminers.entity.ImpactEntity>> IMPACT = ENTITIES.register("impact",
            () -> EntityType.Builder.<com.lunarminers.entity.ImpactEntity>of(com.lunarminers.entity.ImpactEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.2F)
                    .clientTrackingRange(16)
                    .updateInterval(2)
                    .build("impact"));

    public static final RegistryObject<EntityType<com.lunarminers.entity.AlienSpitterEntity>> ALIEN_SPITTER = ENTITIES.register("alien_spitter",
            () -> EntityType.Builder.<com.lunarminers.entity.AlienSpitterEntity>of(com.lunarminers.entity.AlienSpitterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(8).build("alien_spitter"));

    public static final RegistryObject<EntityType<com.lunarminers.entity.AlienQueenEntity>> ALIEN_QUEEN = ENTITIES.register("alien_queen",
            () -> EntityType.Builder.<com.lunarminers.entity.AlienQueenEntity>of(com.lunarminers.entity.AlienQueenEntity::new, MobCategory.MONSTER)
                    .sized(1.5F, 5.0F).clientTrackingRange(12).fireImmune().build("alien_queen"));

    public static final RegistryObject<EntityType<com.lunarminers.entity.AcidBlobEntity>> ACID_BLOB = ENTITIES.register("acid_blob",
            () -> EntityType.Builder.<com.lunarminers.entity.AcidBlobEntity>of(com.lunarminers.entity.AcidBlobEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F).clientTrackingRange(6).updateInterval(10).build("acid_blob"));

    private ModEntities() {}
}

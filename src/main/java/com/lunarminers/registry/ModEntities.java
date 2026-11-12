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
                    .sized(2.2F, 1.4F)
                    .clientTrackingRange(10)
                    .build("rover"));

    private ModEntities() {}
}

package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.AlienEntity;
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

    private ModEntities() {}
}

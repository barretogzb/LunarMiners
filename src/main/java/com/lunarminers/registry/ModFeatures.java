package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.world.CraterFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, LunarMiners.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CRATER =
            FEATURES.register("crater", () -> new CraterFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ABANDONED_SITE =
            FEATURES.register("abandoned_site",
                    () -> new com.lunarminers.world.AbandonedSiteFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}
}

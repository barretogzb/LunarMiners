package com.lunarminers;

import com.lunarminers.registry.ModBlocks;
import com.lunarminers.registry.ModCreativeTabs;
import com.lunarminers.registry.ModEntities;
import com.lunarminers.registry.ModFeatures;
import com.lunarminers.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LunarMiners.MOD_ID)
public class LunarMiners {
    public static final String MOD_ID = "lunarminers";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LunarMiners() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }
}

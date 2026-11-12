package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LunarMiners.MOD_ID);

    // Nióbio: o Brasil tem a maior reserva do mundo, então é o primeiro metal da base.
    public static final RegistryObject<Item> RAW_NIOBIUM =
            ITEMS.register("raw_niobium", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> NIOBIUM_INGOT =
            ITEMS.register("niobium_ingot", () -> new Item(new Item.Properties()));

    private ModItems() {}
}

package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.item.FlagKitItem;
import com.lunarminers.item.ModArmorMaterials;
import com.lunarminers.item.ModTiers;
import com.lunarminers.item.ModuleItem;
import com.lunarminers.item.RoverKitItem;
import com.lunarminers.item.SuitArmorItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BedItem;
import com.lunarminers.world.Bandeiras;
import java.util.function.Supplier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, LunarMiners.MOD_ID);

    // ---------- Materiais ----------
    public static final RegistryObject<Item> RAW_NIOBIUM = simples("raw_niobium");
    public static final RegistryObject<Item> NIOBIUM_INGOT = simples("niobium_ingot");
    public static final RegistryObject<Item> RAW_TITANIUM = simples("raw_titanium");
    public static final RegistryObject<Item> TITANIUM_INGOT = simples("titanium_ingot");
    public static final RegistryObject<Item> RAW_ALUMINUM = simples("raw_aluminum");
    public static final RegistryObject<Item> ALUMINUM_INGOT = simples("aluminum_ingot");
    public static final RegistryObject<Item> ALUMINUM_ROD = simples("aluminum_rod");
    public static final RegistryObject<Item> SILICON = simples("silicon");
    public static final RegistryObject<Item> HELIUM3_CRYSTAL = simples("helium3_crystal");
    public static final RegistryObject<Item> ICE_SHARD = simples("ice_shard");
    public static final RegistryObject<Item> ALIEN_GOO = simples("alien_goo");

    // ---------- Ferramentas ----------
    public static final RegistryObject<Item> ALUMINUM_PICKAXE = picareta("aluminum", ModTiers.ALUMINUM);
    public static final RegistryObject<Item> ALUMINUM_AXE = machado("aluminum", ModTiers.ALUMINUM);
    public static final RegistryObject<Item> ALUMINUM_SHOVEL = pa("aluminum", ModTiers.ALUMINUM);
    public static final RegistryObject<Item> ALUMINUM_SWORD = espada("aluminum", ModTiers.ALUMINUM);
    public static final RegistryObject<Item> ALUMINUM_HOE = enxada("aluminum", ModTiers.ALUMINUM);
    public static final RegistryObject<Item> NIOBIUM_PICKAXE = picareta("niobium", ModTiers.NIOBIUM);
    public static final RegistryObject<Item> NIOBIUM_AXE = machado("niobium", ModTiers.NIOBIUM);
    public static final RegistryObject<Item> NIOBIUM_SHOVEL = pa("niobium", ModTiers.NIOBIUM);
    public static final RegistryObject<Item> NIOBIUM_SWORD = espada("niobium", ModTiers.NIOBIUM);
    public static final RegistryObject<Item> NIOBIUM_HOE = enxada("niobium", ModTiers.NIOBIUM);
    public static final RegistryObject<Item> TITANIUM_PICKAXE = picareta("titanium", ModTiers.TITANIUM);
    public static final RegistryObject<Item> TITANIUM_AXE = machado("titanium", ModTiers.TITANIUM);
    public static final RegistryObject<Item> TITANIUM_SHOVEL = pa("titanium", ModTiers.TITANIUM);
    public static final RegistryObject<Item> TITANIUM_SWORD = espada("titanium", ModTiers.TITANIUM);
    public static final RegistryObject<Item> TITANIUM_HOE = enxada("titanium", ModTiers.TITANIUM);

    // ---------- Trajes (oxigênio em segundos: capacete e peitoral) ----------
    public static final RegistryObject<Item> SPACE_SUIT_HELMET = traje("space_suit_helmet", ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.HELMET, 60);
    public static final RegistryObject<Item> SPACE_SUIT_CHESTPLATE = traje("space_suit_chestplate", ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.CHESTPLATE, 240);
    public static final RegistryObject<Item> SPACE_SUIT_LEGGINGS = traje("space_suit_leggings", ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.LEGGINGS, 0);
    public static final RegistryObject<Item> SPACE_SUIT_BOOTS = traje("space_suit_boots", ModArmorMaterials.SPACE_SUIT, ArmorItem.Type.BOOTS, 0);
    public static final RegistryObject<Item> TITANIUM_SUIT_HELMET = traje("titanium_suit_helmet", ModArmorMaterials.TITANIUM_SUIT, ArmorItem.Type.HELMET, 120);
    public static final RegistryObject<Item> TITANIUM_SUIT_CHESTPLATE = traje("titanium_suit_chestplate", ModArmorMaterials.TITANIUM_SUIT, ArmorItem.Type.CHESTPLATE, 480);
    public static final RegistryObject<Item> TITANIUM_SUIT_LEGGINGS = traje("titanium_suit_leggings", ModArmorMaterials.TITANIUM_SUIT, ArmorItem.Type.LEGGINGS, 0);
    public static final RegistryObject<Item> TITANIUM_SUIT_BOOTS = traje("titanium_suit_boots", ModArmorMaterials.TITANIUM_SUIT, ArmorItem.Type.BOOTS, 0);

    // ---------- Módulos do traje ----------
    public static final RegistryObject<Item> O2_TANK_MODULE = modulo(ModuleItem.Tipo.TANQUE);
    public static final RegistryObject<Item> NIGHT_VISION_MODULE = modulo(ModuleItem.Tipo.VISOR);
    public static final RegistryObject<Item> SERVO_MODULE = modulo(ModuleItem.Tipo.SERVO);
    public static final RegistryObject<Item> THRUSTER_MODULE = modulo(ModuleItem.Tipo.PROPULSOR);
    public static final RegistryObject<Item> JETPACK_MODULE = modulo(ModuleItem.Tipo.JETPACK);

    // ---------- Equipamentos ----------
    public static final RegistryObject<Item> ORE_SCANNER = ITEMS.register("ore_scanner",
            () -> new com.lunarminers.item.ScannerItem(new Item.Properties().durability(64)));
    public static final RegistryObject<Item> METEORITE_FRAGMENT = simples("meteorite_fragment");
    public static final RegistryObject<Item> QUEEN_CORE = ITEMS.register("queen_core",
            () -> new Item(new Item.Properties().stacksTo(1).fireResistant().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> ALIEN_SPITTER_SPAWN_EGG = ITEMS.register("alien_spitter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ALIEN_SPITTER, 0xC7D25A, 0x5A2A6E, new Item.Properties()));
    public static final RegistryObject<Item> ALIEN_QUEEN_SPAWN_EGG = ITEMS.register("alien_queen_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ALIEN_QUEEN, 0x3A1A4E, 0xB040E0, new Item.Properties()));

    // ---------- Veículos, comida e cama ----------
    public static final RegistryObject<Item> ROVER_KIT = ITEMS.register("rover_kit",
            () -> new RoverKitItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPACE_RATION = ITEMS.register("space_ration",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationMod(0.9F).build())));
    public static final RegistryObject<Item> SLEEPING_POD = ITEMS.register("sleeping_pod",
            () -> new BedItem(ModBlocks.SLEEPING_POD.get(), new Item.Properties().stacksTo(1)));

    // ---------- Outros ----------
    public static final RegistryObject<Item> ALIEN_SPAWN_EGG = ITEMS.register("alien_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ALIEN, 0x9BD77A, 0x14171F, new Item.Properties()));

    static {
        for (Bandeiras b : Bandeiras.values()) {
            ITEMS.register("flag_kit_" + b.id(), () -> new FlagKitItem(b, new Item.Properties().stacksTo(16)));
        }
    }

    private static RegistryObject<Item> simples(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> picareta(String m, Tier t) {
        return ITEMS.register(m + "_pickaxe", () -> new PickaxeItem(t, 1, -2.8F, new Item.Properties()));
    }

    private static RegistryObject<Item> machado(String m, Tier t) {
        return ITEMS.register(m + "_axe", () -> new AxeItem(t, 5.5F, -3.0F, new Item.Properties()));
    }

    private static RegistryObject<Item> pa(String m, Tier t) {
        return ITEMS.register(m + "_shovel", () -> new ShovelItem(t, 1.5F, -3.0F, new Item.Properties()));
    }

    private static RegistryObject<Item> espada(String m, Tier t) {
        return ITEMS.register(m + "_sword", () -> new SwordItem(t, 3, -2.4F, new Item.Properties()));
    }

    private static RegistryObject<Item> enxada(String m, Tier t) {
        return ITEMS.register(m + "_hoe", () -> new HoeItem(t, -2, -1.0F, new Item.Properties()));
    }

    private static RegistryObject<Item> traje(String name, ModArmorMaterials mat, ArmorItem.Type type, int o2) {
        Supplier<Item> s = () -> new SuitArmorItem(mat, type, o2, new Item.Properties());
        return ITEMS.register(name, s);
    }

    private static RegistryObject<Item> modulo(ModuleItem.Tipo tipo) {
        return ITEMS.register(tipo.id + "_module", () -> new ModuleItem(tipo, new Item.Properties().stacksTo(8)));
    }

    private ModItems() {}
}

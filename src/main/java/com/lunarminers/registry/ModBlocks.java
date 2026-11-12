package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import com.lunarminers.block.AssemblyBenchBlock;
import com.lunarminers.block.FlagClothBlock;
import com.lunarminers.block.AirlockDoorBlock;
import com.lunarminers.block.CableBlock;
import com.lunarminers.block.Machines;
import com.lunarminers.block.SleepingPodBlock;
import com.lunarminers.block.ShapedBlock;
import com.lunarminers.block.SolarFurnaceBlock;
import com.lunarminers.block.SupplyCrateBlock;
import com.lunarminers.world.Bandeiras;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, LunarMiners.MOD_ID);

    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties moveis() {
        return BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(1.5F, 6.0F).noOcclusion();
    }

    // ---------- Solo e rochas da Lua ----------
    public static final RegistryObject<Block> MOON_REGOLITH = register("moon_regolith",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).strength(0.6F)));
    public static final RegistryObject<Block> MOON_MARE_REGOLITH = register("moon_mare_regolith",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).strength(0.6F)));
    public static final RegistryObject<Block> MOON_ROCK = register("moon_rock",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MOON_COBBLESTONE = register("moon_cobblestone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MOON_DEEP_ROCK = register("moon_deep_rock",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> LUNAR_ICE = register("lunar_ice",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.PACKED_ICE).strength(1.2F)));

    // ---------- Minérios ----------
    public static final RegistryObject<Block> MOON_NIOBIUM_ORE = ore("moon_niobium_ore", 3.5F, 1, 3);
    public static final RegistryObject<Block> MOON_IRON_ORE = ore("moon_iron_ore", 3.0F, 0, 0);
    public static final RegistryObject<Block> MOON_TITANIUM_ORE = ore("moon_titanium_ore", 4.5F, 1, 3);
    public static final RegistryObject<Block> MOON_ALUMINUM_ORE = ore("moon_aluminum_ore", 3.0F, 0, 1);
    public static final RegistryObject<Block> MOON_SILICON_ORE = ore("moon_silicon_ore", 3.0F, 1, 3);
    public static final RegistryObject<Block> MOON_HELIUM3_ORE = register("moon_helium3_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_DIAMOND_ORE)
                    .requiresCorrectToolForDrops().lightLevel(s -> 6), UniformInt.of(3, 7)));
    public static final RegistryObject<Block> NIOBIUM_ORE = ore("niobium_ore", 3.5F, 1, 3);
    public static final RegistryObject<Block> DEEPSLATE_NIOBIUM_ORE = ore("deepslate_niobium_ore", 5.0F, 1, 3);

    // ---------- Blocos de metal ----------
    public static final RegistryObject<Block> NIOBIUM_BLOCK = register("niobium_block", () -> new Block(metal()));
    public static final RegistryObject<Block> TITANIUM_BLOCK = register("titanium_block", () -> new Block(metal().strength(7.0F, 9.0F)));
    public static final RegistryObject<Block> ALUMINUM_BLOCK = register("aluminum_block", () -> new Block(metal()));

    // ---------- Construção do habitat ----------
    public static final RegistryObject<Block> HABITAT_WALL = register("habitat_wall", () -> new Block(metal()));
    public static final RegistryObject<Block> HABITAT_FLOOR = register("habitat_floor", () -> new Block(metal()));
    public static final RegistryObject<Block> HABITAT_GLASS = register("habitat_glass",
            () -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).strength(1.0F, 6.0F)));
    public static final RegistryObject<Block> LANDING_PAD = register("landing_pad", () -> new Block(metal()));

    // ---------- Máquinas ----------
    public static final RegistryObject<Block> ASSEMBLY_BENCH = register("assembly_bench",
            () -> new AssemblyBenchBlock(moveis(), Shapes.or(Block.box(2, 0, 2, 14, 12, 14), Block.box(0, 12, 0, 16, 15, 16))));
    public static final RegistryObject<Block> SOLAR_FURNACE = register("solar_furnace",
            () -> new SolarFurnaceBlock(moveis().lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0)));
    public static final RegistryObject<Block> SUPPLY_CRATE = register("supply_crate",
            () -> new SupplyCrateBlock(moveis()));
    public static final RegistryObject<Block> OXYGEN_DISTRIBUTOR = register("oxygen_distributor",
            () -> new Machines.Distributor(moveis().lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 10 : 2),
                    Block.box(3, 0, 3, 13, 13, 13)));
    public static final RegistryObject<Block> OXYGEN_TANK = register("oxygen_tank",
            () -> new ShapedBlock(metal().noOcclusion(), Block.box(2, 0, 2, 14, 16, 14)));
    public static final RegistryObject<Block> OXYGEN_TANK_CAP = register("oxygen_tank_cap",
            () -> new Machines.TankValve(metal().noOcclusion(), Block.box(2, 0, 2, 14, 13, 14)));
    public static final RegistryObject<Block> SOLAR_PANEL = register("solar_panel",
            () -> new Machines.SolarPanel(moveis(), Block.box(0, 0, 0, 16, 14, 16)));
    public static final RegistryObject<Block> SOLAR_ARRAY = register("solar_array",
            () -> new Machines.SolarPanel(metal().noOcclusion(), Block.box(5, 0, 5, 11, 16, 11)));
    public static final RegistryObject<Block> COMMS_TOWER = register("comms_tower",
            () -> new Machines.CommsTower(metal().noOcclusion().lightLevel(s -> 4), Block.box(4, 0, 4, 12, 16, 12)));
    public static final RegistryObject<Block> METEORITE_ORE = register("meteorite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.ANCIENT_DEBRIS).requiresCorrectToolForDrops()
                    .lightLevel(s -> 5), UniformInt.of(4, 9)));
    public static final RegistryObject<Block> ALIEN_RESIN = register("alien_resin",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD_BRICKS).strength(2.0F, 4.0F).sound(SoundType.SLIME_BLOCK)));
    public static final RegistryObject<Block> ALIEN_RESIN_GLOW = register("alien_resin_glow",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD_BRICKS).strength(2.0F, 4.0F).sound(SoundType.SLIME_BLOCK)
                    .lightLevel(s -> 9)));
    public static final RegistryObject<Block> ALIEN_EGG = register("alien_egg",
            () -> new com.lunarminers.block.AlienEggBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).strength(0.8F)
                    .noOcclusion().lightLevel(s -> 4), Block.box(3, 0, 3, 13, 14, 13)));
    public static final RegistryObject<Block> CABLE = register("cable",
            () -> new CableBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).strength(0.6F).noOcclusion()));
    public static final RegistryObject<Block> BATTERY = register("battery",
            () -> new Machines.Battery(metal().noOcclusion(), Block.box(1, 0, 1, 15, 15, 15)));
    public static final RegistryObject<Block> HE3_GENERATOR = register("he3_generator",
            () -> new Machines.He3Generator(metal().noOcclusion().lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 12 : 0),
                    Block.box(1, 0, 1, 15, 15, 15)));
    public static final RegistryObject<Block> ELECTROLYZER = register("electrolyzer",
            () -> new Machines.Electrolyzer(metal().noOcclusion().lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 8 : 0),
                    Block.box(2, 0, 2, 14, 15, 14)));
    public static final RegistryObject<Block> AIRLOCK_DOOR = register("airlock_door",
            () -> new AirlockDoorBlock(metal().strength(5.0F, 12.0F).noOcclusion()));
    public static final RegistryObject<Block> SLEEPING_POD = registerNoItem("sleeping_pod",
            () -> new SleepingPodBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_BED).strength(1.0F).noOcclusion()));
    public static final RegistryObject<Block> LIGHT_STICK = register("light_stick",
            () -> new ShapedBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(s -> 14).noCollission(),
                    Block.box(6.5, 0, 6.5, 9.5, 11, 9.5)));

    // ---------- Móveis ----------
    private static final VoxelShape MESA = Shapes.or(Block.box(6, 0, 6, 10, 12, 10), Block.box(1, 12, 1, 15, 14, 15));
    public static final RegistryObject<Block> ROUND_TABLE = register("round_table", () -> new ShapedBlock(moveis(), MESA));
    public static final RegistryObject<Block> STOOL = register("stool",
            () -> new ShapedBlock(moveis(), Shapes.or(Block.box(6, 0, 6, 10, 7, 10), Block.box(3, 7, 3, 13, 9, 13))));
    public static final RegistryObject<Block> CEILING_LAMP = register("ceiling_lamp",
            () -> new ShapedBlock(moveis().lightLevel(s -> 15), Block.box(3, 10, 3, 13, 16, 13)));
    public static final RegistryObject<Block> FLOOR_LAMP = register("floor_lamp",
            () -> new ShapedBlock(moveis().lightLevel(s -> 15), Shapes.or(Block.box(5, 0, 5, 11, 12, 11), Block.box(4, 11, 4, 12, 16, 12))));

    // ---------- Bandeiras ----------
    public static final RegistryObject<Block> FLAG_POLE = register("flag_pole",
            () -> new ShapedBlock(moveis(), Block.box(7, 0, 7, 9, 16, 9)));
    public static final RegistryObject<Block> FLAG_POLE_TOP = registerNoItem("flag_pole_top",
            () -> new ShapedBlock(moveis(), Block.box(6, 0, 6, 10, 5, 10)));
    public static final Map<Bandeiras, RegistryObject<Block>> FLAGS = new EnumMap<>(Bandeiras.class);

    static {
        for (Bandeiras b : Bandeiras.values()) {
            FLAGS.put(b, registerNoItem("flag_" + b.id(), () -> new FlagClothBlock(
                    BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(0.5F).noCollission().noOcclusion()
                            .sound(SoundType.WOOL))));
        }
    }

    private static RegistryObject<Block> ore(String name, float strength, int xpMin, int xpMax) {
        return register(name, () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)
                .strength(strength, 3.0F).requiresCorrectToolForDrops(), UniformInt.of(xpMin, xpMax)));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private ModBlocks() {}
}

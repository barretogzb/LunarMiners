package com.lunarminers.registry;

import com.lunarminers.LunarMiners;
import java.util.function.Supplier;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, LunarMiners.MOD_ID);

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

    // ---------- Minérios ----------
    public static final RegistryObject<Block> MOON_NIOBIUM_ORE = register("moon_niobium_ore",
            () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_ORE).strength(3.5F, 3.0F).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 3)));

    public static final RegistryObject<Block> MOON_IRON_ORE = register("moon_iron_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).requiresCorrectToolForDrops()));

    // Minérios de nióbio da Terra (para quando o jogador voltar ao planeta)
    public static final RegistryObject<Block> NIOBIUM_ORE = register("niobium_ore",
            () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_ORE).strength(3.5F, 3.0F).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 3)));

    public static final RegistryObject<Block> DEEPSLATE_NIOBIUM_ORE = register("deepslate_niobium_ore",
            () -> new DropExperienceBlock(
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE).strength(5.0F, 3.0F).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 3)));

    // ---------- Construção ----------
    public static final RegistryObject<Block> NIOBIUM_BLOCK = register("niobium_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops()));

    // Tanque de oxigênio: corpo cilíndrico (empilhável) e tampa com válvula
    public static final RegistryObject<Block> OXYGEN_TANK = register("oxygen_tank",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> OXYGEN_TANK_CAP = register("oxygen_tank_cap",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()
                    .requiresCorrectToolForDrops()));

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }

    private ModBlocks() {}
}

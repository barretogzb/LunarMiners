package com.lunarminers.world;

import com.lunarminers.LunarMiners;
import com.lunarminers.blockentity.SupplyCrateBlockEntity;
import com.lunarminers.registry.ModBlocks;
import com.lunarminers.registry.ModItems;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Complexo Lunar Cruzeiro do Sul: cúpula central com três cúpulas ligadas por túneis
 * (estufa, oficina e dormitório), eclusa, plataforma de pouso, fazenda de tanques,
 * campo de painéis solares e a bandeira do Brasil num mastro.
 */
public final class LunarBase {
    private static final int R_CENTRAL = 13;
    private static final int R_SATELITE = 8;
    private static final int DIST_SATELITE = 27;

    public static void ensureBuilt(ServerLevel level) {
        LunarBaseData data = LunarBaseData.get(level);
        if (data.built) {
            return;
        }
        BlockPos spawn = level.getSharedSpawnPos();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        BlockPos c = new BlockPos(spawn.getX(), y, spawn.getZ());

        construir(level, c);

        level.setDefaultSpawnPos(c, 0.0F);
        level.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, level.getServer());
        data.built = true;
        data.center = c;
        data.setDirty();
        LunarMiners.LOGGER.info("[LunarMiners] Base Cruzeiro do Sul construida em {}", c);
    }

    public static void welcome(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        ensureBuilt(overworld);
        LunarBaseData data = LunarBaseData.get(overworld);
        if (data.welcomed.add(player.getUUID())) {
            data.setDirty();
            BlockPos c = data.center;
            player.teleportTo(overworld, c.getX() + 0.5D, c.getY(), c.getZ() + 2.5D, 180.0F, 0.0F);
            OxygenSystem.encher(player);
            player.sendSystemMessage(Component.translatable("message.lunarminers.welcome"));
        }
    }

    private static void construir(ServerLevel level, BlockPos c) {
        Construtor b = new Construtor(level, p -> true);
        Random rnd = new Random(level.getSeed());
        BlockPos leste = c.offset(DIST_SATELITE, 0, 0);
        BlockPos oeste = c.offset(-DIST_SATELITE, 0, 0);
        BlockPos norte = c.offset(0, 0, -DIST_SATELITE);

        // 1) praça aplainada para todo o complexo
        b.aplainar(c, 44, 18);

        // 2) cúpulas
        b.cupula(c, R_CENTRAL, 0, rnd);
        b.cupula(leste, R_SATELITE, 0, rnd);
        b.cupula(oeste, R_SATELITE, 0, rnd);
        b.cupula(norte, R_SATELITE, 0, rnd);

        java.util.function.Predicate<BlockPos> dentro = p ->
                dentroDe(p, c, R_CENTRAL) || dentroDe(p, leste, R_SATELITE)
                        || dentroDe(p, oeste, R_SATELITE) || dentroDe(p, norte, R_SATELITE);

        // 3) túneis
        b.tunel(c.offset(R_CENTRAL - 2, 0, 0), Direction.EAST, DIST_SATELITE - R_CENTRAL - R_SATELITE + 4, dentro);
        b.tunel(c.offset(-R_CENTRAL + 2, 0, 0), Direction.WEST, DIST_SATELITE - R_CENTRAL - R_SATELITE + 4, dentro);
        b.tunel(c.offset(0, 0, -R_CENTRAL + 2), Direction.NORTH, DIST_SATELITE - R_CENTRAL - R_SATELITE + 4, dentro);
        b.tunel(c.offset(0, 0, R_CENTRAL - 2), Direction.SOUTH, 9, dentro); // eclusa

        // 4) eclusa: duas portas
        BlockState parede = ModBlocks.HABITAT_WALL.get().defaultBlockState();
        for (int dz : new int[]{R_CENTRAL + 1, R_CENTRAL + 7}) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int h = 0; h <= 4; h++) {
                    if (Math.sqrt(dx * dx + (h - 1.5) * (h - 1.5)) < 2.1) {
                        b.set(c.offset(dx, h, dz), parede);
                    }
                }
            }
            porta(b, c.offset(0, 0, dz));
        }
        b.set(c.offset(0, 3, R_CENTRAL + 4), ModBlocks.CEILING_LAMP.get().defaultBlockState());

        mobiliarCentral(b, c);
        mobiliarEstufa(b, leste);
        mobiliarOficina(b, oeste);
        mobiliarDormitorio(b, norte);

        // 5) área externa: plataforma de pouso, tanques, painéis solares e bandeira
        BlockPos pouso = c.offset(0, 0, R_CENTRAL + 22);
        b.pisoCircular(pouso, 7, ModBlocks.HABITAT_FLOOR.get().defaultBlockState());
        b.pisoCircular(pouso, 4.5, ModBlocks.LANDING_PAD.get().defaultBlockState());
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8;
            b.set(pouso.offset((int) Math.round(Math.cos(a) * 7), 0, (int) Math.round(Math.sin(a) * 7)),
                    ModBlocks.FLOOR_LAMP.get().defaultBlockState());
        }

        BlockPos tanques = c.offset(19, 0, -19);
        b.pisoCircular(tanques, 5, ModBlocks.HABITAT_FLOOR.get().defaultBlockState());
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            b.tanque(tanques.offset((int) Math.round(Math.cos(a) * 3), 0, (int) Math.round(Math.sin(a) * 3)), 3);
        }
        b.tanque(tanques, 4);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                b.set(c.offset(16 + i * 2, 0, 15 + j * 2), ModBlocks.SOLAR_PANEL.get().defaultBlockState());
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                b.set(c.offset(-22 + i * 2, 0, 15 + j * 2), ModBlocks.SOLAR_PANEL.get().defaultBlockState());
            }
        }

        Bandeiras.BRASIL.fincar(b.colocador(), c.offset(-5, 0, R_CENTRAL + 12), Direction.SOUTH, 8);
    }

    private static boolean dentroDe(BlockPos p, BlockPos centro, int raio) {
        int dx = p.getX() - centro.getX();
        int dy = p.getY() - centro.getY();
        int dz = p.getZ() - centro.getZ();
        return dy >= 0 && Math.sqrt(dx * dx + dy * dy + dz * dz) < raio - 0.5;
    }

    private static void porta(Construtor b, BlockPos p) {
        BlockState baixo = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.set(p, baixo);
        b.set(p.above(), baixo.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void distribuidor(Construtor b, BlockPos p) {
        b.set(p, ModBlocks.OXYGEN_DISTRIBUTOR.get().defaultBlockState());
        if (b.level instanceof ServerLevel server) {
            OxygenData.get(server).adicionar(p);
        }
        b.tanque(p.east(), 2);
        b.tanque(p.west(), 2);
    }

    private static void conteiner(Construtor b, BlockPos p, List<ItemStack> itens) {
        b.set(p, ModBlocks.SUPPLY_CRATE.get().defaultBlockState());
        if (b.level.getBlockEntity(p) instanceof SupplyCrateBlockEntity crate) {
            int slot = 0;
            for (ItemStack stack : itens) {
                crate.setItem(slot++, stack);
            }
            crate.setChanged();
        }
    }

    private static void mesaComBancos(Construtor b, BlockPos p) {
        b.set(p, ModBlocks.ROUND_TABLE.get().defaultBlockState());
        for (Direction d : Direction.Plane.HORIZONTAL) {
            b.set(p.relative(d), ModBlocks.STOOL.get().defaultBlockState());
        }
    }

    private static void mobiliarCentral(Construtor b, BlockPos c) {
        distribuidor(b, c.offset(0, 0, -6));
        b.set(c.offset(0, R_CENTRAL - 1, 0), ModBlocks.CEILING_LAMP.get().defaultBlockState());
        mesaComBancos(b, c.offset(-6, 0, 2));
        mesaComBancos(b, c.offset(6, 0, 2));
        conteiner(b, c.offset(-3, 0, -9), kitInicial());
        conteiner(b, c.offset(3, 0, -9), List.of(new ItemStack(Items.BREAD, 32), new ItemStack(Items.BAKED_POTATO, 16),
                new ItemStack(ModItems.ALUMINUM_INGOT.get(), 12), new ItemStack(ModItems.SILICON.get(), 6)));
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8 + Math.PI / 8;
            b.set(c.offset((int) Math.round(Math.cos(a) * 10), 0, (int) Math.round(Math.sin(a) * 10)),
                    ModBlocks.FLOOR_LAMP.get().defaultBlockState());
        }
        Bandeiras.BRASIL.fincar(b.colocador(), c.offset(-2, 0, -10), Direction.SOUTH, 4);
    }

    private static void mobiliarEstufa(Construtor b, BlockPos e) {
        distribuidor(b, e);
        b.set(e.offset(0, R_SATELITE - 1, 0), ModBlocks.CEILING_LAMP.get().defaultBlockState());
        Random rnd = new Random(e.asLong());
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < 2.5 || dist > 6.2) {
                    continue;
                }
                BlockPos chao = e.offset(dx, -1, dz);
                if (Math.abs(dist - 4.3) < 0.5) {
                    b.set(chao, Blocks.WATER.defaultBlockState());
                } else {
                    b.set(chao, Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
                    BlockState planta = (rnd.nextInt(3) == 0 ? Blocks.CARROTS : Blocks.WHEAT).defaultBlockState();
                    b.set(chao.above(), planta.setValue(CropBlock.AGE, rnd.nextInt(8)));
                }
            }
        }
    }

    private static void mobiliarOficina(Construtor b, BlockPos o) {
        distribuidor(b, o);
        b.set(o.offset(0, R_SATELITE - 1, 0), ModBlocks.CEILING_LAMP.get().defaultBlockState());
        b.set(o.offset(-4, 0, -3), ModBlocks.ASSEMBLY_BENCH.get().defaultBlockState());
        b.set(o.offset(-4, 0, 3), ModBlocks.ASSEMBLY_BENCH.get().defaultBlockState());
        for (int i = -1; i <= 1; i++) {
            b.set(o.offset(-5, 0, i), ModBlocks.SOLAR_FURNACE.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        }
        conteiner(b, o.offset(2, 0, -5), List.of(new ItemStack(ModItems.ALUMINUM_INGOT.get(), 24),
                new ItemStack(ModItems.ALUMINUM_ROD.get(), 12), new ItemStack(Items.COAL, 16)));
        conteiner(b, o.offset(2, 0, 5), List.of());
        conteiner(b, o.offset(4, 0, -3), List.of());
    }

    private static void mobiliarDormitorio(Construtor b, BlockPos n) {
        distribuidor(b, n);
        b.set(n.offset(0, R_SATELITE - 1, 0), ModBlocks.CEILING_LAMP.get().defaultBlockState());
        BlockState cama = Blocks.GREEN_BED.defaultBlockState();
        int[][] camas = {{-4, -3, 0}, {-2, -5, 0}, {2, -5, 0}, {4, -3, 0}};
        for (int[] k : camas) {
            BlockState s = cama.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
            b.set(n.offset(k[0], 0, k[1] + 1), s.setValue(BlockStateProperties.BED_PART, BedPart.FOOT));
            b.set(n.offset(k[0], 0, k[1]), s.setValue(BlockStateProperties.BED_PART, BedPart.HEAD));
        }
        mesaComBancos(b, n.offset(0, 0, 3));
        b.set(n.offset(-5, 0, 2), ModBlocks.FLOOR_LAMP.get().defaultBlockState());
        b.set(n.offset(5, 0, 2), ModBlocks.FLOOR_LAMP.get().defaultBlockState());
        conteiner(b, n.offset(-4, 0, 4), List.of(new ItemStack(Items.COOKED_BEEF, 8), new ItemStack(Items.APPLE, 8)));
    }

    private static List<ItemStack> kitInicial() {
        return List.of(
                new ItemStack(ModItems.SPACE_SUIT_HELMET.get()),
                new ItemStack(ModItems.SPACE_SUIT_CHESTPLATE.get()),
                new ItemStack(ModItems.SPACE_SUIT_LEGGINGS.get()),
                new ItemStack(ModItems.SPACE_SUIT_BOOTS.get()),
                new ItemStack(ModItems.ALUMINUM_PICKAXE.get()),
                new ItemStack(ModItems.ALUMINUM_SHOVEL.get()),
                new ItemStack(ModItems.ALUMINUM_SWORD.get()),
                new ItemStack(ModItems.ALUMINUM_AXE.get()),
                new ItemStack(Items.BREAD, 16),
                new ItemStack(Items.TORCH, 32),
                new ItemStack(Items.WHEAT_SEEDS, 16),
                new ItemStack(Items.BONE_MEAL, 16),
                new ItemStack(Items.OAK_SAPLING, 4),
                new ItemStack(Items.DIRT, 32),
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(ModBlocks.OXYGEN_DISTRIBUTOR.get(), 1),
                new ItemStack(ModBlocks.OXYGEN_TANK.get(), 2),
                new ItemStack(ModBlocks.OXYGEN_TANK_CAP.get(), 1),
                new ItemStack(ModBlocks.HABITAT_GLASS.get(), 32),
                new ItemStack(ModBlocks.HABITAT_WALL.get(), 32),
                new ItemStack(ModItems.ALUMINUM_INGOT.get(), 8));
    }

    private LunarBase() {}
}

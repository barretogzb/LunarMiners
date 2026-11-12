package com.lunarminers.world;

import com.lunarminers.LunarMiners;
import com.lunarminers.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;

/** Constrói a Base Lunar Cruzeiro do Sul no ponto de nascimento do mundo. */
public final class LunarBase {
    private static final int R = 5; // "raio" da base: 11x11 blocos
    private static final int FLAGS = 2;

    public static void ensureBuilt(ServerLevel level) {
        LunarBaseData data = LunarBaseData.get(level);
        if (data.built) {
            return;
        }
        BlockPos spawn = level.getSharedSpawnPos();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        BlockPos center = new BlockPos(spawn.getX(), y, spawn.getZ());

        build(level, center);

        level.setDefaultSpawnPos(center, 0.0F);
        level.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, level.getServer());
        data.built = true;
        data.center = center;
        data.setDirty();
        LunarMiners.LOGGER.info("[LunarMiners] Base Cruzeiro do Sul construida em {}", center);
    }

    public static void welcome(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        ensureBuilt(overworld);
        LunarBaseData data = LunarBaseData.get(overworld);
        if (data.welcomed.add(player.getUUID())) {
            data.setDirty();
            BlockPos c = data.center;
            player.teleportTo(overworld, c.getX() + 0.5D, c.getY(), c.getZ() + 0.5D, 180.0F, 0.0F);
            player.sendSystemMessage(Component.literal(
                    "§bBem-vindo à Base Lunar Cruzeiro do Sul! §7Seu kit de sobrevivência está no baú. "
                            + "Plante na estufa, explore as crateras e cuidado com os alienígenas à noite."));
        }
    }

    private static void build(ServerLevel level, BlockPos c) {
        BlockState hull = ModBlocks.NIOBIUM_BLOCK.get().defaultBlockState();
        BlockState rock = ModBlocks.MOON_ROCK.get().defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState light = Blocks.SEA_LANTERN.defaultBlockState();
        int y = c.getY();
        double r = R + 2.0; // raio da cúpula = 7

        // 1) terreno: limpa tudo e faz a fundação circular (com folga para tanques e entrada)
        for (int dx = -12; dx <= 12; dx++) {
            for (int dz = -10; dz <= 12; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                boolean area = dist <= r + 0.5 || (dx >= 8 && dx <= 12 && Math.abs(dz) <= 4)
                        || (Math.abs(dx) <= 3 && dz > 0 && dz <= 11);
                if (!area) {
                    continue;
                }
                for (int h = 0; h <= 9; h++) {
                    level.setBlock(c.offset(dx, h, dz), air, FLAGS);
                }
                for (int d = 1; d <= 24; d++) {
                    BlockPos p = c.offset(dx, -d, dz);
                    if (d > 1 && !level.getBlockState(p).canBeReplaced()) {
                        break;
                    }
                    level.setBlock(p, d == 1 ? hull : rock, FLAGS);
                }
            }
        }

        // 2) cúpula: casca de vidro com nervuras de nióbio (anel da base, meridianos e um anel no meio)
        for (int dx = -R - 2; dx <= R + 2; dx++) {
            for (int dz = -R - 2; dz <= R + 2; dz++) {
                for (int h = 0; h <= R + 2; h++) {
                    double dist = Math.sqrt(dx * dx + dz * dz + h * h);
                    if (dist < r - 0.5 || dist > r + 0.5) {
                        continue;
                    }
                    boolean nervura = h == 0 || h == 4 || dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz);
                    level.setBlock(c.offset(dx, h, dz), nervura ? hull : glass, FLAGS);
                }
                double piso = Math.sqrt(dx * dx + dz * dz);
                if (Math.abs(piso - 4.0) < 0.5 && (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz))) {
                    level.setBlock(c.offset(dx, -1, dz), light, FLAGS);
                }
            }
        }
        level.setBlock(c.offset(0, (int) r, 0), light, FLAGS);

        // 3) túnel de entrada (eclusa) no lado sul
        for (int dz = 5; dz <= 10; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int h = 0; h <= 3; h++) {
                    boolean parede = Math.abs(dx) == 2 || h == 3;
                    boolean janela = parede && h == 1 && Math.abs(dx) == 2 && (dz == 7 || dz == 8);
                    level.setBlock(c.offset(dx, h, dz), parede ? (janela ? glass : hull) : air, FLAGS);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int h = 0; h <= 2; h++) {
                level.setBlock(c.offset(dx, h, 10), hull, FLAGS);
            }
        }
        level.setBlock(c.offset(0, 2, 9), light, FLAGS);
        BlockState doorLower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        BlockPos door = c.offset(0, 0, 10);
        level.setBlock(door, doorLower, FLAGS);
        level.setBlock(door.above(), doorLower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), FLAGS);
        BlockPos innerDoor = c.offset(0, 0, 6);
        level.setBlock(innerDoor, doorLower, FLAGS);
        level.setBlock(innerDoor.above(), doorLower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), FLAGS);
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int h = 0; h <= 2; h++) {
                level.setBlock(c.offset(dx, h, 6), hull, FLAGS);
            }
        }

        // 4) móveis encostados na parede norte
        level.setBlock(c.offset(-3, 0, -4), Blocks.CRAFTING_TABLE.defaultBlockState(), FLAGS);
        level.setBlock(c.offset(-2, 0, -5), Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), FLAGS);
        level.setBlock(c.offset(-1, 0, -5), Blocks.BLAST_FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), FLAGS);
        BlockPos chestPos = c.offset(2, 0, -5);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), FLAGS);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            int slot = 0;
            for (ItemStack stack : starterKit()) {
                chest.setItem(slot++, stack);
            }
            chest.setChanged();
        }
        Bandeiras.BRASIL.colocar(level, c.offset(0, 0, -5), 0);

        BlockState bed = Blocks.GREEN_BED.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        level.setBlock(c.offset(4, 0, 1), bed.setValue(BlockStateProperties.BED_PART, BedPart.FOOT), FLAGS);
        level.setBlock(c.offset(4, 0, 0), bed.setValue(BlockStateProperties.BED_PART, BedPart.HEAD), FLAGS);

        // 5) estufa: canteiro com água no meio, do lado oeste
        for (int dx = -5; dx <= -3; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                level.setBlock(c.offset(dx, -1, dz), dx == -4 && dz == 1
                        ? Blocks.WATER.defaultBlockState() : Blocks.FARMLAND.defaultBlockState(), FLAGS);
            }
        }

        // 6) tanques de oxigênio do lado leste, numa plataforma
        for (int dz = -2; dz <= 2; dz += 2) {
            BlockPos t = c.offset(10, 0, dz);
            level.setBlock(t, ModBlocks.OXYGEN_TANK.get().defaultBlockState(), FLAGS);
            level.setBlock(t.above(), ModBlocks.OXYGEN_TANK.get().defaultBlockState(), FLAGS);
            level.setBlock(t.above(2), ModBlocks.OXYGEN_TANK_CAP.get().defaultBlockState(), FLAGS);
        }
        for (int dx = 8; dx <= 9; dx++) {
            level.setBlock(c.offset(dx, 1, 0), Blocks.IRON_BARS.defaultBlockState(), FLAGS);
        }

        // 7) bandeiras na entrada
        Bandeiras.BRASIL.colocar(level, c.offset(-3, 0, 11), 8);
        Bandeiras.BRASIL.colocar(level, c.offset(3, 0, 11), 8);
    }

    private static List<ItemStack> starterKit() {
        return List.of(
                new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.IRON_SHOVEL),
                new ItemStack(Items.IRON_SWORD),
                new ItemStack(Items.IRON_HOE),
                new ItemStack(Items.BREAD, 16),
                new ItemStack(Items.TORCH, 32),
                new ItemStack(Items.OAK_LOG, 16),
                new ItemStack(Items.OAK_SAPLING, 4),
                new ItemStack(Items.WHEAT_SEEDS, 12),
                new ItemStack(Items.CARROT, 4),
                new ItemStack(Items.BONE_MEAL, 16),
                new ItemStack(Items.DIRT, 32),
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.GLASS, 32),
                new ItemStack(Items.COAL, 8));
    }

    private LunarBase() {}
}

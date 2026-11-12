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

        for (int dx = -R - 1; dx <= R + 1; dx++) {
            for (int dz = -R - 1; dz <= R + 1; dz++) {
                int x = c.getX() + dx;
                int z = c.getZ() + dz;
                boolean inside = Math.abs(dx) <= R && Math.abs(dz) <= R;
                boolean edge = Math.abs(dx) == R || Math.abs(dz) == R;
                boolean corner = Math.abs(dx) == R && Math.abs(dz) == R;

                // limpa o espaço acima
                for (int h = 0; h <= 6; h++) {
                    level.setBlock(new BlockPos(x, y + h, z), air, FLAGS);
                }
                // fundação até encostar no chão
                for (int d = 1; d <= 24; d++) {
                    BlockPos p = new BlockPos(x, y - d, z);
                    if (d > 1 && !level.getBlockState(p).canBeReplaced()) {
                        break;
                    }
                    level.setBlock(p, d == 1 ? hull : rock, FLAGS);
                }
                if (!inside) {
                    continue;
                }
                if (Math.abs(dx) == 3 && Math.abs(dz) == 3) {
                    level.setBlock(new BlockPos(x, y - 1, z), light, FLAGS);
                }
                if (edge) {
                    for (int h = 0; h <= 3; h++) {
                        boolean window = !corner && (h == 1 || h == 2)
                                && (Math.abs(dx) <= 3 || Math.abs(dz) <= 3);
                        level.setBlock(new BlockPos(x, y + h, z), window ? glass : hull, FLAGS);
                    }
                }
                level.setBlock(new BlockPos(x, y + 4, z), edge ? hull : glass, FLAGS);
            }
        }

        // porta no lado sul
        BlockPos door = c.offset(0, 0, R);
        BlockState doorLower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        level.setBlock(door, doorLower, FLAGS);
        level.setBlock(door.above(), doorLower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), FLAGS);
        level.setBlock(door.offset(0, -1, 1), hull, FLAGS);

        // móveis
        level.setBlock(c.offset(-4, 0, -4), Blocks.CRAFTING_TABLE.defaultBlockState(), FLAGS);
        level.setBlock(c.offset(-3, 0, -4), Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), FLAGS);
        level.setBlock(c.offset(-2, 0, -4), Blocks.BLAST_FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), FLAGS);

        BlockPos chestPos = c.offset(3, 0, -4);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), FLAGS);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            int slot = 0;
            for (ItemStack stack : starterKit()) {
                chest.setItem(slot++, stack);
            }
            chest.setChanged();
        }

        BlockState bed = Blocks.GREEN_BED.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        level.setBlock(c.offset(4, 0, 1), bed.setValue(BlockStateProperties.BED_PART, BedPart.FOOT), FLAGS);
        level.setBlock(c.offset(4, 0, 0), bed.setValue(BlockStateProperties.BED_PART, BedPart.HEAD), FLAGS);

        // estufa: canteiro de terra com água no meio
        for (int dx = -4; dx <= -2; dx++) {
            for (int dz = 1; dz <= 3; dz++) {
                BlockPos p = c.offset(dx, -1, dz);
                level.setBlock(p, dx == -3 && dz == 2 ? Blocks.WATER.defaultBlockState()
                        : Blocks.FARMLAND.defaultBlockState(), FLAGS);
            }
        }

        // bandeiras do Brasil
        placeFlag(level, c.offset(0, 0, -4), 0);
        placeFlag(level, c.offset(-2, 0, R + 1), 8);
        placeFlag(level, c.offset(2, 0, R + 1), 8);
        level.setBlock(c.offset(-2, -1, R + 1), hull, FLAGS);
        level.setBlock(c.offset(2, -1, R + 1), hull, FLAGS);
    }

    private static void placeFlag(ServerLevel level, BlockPos pos, int rotation) {
        BlockState state = Blocks.GREEN_BANNER.defaultBlockState()
                .setValue(BlockStateProperties.ROTATION_16, rotation);
        level.setBlock(pos, state, FLAGS);
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
            CompoundTag tag = banner.saveWithoutMetadata();
            ListTag patterns = new ListTag();
            patterns.add(pattern("mr", DyeColor.YELLOW));
            patterns.add(pattern("mc", DyeColor.BLUE));
            tag.put("Patterns", patterns);
            banner.load(tag);
            banner.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private static CompoundTag pattern(String code, DyeColor color) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Pattern", code);
        tag.putInt("Color", color.getId());
        return tag;
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

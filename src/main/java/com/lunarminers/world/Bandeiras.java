package com.lunarminers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Bandeiras de países feitas com estandartes (aproximadas com os padrões do jogo). */
public enum Bandeiras {
    BRASIL(Blocks.GREEN_BANNER, "mr", DyeColor.YELLOW, "mc", DyeColor.BLUE),
    EUA(Blocks.WHITE_BANNER, "ss", DyeColor.RED, "sqtl", DyeColor.BLUE),
    CHINA(Blocks.RED_BANNER, "sqtl", DyeColor.YELLOW),
    RUSSIA(Blocks.WHITE_BANNER, "hhb", DyeColor.RED, "ms", DyeColor.BLUE),
    INDIA(Blocks.WHITE_BANNER, "ts", DyeColor.ORANGE, "bs", DyeColor.GREEN, "mc", DyeColor.BLUE),
    JAPAO(Blocks.WHITE_BANNER, "mc", DyeColor.RED),
    FRANCA(Blocks.WHITE_BANNER, "ls", DyeColor.BLUE, "rs", DyeColor.RED),
    ITALIA(Blocks.WHITE_BANNER, "ls", DyeColor.GREEN, "rs", DyeColor.RED),
    ALEMANHA(Blocks.YELLOW_BANNER, "ts", DyeColor.BLACK, "ms", DyeColor.RED),
    ARGENTINA(Blocks.WHITE_BANNER, "ts", DyeColor.LIGHT_BLUE, "bs", DyeColor.LIGHT_BLUE, "mc", DyeColor.YELLOW);

    private final Block banner;
    private final Object[] padroes;

    Bandeiras(Block banner, Object... padroes) {
        this.banner = banner;
        this.padroes = padroes;
    }

    public static Bandeiras aleatoria(RandomSource random) {
        Bandeiras[] todas = values();
        return todas[random.nextInt(todas.length)];
    }

    public void colocar(LevelAccessor level, BlockPos pos, int rotacao) {
        BlockState state = banner.defaultBlockState().setValue(BlockStateProperties.ROTATION_16, rotacao & 15);
        level.setBlock(pos, state, 2);
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity be) {
            CompoundTag tag = be.saveWithoutMetadata();
            ListTag lista = new ListTag();
            for (int i = 0; i + 1 < padroes.length; i += 2) {
                CompoundTag p = new CompoundTag();
                p.putString("Pattern", (String) padroes[i]);
                p.putInt("Color", ((DyeColor) padroes[i + 1]).getId());
                lista.add(p);
            }
            tag.put("Patterns", lista);
            be.load(tag);
            be.setChanged();
        }
    }
}

package com.lunarminers.world;

import com.lunarminers.block.FlagClothBlock;
import com.lunarminers.registry.ModBlocks;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bandeiras reais de países: um mastro de metal com o tecido 3x2 blocos ao lado,
 * como as bandeiras fincadas pelas missões que foram à Lua.
 */
public enum Bandeiras {
    BRASIL, EUA, CHINA, RUSSIA, INDIA, JAPAO, FRANCA, ITALIA, ALEMANHA, ARGENTINA;

    /** Quem coloca blocos (mundo, estrutura em geração, etc.). */
    @FunctionalInterface
    public interface Colocador {
        void set(BlockPos pos, BlockState state);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Bandeiras aleatoria(RandomSource random) {
        Bandeiras[] todas = values();
        return todas[random.nextInt(todas.length)];
    }

    public static Bandeiras estrangeira(RandomSource random) {
        Bandeiras[] todas = values();
        return todas[1 + random.nextInt(todas.length - 1)];
    }

    /**
     * Finca a bandeira. {@code frente} é o lado de onde ela é vista corretamente;
     * o mastro fica na esquerda de quem olha e o tecido se estende para a direita.
     */
    public void fincar(Colocador c, BlockPos base, Direction frente, int alturaMastro) {
        BlockState mastro = ModBlocks.FLAG_POLE.get().defaultBlockState();
        for (int h = 0; h < alturaMastro; h++) {
            c.set(base.above(h), mastro);
        }
        c.set(base.above(alturaMastro), ModBlocks.FLAG_POLE_TOP.get().defaultBlockState());
        Direction direita = frente.getCounterClockWise();
        BlockState tecido = ModBlocks.FLAGS.get(this).get().defaultBlockState()
                .setValue(FlagClothBlock.FACING, frente);
        for (int linha = 0; linha < 2; linha++) {
            for (int coluna = 0; coluna < 3; coluna++) {
                BlockPos p = base.above(alturaMastro - 1 - linha).relative(direita, coluna + 1);
                c.set(p, tecido.setValue(FlagClothBlock.PART, coluna + linha * 3));
            }
        }
    }
}

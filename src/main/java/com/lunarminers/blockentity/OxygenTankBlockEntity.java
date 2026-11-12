package com.lunarminers.blockentity;

import com.lunarminers.registry.ModBlockEntities;
import com.lunarminers.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Válvula do tanque de oxigênio: guarda o O₂ do tanque inteiro.
 * Cada corpo de tanque empilhado embaixo aumenta a capacidade.
 */
public class OxygenTankBlockEntity extends BlockEntity {
    public static final int POR_SECAO = 8000;
    private int oxigenio;

    public OxygenTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OXYGEN_TANK.get(), pos, state);
    }

    public int capacidade() {
        if (level == null) {
            return POR_SECAO;
        }
        int corpos = 0;
        BlockPos p = worldPosition.below();
        while (corpos < 6 && level.getBlockState(p).is(ModBlocks.OXYGEN_TANK.get())) {
            corpos++;
            p = p.below();
        }
        return POR_SECAO * (1 + corpos);
    }

    public int oxigenio() {
        return oxigenio;
    }

    public void encher() {
        oxigenio = capacidade();
        setChanged();
    }

    public int colocar(int quantidade) {
        int cabe = Math.min(quantidade, capacidade() - oxigenio);
        if (cabe > 0) {
            oxigenio += cabe;
            setChanged();
        }
        return Math.max(0, cabe);
    }

    public int tirar(int quantidade) {
        int tirado = Math.min(quantidade, oxigenio);
        if (tirado > 0) {
            oxigenio -= tirado;
            setChanged();
        }
        return tirado;
    }

    /** Válvulas de tanque registradas perto de um ponto (rede de O₂ da base). */
    public static List<OxygenTankBlockEntity> proximos(Level level, BlockPos centro, int raio) {
        List<OxygenTankBlockEntity> lista = new ArrayList<>();
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) {
            return lista;
        }
        long r2 = (long) raio * raio;
        for (BlockPos p : com.lunarminers.world.OxygenData.get(server).tanques()) {
            if (p.distSqr(centro) <= r2 && level.isLoaded(p) && level.getBlockEntity(p) instanceof OxygenTankBlockEntity t) {
                lista.add(t);
            }
        }
        return lista;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        oxigenio = tag.getInt("o2");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("o2", oxigenio);
    }
}

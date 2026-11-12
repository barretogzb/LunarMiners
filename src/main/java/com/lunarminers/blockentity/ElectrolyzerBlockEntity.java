package com.lunarminers.blockentity;

import com.lunarminers.energy.EnergyBlockEntity;
import com.lunarminers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Eletrolisador: quebra o gelo lunar (água) em oxigênio usando energia
 * e manda o O₂ para os tanques num raio de 6 blocos.
 */
public class ElectrolyzerBlockEntity extends EnergyBlockEntity {
    public static final int CUSTO_POR_CICLO = 1200; // 60 FE/t
    public static final int O2_POR_GELO = 900;
    public static final int MAX_GELO = 128;
    private int gelo;

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYZER.get(), pos, state, 20_000, 2000, 0);
    }

    public int gelo() {
        return gelo;
    }

    public int adicionarGelo(int fragmentos) {
        int cabe = Math.min(fragmentos, MAX_GELO - gelo);
        gelo += cabe;
        setChanged();
        return cabe;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectrolyzerBlockEntity e) {
        if (level.getGameTime() % 20 != 0) {
            return;
        }
        boolean trabalhou = false;
        if (e.gelo > 0 && e.energia.getEnergyStored() >= CUSTO_POR_CICLO) {
            int restante = O2_POR_GELO;
            for (OxygenTankBlockEntity t : OxygenTankBlockEntity.proximos(level, pos, com.lunarminers.world.OxygenData.RAIO_TANQUE)) {
                restante -= t.colocar(restante);
                if (restante <= 0) {
                    break;
                }
            }
            if (restante < O2_POR_GELO) {
                e.gelo--;
                e.energia.gastar(CUSTO_POR_CICLO);
                e.setChanged();
                trabalhou = true;
            }
        }
        if (state.getValue(BlockStateProperties.LIT) != trabalhou) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, trabalhou), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        gelo = tag.getInt("gelo");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("gelo", gelo);
    }
}

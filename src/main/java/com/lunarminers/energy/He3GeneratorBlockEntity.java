package com.lunarminers.energy;

import com.lunarminers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Gerador de fusão de Hélio-3: queima cristais e dá muita energia, de dia ou de noite. */
public class He3GeneratorBlockEntity extends EnergyBlockEntity implements EnergyBlockEntity.GeradorMarcador {
    public static final int GERACAO = 160;
    public static final int TICKS_POR_CRISTAL = 2500;
    public static final int MAX_CRISTAIS = 16;
    private int combustivel;

    public He3GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HE3_GENERATOR.get(), pos, state, 50_000, 0, 2000);
    }

    public boolean adicionarCristal() {
        if (combustivel + TICKS_POR_CRISTAL > TICKS_POR_CRISTAL * MAX_CRISTAIS) {
            return false;
        }
        combustivel += TICKS_POR_CRISTAL;
        setChanged();
        return true;
    }

    public int combustivelTicks() {
        return combustivel;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, He3GeneratorBlockEntity g) {
        boolean ligado = g.combustivel > 0 && g.energia.getEnergyStored() < g.energia.getMaxEnergyStored();
        if (ligado) {
            g.combustivel--;
            g.energia.gerar(GERACAO);
        }
        if (state.getValue(BlockStateProperties.LIT) != ligado) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, ligado), 3);
        }
        g.empurrar(2000);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        combustivel = tag.getInt("combustivel");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("combustivel", combustivel);
    }
}

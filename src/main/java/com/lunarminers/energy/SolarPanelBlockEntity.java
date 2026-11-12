package com.lunarminers.energy;

import com.lunarminers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/** Painel solar: gera energia de dia, com o céu aberto (na Lua não há nuvens). */
public class SolarPanelBlockEntity extends EnergyBlockEntity implements EnergyBlockEntity.GeradorMarcador {
    public static final int GERACAO = 15;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state, 4000, 0, 400);
    }

    public static boolean temSol(Level level, BlockPos pos) {
        return level.isDay() && level.getBrightness(LightLayer.SKY, pos.above()) >= 14;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity painel) {
        if (temSol(level, pos)) {
            painel.energia.gerar(GERACAO);
        }
        painel.empurrar(400);
    }
}

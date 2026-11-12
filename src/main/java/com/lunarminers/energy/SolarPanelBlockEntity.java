package com.lunarminers.energy;

import com.lunarminers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/** Painel solar: gera energia de dia, com o céu aberto (na Lua não há nuvens). */
public class SolarPanelBlockEntity extends EnergyBlockEntity implements EnergyBlockEntity.GeradorMarcador {
    public static final int GERACAO = 15;
    public static final int GERACAO_GRANDE = 60;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state, 12000, 0, 1000);
    }

    public static boolean temSol(Level level, BlockPos pos) {
        return level.isDay() && level.getBrightness(LightLayer.SKY, pos.above()) >= 14;
    }

    /** Quanto este painel gera agora (o grande gera 4x; a tempestade solar triplica). */
    public int geracaoAtual() {
        if (level == null || !temSol(level, worldPosition)) {
            return 0;
        }
        int base = getBlockState().is(com.lunarminers.registry.ModBlocks.SOLAR_ARRAY.get()) ? GERACAO_GRANDE : GERACAO;
        if (level instanceof net.minecraft.server.level.ServerLevel sl
                && com.lunarminers.world.EventosLunares.get(sl).tempestadeAtiva(sl.getGameTime())) {
            base *= 3;
        }
        return base;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity painel) {
        painel.energia.gerar(painel.geracaoAtual());
        painel.empurrar(1000);
    }
}

package com.lunarminers.blockentity;

import com.lunarminers.energy.EnergyBlockEntity;
import com.lunarminers.registry.ModBlockEntities;
import com.lunarminers.world.OxygenData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Distribuidor de Oxigênio: a cada segundo gasta energia e puxa O₂ dos tanques próximos.
 * Mais gente respirando = mais consumo. Sem energia ou sem O₂, desliga e o ar acaba.
 */
public class DistributorBlockEntity extends EnergyBlockEntity {
    public static final int ENERGIA_POR_SEGUNDO = 400; // 20 FE/t
    public static final int O2_BASE = 4;
    public static final int O2_POR_PESSOA = 30;
    private boolean ativo;
    private String motivo = "";

    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTRIBUTOR.get(), pos, state, 10_000, 2000, 0);
    }

    public boolean ativo() {
        return ativo;
    }

    public String motivo() {
        return motivo;
    }

    public int oxigenioDisponivel() {
        if (level == null) {
            return 0;
        }
        int total = 0;
        for (OxygenTankBlockEntity t : OxygenTankBlockEntity.proximos(level, worldPosition, OxygenData.RAIO_TANQUE)) {
            total += t.oxigenio();
        }
        return total;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DistributorBlockEntity d) {
        if (level.getGameTime() % 20 != 0) {
            return;
        }
        long raio2 = (long) OxygenData.RAIO_AR * OxygenData.RAIO_AR;
        int pessoas = (int) level.players().stream()
                .filter(p -> !p.isSpectator() && p.blockPosition().distSqr(pos) <= raio2).count();
        int custoO2 = O2_BASE + O2_POR_PESSOA * pessoas;

        boolean agora;
        if (d.energia.getEnergyStored() < ENERGIA_POR_SEGUNDO) {
            agora = false;
            d.motivo = "energy";
        } else {
            int falta = custoO2;
            for (OxygenTankBlockEntity t : OxygenTankBlockEntity.proximos(level, pos, OxygenData.RAIO_TANQUE)) {
                falta -= t.tirar(falta);
                if (falta <= 0) {
                    break;
                }
            }
            agora = falta <= 0;
            d.motivo = agora ? "" : "oxygen";
            if (agora) {
                d.energia.gastar(ENERGIA_POR_SEGUNDO);
            }
        }
        if (agora != d.ativo) {
            d.ativo = agora;
            d.setChanged();
        }
        if (state.getValue(BlockStateProperties.LIT) != agora) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, agora), 3);
        }
    }
}

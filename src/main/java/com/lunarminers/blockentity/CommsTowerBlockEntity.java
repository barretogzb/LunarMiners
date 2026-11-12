package com.lunarminers.blockentity;

import com.lunarminers.energy.EnergyBlockEntity;
import com.lunarminers.entity.ImpactEntity;
import com.lunarminers.registry.ModBlockEntities;
import com.lunarminers.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Torre de Comunicação: com energia suficiente, chama a Terra e pede uma
 * cápsula de suprimentos, que desce perto da torre. Uma chamada por dia lunar.
 */
public class CommsTowerBlockEntity extends EnergyBlockEntity {
    public static final int CUSTO = 150_000;
    public static final int ESPERA = 12_000;
    private long ultimaChamada = -ESPERA;

    public CommsTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMMS_TOWER.get(), pos, state, 200_000, 4000, 0);
    }

    public long faltaParaChamar(long agora) {
        return Math.max(0, ultimaChamada + ESPERA - agora);
    }

    /** @return 0 = enviado, 1 = sem energia, 2 = aguardando */
    public int chamar(ServerLevel level) {
        long agora = level.getGameTime();
        if (faltaParaChamar(agora) > 0) {
            return 2;
        }
        if (!energia.gastar(CUSTO)) {
            return 1;
        }
        ultimaChamada = agora;
        setChanged();
        ImpactEntity capsula = ModEntities.IMPACT.get().create(level);
        if (capsula != null) {
            double ang = level.random.nextDouble() * Math.PI * 2;
            double dist = 6 + level.random.nextInt(6);
            capsula.tipo(ImpactEntity.CAPSULA);
            capsula.moveTo(worldPosition.getX() + 0.5 + Math.cos(ang) * dist, worldPosition.getY() + 110,
                    worldPosition.getZ() + 0.5 + Math.sin(ang) * dist, 0, 0);
            capsula.setDeltaMovement(0, -0.55, 0);
            level.addFreshEntity(capsula);
        }
        return 0;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ultimaChamada = tag.contains("ultima") ? tag.getLong("ultima") : -ESPERA;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("ultima", ultimaChamada);
    }
}

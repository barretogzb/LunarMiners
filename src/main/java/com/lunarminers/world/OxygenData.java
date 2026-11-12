package com.lunarminers.world;

import com.lunarminers.blockentity.DistributorBlockEntity;
import com.lunarminers.blockentity.OxygenTankBlockEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Rede de oxigênio do mundo: onde estão os distribuidores e as válvulas de tanque.
 * Um distribuidor puxa O₂ de qualquer tanque num raio de 32 blocos (a base inteira).
 */
public class OxygenData extends SavedData {
    private static final String NAME = "lunarminers_oxygen";
    public static final int RAIO_AR = 18;
    public static final int RAIO_TANQUE = 32;

    private final Set<Long> distribuidores = new HashSet<>();
    private final Set<Long> tanques = new HashSet<>();

    public static OxygenData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(OxygenData::load, OxygenData::new, NAME);
    }

    public static OxygenData load(CompoundTag tag) {
        OxygenData data = new OxygenData();
        for (long l : tag.getLongArray("d")) {
            data.distribuidores.add(l);
        }
        for (long l : tag.getLongArray("t")) {
            data.tanques.add(l);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("d", distribuidores.stream().mapToLong(Long::longValue).toArray());
        tag.putLongArray("t", tanques.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }

    public void adicionar(BlockPos pos) {
        if (distribuidores.add(pos.asLong())) {
            setDirty();
        }
    }

    public void remover(BlockPos pos) {
        if (distribuidores.remove(pos.asLong())) {
            setDirty();
        }
    }

    public void adicionarTanque(BlockPos pos) {
        if (tanques.add(pos.asLong())) {
            setDirty();
        }
    }

    public void removerTanque(BlockPos pos) {
        if (tanques.remove(pos.asLong())) {
            setDirty();
        }
    }

    public List<BlockPos> tanques() {
        List<BlockPos> lista = new ArrayList<>(tanques.size());
        for (long l : tanques) {
            lista.add(BlockPos.of(l));
        }
        return lista;
    }

    /** O ar é respirável perto de um distribuidor ligado (com energia e oxigênio). */
    public boolean respiravel(ServerLevel level, BlockPos pos) {
        long raio2 = (long) RAIO_AR * RAIO_AR;
        for (long l : distribuidores) {
            BlockPos d = BlockPos.of(l);
            if (d.distSqr(pos) > raio2 || !level.isLoaded(d)) {
                continue;
            }
            if (level.getBlockEntity(d) instanceof DistributorBlockEntity dist && dist.ativo()) {
                return true;
            }
        }
        return false;
    }

    public static int oxigenioPerto(ServerLevel level, BlockPos pos) {
        int total = 0;
        for (OxygenTankBlockEntity t : OxygenTankBlockEntity.proximos(level, pos, RAIO_TANQUE)) {
            total += t.oxigenio();
        }
        return total;
    }
}

package com.lunarminers.world;

import com.lunarminers.block.OxygenDistributorBlock;
import com.lunarminers.registry.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Lista de Distribuidores de Oxigênio do mundo. Um distribuidor só funciona
 * se tiver uma válvula de tanque de oxigênio por perto — os tanques são a reserva da base.
 */
public class OxygenData extends SavedData {
    private static final String NAME = "lunarminers_oxygen";
    public static final int RAIO_AR = 18;
    public static final int RAIO_TANQUE = 6;

    private final Set<Long> distribuidores = new HashSet<>();

    public static OxygenData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(OxygenData::load, OxygenData::new, NAME);
    }

    public static OxygenData load(CompoundTag tag) {
        OxygenData data = new OxygenData();
        for (long l : tag.getLongArray("d")) {
            data.distribuidores.add(l);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("d", distribuidores.stream().mapToLong(Long::longValue).toArray());
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

    public static int contarTanques(ServerLevel level, BlockPos centro) {
        int n = 0;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -RAIO_TANQUE; dx <= RAIO_TANQUE; dx++) {
            for (int dy = -3; dy <= 4; dy++) {
                for (int dz = -RAIO_TANQUE; dz <= RAIO_TANQUE; dz++) {
                    p.set(centro.getX() + dx, centro.getY() + dy, centro.getZ() + dz);
                    if (level.getBlockState(p).is(ModBlocks.OXYGEN_TANK_CAP.get())) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    /** O ar é respirável se houver um distribuidor ativo (com tanque) perto. */
    public boolean respiravel(ServerLevel level, BlockPos pos) {
        long raio2 = (long) RAIO_AR * RAIO_AR;
        for (long l : distribuidores) {
            BlockPos d = BlockPos.of(l);
            if (d.distSqr(pos) > raio2 || !level.isLoaded(d)) {
                continue;
            }
            if (!(level.getBlockState(d).getBlock() instanceof OxygenDistributorBlock)) {
                continue;
            }
            if (contarTanques(level, d) > 0) {
                return true;
            }
        }
        return false;
    }
}

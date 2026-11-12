package com.lunarminers.energy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

/** Base dos blocos que guardam energia (Forge Energy, compatível com outros mods). */
public abstract class EnergyBlockEntity extends BlockEntity {
    protected final Bateria energia;
    private final LazyOptional<IEnergyStorage> opcional;

    protected EnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                int capacidade, int entrada, int saida) {
        super(type, pos, state);
        this.energia = new Bateria(capacidade, entrada, saida, this::setChanged);
        this.opcional = LazyOptional.of(() -> energia);
    }

    public Bateria energia() {
        return energia;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return opcional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        opcional.invalidate();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energia.definir(tag.getInt("energia"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energia", energia.getEnergyStored());
    }

    @Nullable
    protected IEnergyStorage vizinho(Direction d) {
        if (level == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(worldPosition.relative(d));
        if (be == null) {
            return null;
        }
        return be.getCapability(ForgeCapabilities.ENERGY, d.getOpposite()).orElse(null);
    }

    /** Empurra energia para quem aceita em volta (máquinas, cabos). */
    protected void empurrar(int maximo) {
        for (Direction d : Direction.values()) {
            if (energia.getEnergyStored() <= 0) {
                return;
            }
            if (level != null && level.getBlockEntity(worldPosition.relative(d)) instanceof BatteryBlockEntity
                    && !(this instanceof GeradorMarcador)) {
                continue;
            }
            IEnergyStorage alvo = vizinho(d);
            if (alvo == null || !alvo.canReceive()) {
                continue;
            }
            int oferta = Math.min(maximo, energia.getEnergyStored());
            int aceito = alvo.receiveEnergy(oferta, false);
            if (aceito > 0) {
                energia.gastar(aceito);
            }
        }
    }

    /** Marca geradores (podem carregar baterias diretamente). */
    public interface GeradorMarcador {
    }
}

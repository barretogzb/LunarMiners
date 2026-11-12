package com.lunarminers.energy;

import com.lunarminers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;

/** Cabo: equaliza a energia com os cabos vizinhos e entrega para as máquinas encostadas. */
public class CableBlockEntity extends EnergyBlockEntity {
    public static final int CAPACIDADE = 2000;
    private static final int VAZAO = 1000;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE.get(), pos, state, CAPACIDADE, VAZAO, VAZAO);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CableBlockEntity cabo) {
        for (Direction d : Direction.values()) {
            int meu = cabo.energia.getEnergyStored();
            if (meu <= 0) {
                return;
            }
            BlockEntity be = level.getBlockEntity(pos.relative(d));
            if (be instanceof CableBlockEntity outro) {
                int diff = meu - outro.energia.getEnergyStored();
                if (diff > 1) {
                    int mover = Math.min(diff / 2, VAZAO);
                    cabo.energia.gastar(mover);
                    outro.energia.gerar(mover);
                }
            } else if (be instanceof BatteryBlockEntity || be instanceof GeradorMarcador) {
                // a bateria puxa/empurra sozinha; geradores não recebem
            } else if (be != null) {
                IEnergyStorage alvo = cabo.vizinho(d);
                if (alvo != null && alvo.canReceive()) {
                    int aceito = alvo.receiveEnergy(Math.min(VAZAO, meu), false);
                    if (aceito > 0) {
                        cabo.energia.gastar(aceito);
                    }
                }
            }
        }
    }
}

package com.lunarminers.energy;

import com.lunarminers.block.EnergyMachineBlock;
import com.lunarminers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;

/** Banco de baterias: guarda a energia do dia para a noite e mantém a rede de cabos abastecida. */
public class BatteryBlockEntity extends EnergyBlockEntity {
    public static final int CAPACIDADE = 400_000;
    private static final int VAZAO = 2000;

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY.get(), pos, state, CAPACIDADE, VAZAO, VAZAO);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BatteryBlockEntity bat) {
        for (Direction d : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(d));
            if (be instanceof CableBlockEntity cabo) {
                int noCabo = cabo.energia.getEnergyStored();
                int metade = CableBlockEntity.CAPACIDADE / 2;
                if (noCabo > metade * 1.2) {
                    int puxar = Math.min(Math.min(noCabo - metade, VAZAO),
                            bat.energia.getMaxEnergyStored() - bat.energia.getEnergyStored());
                    if (puxar > 0) {
                        cabo.energia.gastar(puxar);
                        bat.energia.gerar(puxar);
                    }
                } else if (noCabo < metade * 0.8) {
                    int dar = Math.min(Math.min(metade - noCabo, VAZAO), bat.energia.getEnergyStored());
                    if (dar > 0) {
                        bat.energia.gastar(dar);
                        cabo.energia.gerar(dar);
                    }
                }
            } else if (be != null && !(be instanceof BatteryBlockEntity) && !(be instanceof GeradorMarcador)) {
                IEnergyStorage alvo = bat.vizinho(d);
                if (alvo != null && alvo.canReceive() && bat.energia.getEnergyStored() > 0) {
                    int aceito = alvo.receiveEnergy(Math.min(VAZAO, bat.energia.getEnergyStored()), false);
                    if (aceito > 0) {
                        bat.energia.gastar(aceito);
                    }
                }
            }
        }
        if (level.getGameTime() % 20 == 0) {
            int nivel = Math.round(bat.energia.fracao() * 4);
            if (state.getValue(EnergyMachineBlock.LEVEL) != nivel) {
                level.setBlock(pos, state.setValue(EnergyMachineBlock.LEVEL, nivel), 3);
            }
        }
    }
}

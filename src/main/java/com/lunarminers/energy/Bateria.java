package com.lunarminers.energy;

import net.minecraftforge.energy.EnergyStorage;

/** Armazenamento de energia que avisa quando muda (para salvar o bloco). */
public class Bateria extends EnergyStorage {
    private final Runnable aoMudar;

    public Bateria(int capacidade, int entrada, int saida, Runnable aoMudar) {
        super(capacidade, entrada, saida);
        this.aoMudar = aoMudar;
    }

    @Override
    public int receiveEnergy(int max, boolean simular) {
        int r = super.receiveEnergy(max, simular);
        if (r > 0 && !simular) {
            aoMudar.run();
        }
        return r;
    }

    @Override
    public int extractEnergy(int max, boolean simular) {
        int r = super.extractEnergy(max, simular);
        if (r > 0 && !simular) {
            aoMudar.run();
        }
        return r;
    }

    /** Mexe na energia por dentro (gerar ou gastar), ignorando os limites de entrada/saída. */
    public void gerar(int quantidade) {
        int antes = energy;
        energy = Math.min(capacity, energy + quantidade);
        if (energy != antes) {
            aoMudar.run();
        }
    }

    public boolean gastar(int quantidade) {
        if (energy < quantidade) {
            return false;
        }
        energy -= quantidade;
        aoMudar.run();
        return true;
    }

    public void definir(int valor) {
        energy = Math.max(0, Math.min(capacity, valor));
    }

    public float fracao() {
        return capacity == 0 ? 0 : (float) energy / capacity;
    }
}

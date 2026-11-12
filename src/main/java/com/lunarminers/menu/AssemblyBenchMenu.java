package com.lunarminers.menu;

import com.lunarminers.registry.ModBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

/** Menu de fabricação 3x3 que continua aberto enquanto o jogador estiver perto da Bancada de Montagem. */
public class AssemblyBenchMenu extends CraftingMenu {
    private final ContainerLevelAccess bench;

    public AssemblyBenchMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(id, inventory, access);
        this.bench = access;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(bench, player, ModBlocks.ASSEMBLY_BENCH.get());
    }
}

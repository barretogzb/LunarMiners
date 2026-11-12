package com.lunarminers.blockentity;

import com.lunarminers.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SolarFurnaceBlockEntity extends AbstractFurnaceBlockEntity {

    public SolarFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_FURNACE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.lunarminers.solar_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new FurnaceMenu(id, inventory, this, this.dataAccess);
    }

    /** Com Sol batendo (luz do céu máxima e de dia) o forno fica "aceso" sem gastar combustível. */
    public static boolean recebeSol(Level level, BlockPos pos) {
        return level.isDay() && level.getBrightness(LightLayer.SKY, pos.above()) >= 14;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarFurnaceBlockEntity furnace) {
        if (recebeSol(level, pos) && !furnace.getItem(0).isEmpty()) {
            furnace.dataAccess.set(0, 200);
            furnace.dataAccess.set(1, 200);
        }
        AbstractFurnaceBlockEntity.serverTick(level, pos, state, furnace);
    }
}

package com.lunarminers.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Módulo de melhoria do traje. Instale juntando a peça do traje + o módulo numa bigorna. */
public class ModuleItem extends Item {
    public static final String TAG_PREFIX = "lm_mod_";

    public enum Tipo {
        TANQUE("o2_tank", ArmorItem.Type.CHESTPLATE, 3),
        VISOR("night_vision", ArmorItem.Type.HELMET, 1),
        SERVO("servo", ArmorItem.Type.LEGGINGS, 2),
        PROPULSOR("thruster", ArmorItem.Type.BOOTS, 1),
        JETPACK("jetpack", ArmorItem.Type.CHESTPLATE, 1);

        public final String id;
        public final ArmorItem.Type peca;
        public final int maximo;

        Tipo(String id, ArmorItem.Type peca, int maximo) {
            this.id = id;
            this.peca = peca;
            this.maximo = maximo;
        }
    }

    public final Tipo tipo;

    public ModuleItem(Tipo tipo, Properties properties) {
        super(properties);
        this.tipo = tipo;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lunarminers.module." + tipo.id).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lunarminers.module_how").withStyle(ChatFormatting.DARK_GRAY));
    }
}

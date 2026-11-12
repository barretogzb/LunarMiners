package com.lunarminers.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Peça de traje espacial: capacete e peitoral carregam oxigênio; módulos podem ser instalados na bigorna. */
public class SuitArmorItem extends ArmorItem {
    private final int oxigenioSegundos;

    public SuitArmorItem(ArmorMaterial material, Type type, int oxigenioSegundos, Properties properties) {
        super(material, type, properties);
        this.oxigenioSegundos = oxigenioSegundos;
    }

    public int oxigenioSegundos(ItemStack stack) {
        int extra = stack.hasTag() ? stack.getTag().getInt(ModuleItem.TAG_PREFIX + ModuleItem.Tipo.TANQUE.id) : 0;
        return oxigenioSegundos + extra * 300;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int o2 = oxigenioSegundos(stack);
        if (o2 > 0) {
            tooltip.add(Component.translatable("tooltip.lunarminers.suit_oxygen", o2 / 60, o2 % 60)
                    .withStyle(ChatFormatting.AQUA));
        }
        for (ModuleItem.Tipo tipo : ModuleItem.Tipo.values()) {
            int n = stack.hasTag() ? stack.getTag().getInt(ModuleItem.TAG_PREFIX + tipo.id) : 0;
            if (n > 0) {
                tooltip.add(Component.translatable("tooltip.lunarminers.module_installed",
                        Component.translatable("item.lunarminers." + tipo.id + "_module"), n).withStyle(ChatFormatting.GREEN));
            }
        }
        if (getMaterial() == ModArmorMaterials.TITANIUM_SUIT) {
            tooltip.add(Component.translatable("tooltip.lunarminers.titanium_set").withStyle(ChatFormatting.GRAY));
        }
    }
}

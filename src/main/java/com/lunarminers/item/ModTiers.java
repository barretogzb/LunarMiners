package com.lunarminers.item;

import com.lunarminers.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

/** Materiais das ferramentas. Cada metal tem um papel: */
public final class ModTiers {
    /** Alumínio: barato e leve, a primeira ferramenta da Lua. */
    public static final Tier ALUMINUM = new ForgeTier(2, 320, 6.5F, 1.5F, 16,
            BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(ModItems.ALUMINUM_INGOT.get()));

    /** Nióbio: supercondutor, minera muito rápido mas gasta mais. */
    public static final Tier NIOBIUM = new ForgeTier(2, 520, 11.0F, 2.0F, 18,
            BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(ModItems.NIOBIUM_INGOT.get()));

    /** Titânio: o mais resistente, quebra o que o diamante quebra e dura muito. */
    public static final Tier TITANIUM = new ForgeTier(3, 1800, 8.0F, 3.0F, 12,
            BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(ModItems.TITANIUM_INGOT.get()));

    private ModTiers() {}
}

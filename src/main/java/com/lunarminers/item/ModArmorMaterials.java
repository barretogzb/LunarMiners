package com.lunarminers.item;

import com.lunarminers.LunarMiners;
import com.lunarminers.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModArmorMaterials implements ArmorMaterial {
    // proteção: capacete, peitoral, calça, botas
    SPACE_SUIT("space_suit", 18, new int[]{2, 5, 4, 2}, 12, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
            () -> Ingredient.of(ModItems.ALUMINUM_INGOT.get())),
    TITANIUM_SUIT("titanium_suit", 36, new int[]{3, 8, 6, 3}, 14, SoundEvents.ARMOR_EQUIP_IRON, 2.5F, 0.1F,
            () -> Ingredient.of(ModItems.TITANIUM_INGOT.get()));

    private static final int[] DURABILIDADE_BASE = {11, 16, 15, 13};

    private final String nome;
    private final int multiplicador;
    private final int[] protecao;
    private final int encantabilidade;
    private final SoundEvent som;
    private final float resistencia;
    private final float repulsao;
    private final Supplier<Ingredient> reparo;

    ModArmorMaterials(String nome, int multiplicador, int[] protecao, int encantabilidade, SoundEvent som,
                      float resistencia, float repulsao, Supplier<Ingredient> reparo) {
        this.nome = nome;
        this.multiplicador = multiplicador;
        this.protecao = protecao;
        this.encantabilidade = encantabilidade;
        this.som = som;
        this.resistencia = resistencia;
        this.repulsao = repulsao;
        this.reparo = reparo;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return DURABILIDADE_BASE[type.ordinal()] * multiplicador;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return protecao[type.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return encantabilidade;
    }

    @Override
    public SoundEvent getEquipSound() {
        return som;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return reparo.get();
    }

    @Override
    public String getName() {
        return LunarMiners.MOD_ID + ":" + nome;
    }

    @Override
    public float getToughness() {
        return resistencia;
    }

    @Override
    public float getKnockbackResistance() {
        return repulsao;
    }
}

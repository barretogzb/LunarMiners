package com.lunarminers.world;

import com.lunarminers.item.ModArmorMaterials;
import com.lunarminers.item.ModuleItem;
import com.lunarminers.item.SuitArmorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Regras de oxigênio na Lua:
 * - Sem traje você aguenta só 15 segundos fora da área pressurizada.
 * - O capacete e o peitoral do traje carregam oxigênio (mais com módulos de tanque).
 * - Perto de um Distribuidor de Oxigênio com tanque, o ar é respirável e o traje recarrega.
 * - A barra de bolhas mostra quanto oxigênio resta.
 */
public final class OxygenSystem {
    private static final String TAG_O2 = "lm_o2";
    private static final String TAG_AR = "lm_ar";
    private static final int BASE_TICKS = 15 * 20;

    public static int oxigenioMaximo(Player player) {
        int total = BASE_TICKS;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof SuitArmorItem suit) {
                total += suit.oxigenioSegundos(stack) * 20;
            }
        }
        return total;
    }

    public static int modulos(Player player, ModuleItem.Tipo tipo) {
        ItemStack stack = player.getItemBySlot(slotDe(tipo.peca));
        if (!(stack.getItem() instanceof SuitArmorItem) || !stack.hasTag()) {
            return 0;
        }
        return stack.getTag().getInt(ModuleItem.TAG_PREFIX + tipo.id);
    }

    public static EquipmentSlot slotDe(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }

    private static boolean conjuntoTitanio(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(player.getItemBySlot(slot).getItem() instanceof SuitArmorItem suit)
                    || suit.getMaterial() != ModArmorMaterials.TITANIUM_SUIT) {
                return false;
            }
        }
        return true;
    }

    public static void tick(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        CompoundTag data = player.getPersistentData();
        int max = oxigenioMaximo(player);
        int o2 = data.contains(TAG_O2) ? data.getInt(TAG_O2) : max;

        if (player.tickCount % 10 == 0) {
            data.putBoolean(TAG_AR, OxygenData.get(level).respiravel(level, player.blockPosition()));
        }
        boolean respiravel = data.getBoolean(TAG_AR);

        if (respiravel) {
            o2 = Math.min(max, o2 + 12);
        } else {
            o2 = Math.min(max, o2 - 1);
        }

        if (o2 <= 0) {
            o2 = 0;
            if (player.tickCount % 20 == 0) {
                player.hurt(level.damageSources().drown(), 2.0F);
                player.displayClientMessage(Component.translatable("message.lunarminers.no_oxygen")
                        .withStyle(ChatFormatting.RED), true);
            }
        } else if (!respiravel && o2 < max / 4 && player.tickCount % 40 == 0) {
            player.displayClientMessage(Component.translatable("message.lunarminers.low_oxygen", o2 / 20)
                    .withStyle(ChatFormatting.GOLD), true);
        }
        data.putInt(TAG_O2, o2);

        if (!player.isEyeInFluid(FluidTags.WATER)) {
            player.setAirSupply((int) ((long) o2 * player.getMaxAirSupply() / Math.max(1, max)));
        }

        if (player.tickCount % 40 == 0) {
            aplicarModulos(player);
        }
    }

    private static void aplicarModulos(ServerPlayer player) {
        if (modulos(player, ModuleItem.Tipo.VISOR) > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, true));
        }
        int servo = modulos(player, ModuleItem.Tipo.SERVO);
        if (servo > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, servo - 1, true, false, true));
        }
        if (modulos(player, ModuleItem.Tipo.PROPULSOR) > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, true, false, true));
        }
        if (conjuntoTitanio(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false, true));
        }
    }

    /** Ao entrar no mundo pela primeira vez, começa com o tanque cheio. */
    public static void encher(ServerPlayer player) {
        player.getPersistentData().putInt(TAG_O2, oxigenioMaximo(player));
    }

    private OxygenSystem() {}
}

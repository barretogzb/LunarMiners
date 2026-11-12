package com.lunarminers.world;

import com.lunarminers.entity.ImpactEntity;
import com.lunarminers.item.ModArmorMaterials;
import com.lunarminers.item.SuitArmorItem;
import com.lunarminers.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Eventos da Lua, inspirados em filmes e jogos de sobrevivência espacial:
 * - Chuva de meteoros (à noite): rochas em chamas caem perto dos jogadores e deixam minério de meteorito.
 * - Tempestade solar (de dia): radiação para quem estiver a céu aberto; painéis solares geram 3x.
 */
public class EventosLunares extends SavedData {
    private static final String NAME = "lunarminers_eventos";
    private static final int DIA = 24000;

    private long proximaChuva = -1;
    private long fimChuva;
    private long proximaTempestade = -1;
    private long fimTempestade;
    private boolean avisouTempestade;

    public static EventosLunares get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(EventosLunares::load, EventosLunares::new, NAME);
    }

    public static EventosLunares load(CompoundTag t) {
        EventosLunares e = new EventosLunares();
        e.proximaChuva = t.getLong("pc");
        e.fimChuva = t.getLong("fc");
        e.proximaTempestade = t.getLong("pt");
        e.fimTempestade = t.getLong("ft");
        e.avisouTempestade = t.getBoolean("at");
        return e;
    }

    @Override
    public CompoundTag save(CompoundTag t) {
        t.putLong("pc", proximaChuva);
        t.putLong("fc", fimChuva);
        t.putLong("pt", proximaTempestade);
        t.putLong("ft", fimTempestade);
        t.putBoolean("at", avisouTempestade);
        return t;
    }

    public boolean chuvaAtiva(long agora) {
        return agora < fimChuva;
    }

    public boolean tempestadeAtiva(long agora) {
        return agora < fimTempestade;
    }

    private static void anunciar(ServerLevel level, Component msg) {
        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(msg);
            p.playNotifySound(SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.MASTER, 1.0F, 0.6F);
        }
    }

    public void iniciarChuva(ServerLevel level) {
        fimChuva = level.getGameTime() + 1400;
        setDirty();
        anunciar(level, Component.translatable("event.lunarminers.meteor_start").withStyle(ChatFormatting.GOLD));
    }

    public void iniciarTempestade(ServerLevel level) {
        fimTempestade = level.getGameTime() + 1800;
        avisouTempestade = false;
        setDirty();
        anunciar(level, Component.translatable("event.lunarminers.storm_start").withStyle(ChatFormatting.RED));
    }

    public void tick(ServerLevel level) {
        long agora = level.getGameTime();
        RandomSource rnd = level.random;
        if (proximaChuva < 0) {
            proximaChuva = agora + DIA + rnd.nextInt(DIA);
            proximaTempestade = agora + DIA * 2 + rnd.nextInt(DIA);
            setDirty();
        }

        // ---- chuva de meteoros: só começa à noite
        if (!chuvaAtiva(agora) && agora >= proximaChuva && !level.isDay()) {
            iniciarChuva(level);
            proximaChuva = agora + DIA + rnd.nextInt(DIA * 2);
        }
        if (chuvaAtiva(agora) && agora % 12 == 0) {
            for (ServerPlayer p : level.players()) {
                if (p.isSpectator() || rnd.nextFloat() > 0.6F) {
                    continue;
                }
                double x = p.getX() + rnd.nextInt(121) - 60;
                double z = p.getZ() + rnd.nextInt(121) - 60;
                LunarBaseData base = LunarBaseData.get(level);
                if (base.built && base.center.closerThan(new net.minecraft.core.BlockPos((int) x, base.center.getY(), (int) z), 55)) {
                    continue;
                }
                ImpactEntity m = ModEntities.IMPACT.get().create(level);
                if (m != null) {
                    m.tipo(ImpactEntity.METEORO);
                    m.moveTo(x, p.getY() + 90, z, 0, 0);
                    m.setDeltaMovement((rnd.nextDouble() - 0.5) * 0.8, -1.4, (rnd.nextDouble() - 0.5) * 0.8);
                    level.addFreshEntity(m);
                }
            }
        }
        if (fimChuva > 0 && agora == fimChuva) {
            anunciar(level, Component.translatable("event.lunarminers.meteor_end").withStyle(ChatFormatting.GRAY));
        }

        // ---- tempestade solar: aviso 30 s antes, só de dia
        if (!tempestadeAtiva(agora) && !avisouTempestade && agora >= proximaTempestade - 600 && level.isDay()) {
            avisouTempestade = true;
            setDirty();
            anunciar(level, Component.translatable("event.lunarminers.storm_warning").withStyle(ChatFormatting.YELLOW));
        }
        if (!tempestadeAtiva(agora) && avisouTempestade && agora >= proximaTempestade) {
            iniciarTempestade(level);
            proximaTempestade = agora + DIA * 2 + rnd.nextInt(DIA * 2);
        }
        if (tempestadeAtiva(agora) && agora % 40 == 0) {
            for (ServerPlayer p : level.players()) {
                if (p.isCreative() || p.isSpectator()) {
                    continue;
                }
                int topo = level.getHeight(Heightmap.Types.MOTION_BLOCKING, p.getBlockX(), p.getBlockZ());
                boolean exposto = p.getEyeY() >= topo;
                if (exposto) {
                    boolean titanio = p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SuitArmorItem s
                            && s.getMaterial() == ModArmorMaterials.TITANIUM_SUIT;
                    p.hurt(level.damageSources().magic(), titanio ? 1.0F : 2.5F);
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                    p.displayClientMessage(Component.translatable("event.lunarminers.storm_exposed")
                            .withStyle(ChatFormatting.RED), true);
                }
            }
        }
        if (fimTempestade > 0 && agora == fimTempestade) {
            anunciar(level, Component.translatable("event.lunarminers.storm_end").withStyle(ChatFormatting.GREEN));
        }
    }
}

package com.lunarminers.event;

import com.lunarminers.LunarMiners;
import com.lunarminers.world.LunarBase;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Regras da Lua: gravidade baixa, queda mais leve, sem chuva e a base inicial. */
@Mod.EventBusSubscriber(modid = LunarMiners.MOD_ID)
public final class LunarEvents {
    private static final UUID LUNAR_GRAVITY = UUID.fromString("6b2f0c8e-1d3a-4c5b-9e7f-2a1b3c4d5e6f");

    /** O mundo principal (overworld) agora é a Lua. */
    public static boolean isMoon(Level level) {
        return level.dimension() == Level.OVERWORLD;
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        AttributeInstance gravity = living.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        if (gravity == null) {
            return;
        }
        boolean moon = isMoon(event.getLevel());
        if (moon && gravity.getModifier(LUNAR_GRAVITY) == null) {
            gravity.addPermanentModifier(new AttributeModifier(
                    LUNAR_GRAVITY, "Gravidade lunar", -0.6D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (!moon && gravity.getModifier(LUNAR_GRAVITY) != null) {
            gravity.removeModifier(LUNAR_GRAVITY);
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (isMoon(event.getEntity().level())) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.4F);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || !isMoon(level)) {
            return;
        }
        if (level.getGameTime() % 100 == 0 && (level.isRaining() || level.isThundering())) {
            level.setWeatherParameters(24000, 0, false, false);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        LunarBase.ensureBuilt(event.getServer().overworld());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LunarBase.welcome(player);
        }
    }

    private LunarEvents() {}
}

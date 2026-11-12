package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.lunarminers.item.ModuleItem;
import com.lunarminers.item.SuitArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Jetpack: com o módulo instalado no suporte de vida, segure PULAR no ar para subir.
 * O movimento do jogador é calculado no cliente; o servidor desconta o O₂ do traje.
 */
@Mod.EventBusSubscriber(modid = LunarMiners.MOD_ID, value = Dist.CLIENT)
public final class JetpackClient {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.isPaused() || p.isPassenger() || p.getAbilities().flying || p.onGround()) {
            return;
        }
        ItemStack peito = p.getItemBySlot(EquipmentSlot.CHEST);
        if (!(peito.getItem() instanceof SuitArmorItem) || !peito.hasTag()
                || peito.getTag().getInt(ModuleItem.TAG_PREFIX + ModuleItem.Tipo.JETPACK.id) <= 0) {
            return;
        }
        if (p.getAirSupply() <= 0 || !mc.options.keyJump.isDown()) {
            return;
        }
        Vec3 v = p.getDeltaMovement();
        p.setDeltaMovement(v.x, Math.min(v.y + 0.09, 0.45), v.z);
        p.fallDistance = 0;
        for (int i = 0; i < 2; i++) {
            p.level().addParticle(ParticleTypes.FLAME, p.getX() + (p.getRandom().nextDouble() - 0.5) * 0.4,
                    p.getY() + 0.6, p.getZ() + (p.getRandom().nextDouble() - 0.5) * 0.4, 0, -0.2, 0);
            p.level().addParticle(ParticleTypes.CLOUD, p.getX(), p.getY() + 0.3, p.getZ(), 0, -0.1, 0);
        }
    }

    private JetpackClient() {}
}

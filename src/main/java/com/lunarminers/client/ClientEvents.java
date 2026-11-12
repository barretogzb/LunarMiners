package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.lunarminers.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LunarMiners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ALIEN.get(), AlienRenderer::new);
        event.registerEntityRenderer(ModEntities.ROVER.get(), RoverRenderer::new);
        event.registerEntityRenderer(ModEntities.IMPACT.get(), ImpactRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RoverModel.LAYER, RoverModel::criarCamada);
    }

    @SubscribeEvent
    public static void onRegisterSkies(RegisterDimensionSpecialEffectsEvent event) {
        event.register(new ResourceLocation(LunarMiners.MOD_ID, "moon"), new MoonSkyEffects());
    }

    private ClientEvents() {}
}

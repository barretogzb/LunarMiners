package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.AlienSpitterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public final class AlienExtraRenderers {
    private AlienExtraRenderers() {}

    /** Cuspidor: corpo humanoide com textura própria. */
    public static class Spitter extends HumanoidMobRenderer<AlienSpitterEntity, HumanoidModel<AlienSpitterEntity>> {
        private static final ResourceLocation TEX = new ResourceLocation(LunarMiners.MOD_ID, "textures/entity/alien_spitter.png");

        public Spitter(EntityRendererProvider.Context ctx) {
            super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(AlienSpitterEntity e) {
            return TEX;
        }
    }

    /** Rainha: 2,6x maior que um alienígena comum. */
    public static class Queen extends ZombieRenderer {
        private static final ResourceLocation TEX = new ResourceLocation(LunarMiners.MOD_ID, "textures/entity/alien_queen.png");

        public Queen(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.shadowRadius = 1.6F;
        }

        @Override
        protected void scale(Zombie e, PoseStack pose, float partial) {
            pose.scale(2.6F, 2.6F, 2.6F);
        }

        @Override
        public ResourceLocation getTextureLocation(Zombie e) {
            return TEX;
        }
    }
}

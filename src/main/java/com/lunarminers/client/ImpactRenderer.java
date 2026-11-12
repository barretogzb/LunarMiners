package com.lunarminers.client;

import com.lunarminers.entity.ImpactEntity;
import com.lunarminers.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** Desenha o meteoro (rocha brilhante girando) ou a cápsula de suprimentos caindo. */
public class ImpactRenderer extends EntityRenderer<ImpactEntity> {
    public ImpactRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.8F;
    }

    @Override
    public void render(ImpactEntity e, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int luz) {
        boolean meteoro = e.tipo() == ImpactEntity.METEORO;
        BlockState bloco = (meteoro ? ModBlocks.METEORITE_ORE : ModBlocks.SUPPLY_CRATE).get().defaultBlockState();
        pose.pushPose();
        float escala = meteoro ? 1.6F : 1.3F;
        pose.translate(0, escala / 2, 0);
        if (meteoro) {
            float giro = (e.tickCount + partial) * 12;
            pose.mulPose(Axis.XP.rotationDegrees(giro));
            pose.mulPose(Axis.ZP.rotationDegrees(giro * 0.7F));
        }
        pose.scale(escala, escala, escala);
        pose.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(bloco, pose, buffers,
                meteoro ? LightTexture.FULL_BRIGHT : luz, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(e, yaw, partial, pose, buffers, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(ImpactEntity e) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.RocketEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RocketRenderer extends EntityRenderer<RocketEntity> {
    private static final ResourceLocation TEXTURA =
            new ResourceLocation(LunarMiners.MOD_ID, "textures/entity/rocket.png");
    private final RocketModel modelo;

    public RocketRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.modelo = new RocketModel(ctx.bakeLayer(RocketModel.LAYER));
        this.shadowRadius = 1.4F;
    }

    @Override
    public void render(RocketEntity e, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int luz) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.5F, 0.0F);
        VertexConsumer vc = buffers.getBuffer(modelo.renderType(TEXTURA));
        modelo.renderToBuffer(pose, vc, luz, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        pose.popPose();
        super.render(e, yaw, partial, pose, buffers, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity e) {
        return TEXTURA;
    }
}

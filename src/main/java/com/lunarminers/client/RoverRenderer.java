package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.RoverEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RoverRenderer extends EntityRenderer<RoverEntity> {
    private static final ResourceLocation TEXTURA =
            new ResourceLocation(LunarMiners.MOD_ID, "textures/entity/rover.png");
    private final RoverModel modelo;

    public RoverRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.modelo = new RoverModel(ctx.bakeLayer(RoverModel.LAYER));
        this.shadowRadius = 1.2F;
    }

    @Override
    public void render(RoverEntity rover, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int luz) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.5F, 0.0F);
        modelo.setupAnim(rover, 0, 0, rover.tickCount + partial, 0, 0);
        VertexConsumer vc = buffers.getBuffer(modelo.renderType(TEXTURA));
        modelo.renderToBuffer(pose, vc, luz, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        pose.popPose();
        super.render(rover, yaw, partial, pose, buffers, luz);
    }

    @Override
    public ResourceLocation getTextureLocation(RoverEntity rover) {
        return TEXTURA;
    }
}

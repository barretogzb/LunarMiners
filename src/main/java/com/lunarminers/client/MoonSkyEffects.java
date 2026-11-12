package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Céu da Lua: espaço preto com estrelas o tempo todo, o Sol passando e a Terra parada no céu.
 * Sem nuvens, sem neblina azul e sem cores de pôr do sol.
 */
public class MoonSkyEffects extends DimensionSpecialEffects {
    private static final ResourceLocation STARS =
            new ResourceLocation(LunarMiners.MOD_ID, "textures/environment/stars.png");
    private static final ResourceLocation EARTH =
            new ResourceLocation(LunarMiners.MOD_ID, "textures/environment/earth.png");
    private static final ResourceLocation SUN =
            new ResourceLocation("minecraft", "textures/environment/sun.png");

    public MoonSkyEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return new Vec3(0.02D, 0.02D, 0.035D);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                             Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        // 1) Caixa de estrelas
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, STARS);
        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            switch (face) {
                case 1 -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                case 2 -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                case 3 -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                case 4 -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                case 5 -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
                default -> { }
            }
            Matrix4f m = poseStack.last().pose();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(m, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
            buffer.vertex(m, -100.0F, -100.0F, 100.0F).uv(0.0F, 2.0F).color(255, 255, 255, 255).endVertex();
            buffer.vertex(m, 100.0F, -100.0F, 100.0F).uv(2.0F, 2.0F).color(255, 255, 255, 255).endVertex();
            buffer.vertex(m, 100.0F, -100.0F, -100.0F).uv(2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            poseStack.popPose();
        }

        // 2) A Terra, fixa no céu
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EARTH);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(150.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-55.0F));
        drawQuad(buffer, poseStack.last().pose(), 14.0F);
        poseStack.popPose();

        // 3) O Sol, seguindo o horário do dia
        RenderSystem.blendFuncSeparate(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderTexture(0, SUN);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTick) * 360.0F));
        drawQuad(buffer, poseStack.last().pose(), 26.0F);
        poseStack.popPose();

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        return true;
    }

    private static void drawQuad(BufferBuilder buffer, Matrix4f m, float size) {
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(m, -size, 100.0F, -size).uv(0.0F, 0.0F).endVertex();
        buffer.vertex(m, size, 100.0F, -size).uv(1.0F, 0.0F).endVertex();
        buffer.vertex(m, size, 100.0F, size).uv(1.0F, 1.0F).endVertex();
        buffer.vertex(m, -size, 100.0F, size).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
}

package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.RocketEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Foguete de ~6,5 blocos de altura: corpo octogonal, cápsula com janela, ogiva, 4 aletas e tubeira. */
public class RocketModel extends EntityModel<RocketEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(LunarMiners.MOD_ID, "rocket"), "main");
    private final ModelPart raiz;

    public RocketModel(ModelPart raiz) {
        this.raiz = raiz;
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("corpo", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-10, -56, -6, 20, 76, 12)
                .texOffs(64, 0).addBox(-6, -56, -10, 12, 76, 20)
                .texOffs(0, 96).addBox(-8.5F, -56, -8.5F, 17, 76, 17), PartPose.ZERO);
        r.addOrReplaceChild("ogiva", CubeListBuilder.create()
                .texOffs(128, 0).addBox(-7, -66, -7, 14, 10, 14)
                .texOffs(128, 24).addBox(-4, -74, -4, 8, 8, 8)
                .texOffs(128, 40).addBox(-1.5F, -80, -1.5F, 3, 6, 3), PartPose.ZERO);
        r.addOrReplaceChild("aletas", CubeListBuilder.create()
                .texOffs(128, 60).addBox(-1, 0, 10, 2, 22, 10)
                .texOffs(128, 60).addBox(-1, 0, -20, 2, 22, 10)
                .texOffs(160, 60).addBox(10, 0, -1, 10, 22, 2)
                .texOffs(160, 60).addBox(-20, 0, -1, 10, 22, 2), PartPose.ZERO);
        r.addOrReplaceChild("tubeira", CubeListBuilder.create()
                .texOffs(128, 100).addBox(-7, 20, -7, 14, 4, 14), PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(RocketEntity e, float a, float b, float c, float d, float f) {
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int luz, int overlay,
                               float r, float g, float b, float a) {
        raiz.render(pose, buffer, luz, overlay, r, g, b, a);
    }
}

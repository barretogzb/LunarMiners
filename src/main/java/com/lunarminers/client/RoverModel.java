package com.lunarminers.client;

import com.lunarminers.LunarMiners;
import com.lunarminers.entity.RoverEntity;
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

/** Modelo do rover: chassi baixo, cabine com vidro, 6 rodas, teto solar e antena. */
public class RoverModel extends EntityModel<RoverEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(LunarMiners.MOD_ID, "rover"), "main");
    private static final String[] RODAS = {"roda_fe", "roda_fd", "roda_me", "roda_md", "roda_te", "roda_td"};

    private final ModelPart raiz;
    private final ModelPart[] rodas = new ModelPart[6];

    public RoverModel(ModelPart raiz) {
        this.raiz = raiz;
        for (int i = 0; i < 6; i++) {
            rodas[i] = raiz.getChild(RODAS[i]);
        }
    }

    public static LayerDefinition criarCamada() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        // chassi (frente do rover = -Z no modelo)
        r.addOrReplaceChild("chassi", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-11, 14, -17, 22, 4, 34), PartPose.ZERO);
        r.addOrReplaceChild("para_choque", CubeListBuilder.create().texOffs(0, 40)
                .addBox(-10, 15, -19, 20, 3, 2), PartPose.ZERO);
        // cabine
        r.addOrReplaceChild("cabine", CubeListBuilder.create().texOffs(0, 46)
                .addBox(-9, 4, -9, 18, 10, 14), PartPose.ZERO);
        r.addOrReplaceChild("vidro", CubeListBuilder.create().texOffs(64, 46)
                .addBox(-8, 5, -10, 16, 7, 1), PartPose.ZERO);
        // teto solar
        r.addOrReplaceChild("teto_solar", CubeListBuilder.create().texOffs(0, 72)
                .addBox(-10, 2, -11, 20, 2, 26), PartPose.ZERO);
        // compartimento de carga atrás
        r.addOrReplaceChild("carga", CubeListBuilder.create().texOffs(0, 101)
                .addBox(-8, 7, 6, 16, 7, 10), PartPose.ZERO);
        // antena
        r.addOrReplaceChild("antena", CubeListBuilder.create().texOffs(114, 0)
                .addBox(7, -8, 10, 1, 10, 1)
                .texOffs(112, 40).addBox(5.5F, -10, 8.5F, 4, 2, 4), PartPose.ZERO);
        // rodas (giram no eixo X)
        float[] zs = {-12, 0, 12};
        for (int i = 0; i < 3; i++) {
            for (int lado = 0; lado < 2; lado++) {
                float x = lado == 0 ? -13 : 13;
                r.addOrReplaceChild(RODAS[i * 2 + lado], CubeListBuilder.create().texOffs(96, 60)
                        .addBox(-2.5F, -4, -4, 5, 8, 8)
                        .texOffs(96, 76).addBox(-2.5F, -3, -5, 5, 6, 10)
                        .texOffs(96, 92).addBox(-2.5F, -5, -3, 5, 10, 6), PartPose.offset(x, 19, zs[i]));
            }
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(RoverEntity rover, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        float giro = (float) Math.toRadians(rover.giroRodas);
        for (ModelPart roda : rodas) {
            roda.xRot = giro;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int luz, int overlay,
                               float r, float g, float b, float a) {
        raiz.render(pose, buffer, luz, overlay, r, g, b, a);
    }
}

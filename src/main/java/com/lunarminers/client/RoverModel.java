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

/**
 * Rover lunar no estilo do LRV das missões Apollo, em tamanho grande:
 * plataforma larga, 6 rodas altas, dois assentos abertos (o astronauta aparece inteiro),
 * painel de controle, santantônio com teto de painéis solares, bagageiro e antena parabólica.
 * Unidades em pixels (16 = 1 bloco), y para baixo, chão em y=24, frente em -Z.
 */
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
        r.addOrReplaceChild("chassi", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-18, 8, -32, 36, 4, 64), PartPose.ZERO);
        r.addOrReplaceChild("para_choque", CubeListBuilder.create().texOffs(0, 70)
                .addBox(-16, 9, -34, 32, 3, 2), PartPose.ZERO);
        r.addOrReplaceChild("assentos", CubeListBuilder.create()
                .texOffs(0, 80).addBox(-15, 2, -4, 12, 6, 12)
                .texOffs(0, 80).addBox(3, 2, -4, 12, 6, 12)
                .texOffs(50, 80).addBox(-15, -12, 8, 12, 14, 2)
                .texOffs(50, 80).addBox(3, -12, 8, 12, 14, 2), PartPose.ZERO);
        r.addOrReplaceChild("painel", CubeListBuilder.create().texOffs(0, 100)
                .addBox(-8, -8, -20, 16, 16, 4), PartPose.ZERO);
        r.addOrReplaceChild("colunas", CubeListBuilder.create()
                .texOffs(80, 80).addBox(-18, -28, -17, 2, 36, 2)
                .texOffs(80, 80).addBox(16, -28, -17, 2, 36, 2)
                .texOffs(80, 80).addBox(-18, -28, 17, 2, 36, 2)
                .texOffs(80, 80).addBox(16, -28, 17, 2, 36, 2), PartPose.ZERO);
        r.addOrReplaceChild("teto_solar", CubeListBuilder.create().texOffs(0, 124)
                .addBox(-20, -30, -20, 40, 2, 42), PartPose.ZERO);
        r.addOrReplaceChild("bagageiro", CubeListBuilder.create().texOffs(0, 170)
                .addBox(-16, 0, 18, 32, 8, 12), PartPose.ZERO);
        r.addOrReplaceChild("antena", CubeListBuilder.create()
                .texOffs(200, 0).addBox(14, -46, 26, 2, 54, 2)
                .texOffs(204, 60).addBox(9, -48, 21, 12, 1, 12), PartPose.ZERO);
        r.addOrReplaceChild("farois", CubeListBuilder.create()
                .texOffs(210, 80).addBox(-14, 4, -34, 6, 4, 2)
                .texOffs(210, 80).addBox(8, 4, -34, 6, 4, 2), PartPose.ZERO);
        float[] zs = {-24, 0, 24};
        for (int i = 0; i < 3; i++) {
            for (int lado = 0; lado < 2; lado++) {
                float x = lado == 0 ? -22 : 22;
                r.addOrReplaceChild(RODAS[i * 2 + lado], CubeListBuilder.create()
                        .texOffs(100, 190).addBox(-4, -9, -6, 8, 18, 12)
                        .texOffs(140, 190).addBox(-4, -6, -9, 8, 12, 18)
                        .texOffs(192, 190).addBox(-4, -8, -8, 8, 16, 16), PartPose.offset(x, 15, zs[i]));
            }
        }
        return LayerDefinition.create(mesh, 256, 256);
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

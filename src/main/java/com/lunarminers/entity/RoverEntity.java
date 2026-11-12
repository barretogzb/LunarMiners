package com.lunarminers.entity;

import com.lunarminers.registry.ModItems;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

/**
 * Rover lunar: dois lugares, sobe degraus de 1 bloco, bateria com teto solar
 * (carrega de dia, gasta andando) e compartimento de carga com 27 espaços.
 * Agache + clique abre a carga; clique normal entra.
 */
public class RoverEntity extends Entity {
    private static final EntityDataAccessor<Integer> CARGA = SynchedEntityData.defineId(RoverEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DANO = SynchedEntityData.defineId(RoverEntity.class, EntityDataSerializers.FLOAT);
    public static final int CARGA_MAX = 24_000;
    private static final double VEL_MAX = 0.5D;

    private final SimpleContainer carga = new SimpleContainer(27);
    private float velocidade;
    private Vec3 posAnterior = Vec3.ZERO;

    private int lerpPassos;
    private double lerpX, lerpY, lerpZ, lerpYRot;

    public float giroRodas;

    public RoverEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.setMaxUpStep(1.25F);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CARGA, CARGA_MAX);
        entityData.define(DANO, 0.0F);
    }

    public int carga() {
        return entityData.get(CARGA);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().size() < 2;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof LivingEntity l ? l : null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.1D;
    }

    @Override
    protected void positionRider(Entity passageiro, Entity.MoveFunction mover) {
        if (!hasPassenger(passageiro)) {
            return;
        }
        int i = getPassengers().indexOf(passageiro);
        double lado = i == 0 ? -0.56D : 0.56D;
        double rad = Math.toRadians(getYRot());
        double ox = Math.cos(rad) * lado;
        double oz = Math.sin(rad) * lado;
        double y = getY() + getPassengersRidingOffset() + passageiro.getMyRidingOffset();
        mover.accept(passageiro, getX() + ox, y, getZ() + oz);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            if (!level().isClientSide) {
                player.openMenu(new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, carga),
                        Component.translatable("entity.lunarminers.rover")));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide && canAddPassenger(player)) {
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source) || level().isClientSide || isRemoved()) {
            return false;
        }
        float dano = entityData.get(DANO) + amount * 4;
        entityData.set(DANO, dano);
        markHurt();
        boolean criativo = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
        if (criativo || dano > 60) {
            if (!criativo) {
                spawnAtLocation(new ItemStack(ModItems.ROVER_KIT.get()));
            }
            Containers.dropContents(level(), blockPosition(), carga);
            discard();
        }
        return true;
    }

    @Override
    public net.minecraft.world.phys.AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(0.6D, 2.4D, 0.8D).move(0, 1.2D, 0);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.ROVER_KIT.get());
    }

    @Override
    public void tick() {
        super.tick();
        if (entityData.get(DANO) > 0) {
            entityData.set(DANO, Math.max(0, entityData.get(DANO) - 0.2F));
        }

        if (isControlledByLocalInstance()) {
            lerpPassos = 0;
            dirigir();
            move(MoverType.SELF, getDeltaMovement());
        } else {
            tickLerp();
        }

        if (!level().isClientSide) {
            energiaServidor();
        }
        giroRodas += (float) (getDeltaMovement().horizontalDistance() * 40);
    }

    private void dirigir() {
        LivingEntity motorista = getControllingPassenger();
        float frente = motorista != null ? motorista.zza : 0;
        float lado = motorista != null ? motorista.xxa : 0;
        boolean semCarga = carga() <= 0;
        double max = semCarga ? VEL_MAX * 0.25 : VEL_MAX;

        if (frente > 0) {
            velocidade = (float) Math.min(max, velocidade + 0.03);
        } else if (frente < 0) {
            velocidade = (float) Math.max(-max * 0.5, velocidade - 0.04);
        } else {
            velocidade *= 0.85F;
        }
        if (Math.abs(velocidade) > 0.01F && lado != 0) {
            setYRot(getYRot() - lado * 4.0F * Math.signum(velocidade));
        }
        if (motorista != null) {
            motorista.setYBodyRot(getYRot());
        }
        double rad = Math.toRadians(getYRot());
        double vx = -Math.sin(rad) * velocidade;
        double vz = Math.cos(rad) * velocidade;
        double vy = getDeltaMovement().y - 0.035D;
        if (onGround() && vy < 0) {
            vy = -0.04D;
        }
        setDeltaMovement(vx, Math.max(vy, -1.5D), vz);
    }

    private void energiaServidor() {
        Vec3 agora = position();
        double andou = agora.distanceTo(posAnterior);
        posAnterior = agora;
        int c = carga();
        if (andou > 0.02 && getControllingPassenger() != null) {
            c -= 2;
        }
        if (level().isDay() && level().getBrightness(LightLayer.SKY, blockPosition().above(2)) >= 14) {
            c += 3;
        }
        c = Mth.clamp(c, 0, CARGA_MAX);
        if (c != carga()) {
            entityData.set(CARGA, c);
        }
        if (tickCount % 20 == 0 && getControllingPassenger() instanceof ServerPlayer sp) {
            int pct = c * 100 / CARGA_MAX;
            sp.displayClientMessage(Component.translatable("message.lunarminers.rover_battery", pct)
                    .withStyle(pct < 15 ? ChatFormatting.RED : ChatFormatting.AQUA), true);
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int passos, boolean teleporte) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpPassos = 10;
    }

    private void tickLerp() {
        if (lerpPassos > 0) {
            double nx = getX() + (lerpX - getX()) / lerpPassos;
            double ny = getY() + (lerpY - getY()) / lerpPassos;
            double nz = getZ() + (lerpZ - getZ()) / lerpPassos;
            double dr = Mth.wrapDegrees(lerpYRot - getYRot());
            setYRot(getYRot() + (float) dr / lerpPassos);
            lerpPassos--;
            setPos(nx, ny, nz);
            setRot(getYRot(), getXRot());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(CARGA, tag.getInt("carga"));
        carga.fromTag(tag.getList("itens", 10));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("carga", carga());
        tag.put("itens", carga.createTag());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

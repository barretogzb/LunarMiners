package com.lunarminers.entity;

import com.lunarminers.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * Rainha Alienígena: chefe da colmeia. Barra de vida no topo da tela,
 * cospe ácido de longe, invoca filhotes a cada poucos segundos e
 * fica mais agressiva com menos de metade da vida. Deixa o Núcleo da Rainha.
 */
public class AlienQueenEntity extends Zombie {
    private final ServerBossEvent barra = new ServerBossEvent(Component.translatable("entity.lunarminers.alien_queen"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    public AlienQueenEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 150;
    }

    public static AttributeSupplier.Builder createQueenAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 320.0D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        barra.setProgress(getHealth() / getMaxHealth());
        LivingEntity alvo = getTarget();
        if (alvo == null || !alvo.isAlive()) {
            return;
        }
        boolean furiosa = getHealth() < getMaxHealth() / 2;
        if (tickCount % (furiosa ? 30 : 50) == 0 && distanceToSqr(alvo) > 16) {
            AlienSpitterEntity.cuspir(this, alvo);
        }
        if (tickCount % (furiosa ? 140 : 220) == 0) {
            for (int i = 0; i < (furiosa ? 3 : 2); i++) {
                AlienEntity filhote = ModEntities.ALIEN.get().create(level());
                if (filhote != null) {
                    filhote.moveTo(getX() + random.nextInt(5) - 2, getY(), getZ() + random.nextInt(5) - 2, random.nextFloat() * 360, 0);
                    filhote.setTarget(alvo);
                    level().addFreshEntity(filhote);
                }
            }
            playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        barra.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        barra.removePlayer(player);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (hasCustomName()) {
            barra.setName(getDisplayName());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }
}

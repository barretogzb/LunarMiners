package com.lunarminers.entity;

import com.lunarminers.registry.ModEntities;
import com.lunarminers.registry.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Bolha de ácido cuspida pelos alienígenas: dano e veneno. */
public class AcidBlobEntity extends ThrowableItemProjectile {

    public AcidBlobEntity(EntityType<? extends AcidBlobEntity> type, Level level) {
        super(type, level);
    }

    public AcidBlobEntity(Level level, LivingEntity dono) {
        super(ModEntities.ACID_BLOB.get(), dono, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ALIEN_GOO.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(damageSources().thrown(this, getOwner()), 6.0F);
        if (hit.getEntity() instanceof LivingEntity alvo) {
            alvo.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide) {
            level().broadcastEntityEvent(this, (byte) 3);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(getDefaultItem())),
                        getX(), getY(), getZ(), (random.nextDouble() - 0.5) * 0.2, 0.1, (random.nextDouble() - 0.5) * 0.2);
            }
        }
    }
}

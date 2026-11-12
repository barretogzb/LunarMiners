package com.lunarminers.entity;

import com.lunarminers.LunarMiners;
import com.lunarminers.registry.ModBlocks;
import com.lunarminers.world.LunarBaseData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Objeto caindo do céu: um METEORO (explode e deixa minério de meteorito)
 * ou uma CÁPSULA de suprimentos (desce freando com retrofoguetes e vira um contêiner).
 */
public class ImpactEntity extends Entity {
    public static final int METEORO = 0;
    public static final int CAPSULA = 1;
    private static final EntityDataAccessor<Integer> TIPO = SynchedEntityData.defineId(ImpactEntity.class, EntityDataSerializers.INT);
    private static final ResourceLocation LOOT_CAPSULA = new ResourceLocation(LunarMiners.MOD_ID, "chests/supply_drop");

    public ImpactEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public ImpactEntity tipo(int tipo) {
        entityData.set(TIPO, tipo);
        return this;
    }

    public int tipo() {
        return entityData.get(TIPO);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TIPO, METEORO);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 v = getDeltaMovement();
        if (tipo() == CAPSULA) {
            v = new Vec3(v.x * 0.9, Math.max(v.y - 0.01, -0.55), v.z * 0.9);
        } else {
            v = v.add(0, -0.03, 0);
        }
        setDeltaMovement(v);
        move(MoverType.SELF, v);

        if (level().isClientSide) {
            for (int i = 0; i < 4; i++) {
                double ox = (random.nextDouble() - 0.5) * 0.8;
                double oz = (random.nextDouble() - 0.5) * 0.8;
                if (tipo() == METEORO) {
                    level().addParticle(ParticleTypes.FLAME, getX() + ox, getY() + 0.5, getZ() + oz, 0, 0.05, 0);
                    level().addParticle(ParticleTypes.LARGE_SMOKE, getX() + ox, getY() + 0.8, getZ() + oz, 0, 0.1, 0);
                } else {
                    level().addParticle(ParticleTypes.FLAME, getX() + ox * 0.5, getY() - 0.2, getZ() + oz * 0.5, 0, -0.3, 0);
                    level().addParticle(ParticleTypes.CLOUD, getX() + ox, getY() - 0.4, getZ() + oz, 0, -0.05, 0);
                }
            }
            return;
        }
        if (onGround() || horizontalCollision || verticalCollision || tickCount > 600) {
            impactar((ServerLevel) level());
        }
    }

    private boolean pertoDaBase(ServerLevel level) {
        LunarBaseData base = LunarBaseData.get(level.getServer().overworld());
        return base.built && base.center.closerThan(blockPosition(), 60);
    }

    private void impactar(ServerLevel level) {
        BlockPos pos = blockPosition();
        if (tipo() == METEORO) {
            boolean base = pertoDaBase(level);
            level.explode(this, getX(), getY(), getZ(), 3.0F,
                    base ? Level.ExplosionInteraction.NONE : Level.ExplosionInteraction.TNT);
            if (!base) {
                BlockState minerio = ModBlocks.METEORITE_ORE.get().defaultBlockState();
                for (int i = 0; i < 4 + random.nextInt(4); i++) {
                    BlockPos p = pos.offset(random.nextInt(3) - 1, -1 - random.nextInt(2), random.nextInt(3) - 1);
                    if (!level.getBlockState(p).isAir()) {
                        level.setBlock(p, minerio, 3);
                    }
                }
            }
        } else {
            BlockPos p = pos;
            while (!level.getBlockState(p).canBeReplaced() && p.getY() < level.getMaxBuildHeight() - 1) {
                p = p.above();
            }
            level.setBlock(p, ModBlocks.SUPPLY_CRATE.get().defaultBlockState(), 3);
            RandomizableContainerBlockEntity.setLootTable(level, random, p, LOOT_CAPSULA);
            level.playSound(null, p, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 0.6F);
            level.sendParticles(ParticleTypes.CLOUD, getX(), getY(), getZ(), 40, 1.5, 0.3, 1.5, 0.05);
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(TIPO, tag.getInt("tipo"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("tipo", tipo());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist < 256 * 256;
    }
}

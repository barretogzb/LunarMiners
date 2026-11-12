package com.lunarminers.entity;

import com.lunarminers.LunarMiners;
import com.lunarminers.registry.ModEntities;
import com.lunarminers.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Foguete brasileiro: abasteça com Combustível de Hélio-3, entre na cápsula e
 * a contagem regressiva começa sozinha. Decola, sai da atmosfera e pousa no outro mundo:
 * da Lua vai para a Terra; da Terra volta para a Lua.
 */
public class RocketEntity extends Entity {
    public static final ResourceKey<Level> TERRA =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(LunarMiners.MOD_ID, "earth"));
    public static final int COMBUSTIVEL_NECESSARIO = 8;
    private static final int CONTAGEM = 200;

    private static final EntityDataAccessor<Integer> COMBUSTIVEL = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CONTADOR = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DECOLANDO = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);
    private float dano;

    public RocketEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(COMBUSTIVEL, 0);
        entityData.define(CONTADOR, -1);
        entityData.define(DECOLANDO, false);
    }

    public int combustivel() {
        return entityData.get(COMBUSTIVEL);
    }

    public boolean decolando() {
        return entityData.get(DECOLANDO);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 3.2D;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(1.0D, 1.5D, 1.0D);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack mao = player.getItemInHand(hand);
        if (mao.is(ModItems.ROCKET_FUEL.get())) {
            if (!level().isClientSide && combustivel() < COMBUSTIVEL_NECESSARIO) {
                entityData.set(COMBUSTIVEL, combustivel() + 1);
                if (!player.getAbilities().instabuild) {
                    mao.shrink(1);
                }
                player.displayClientMessage(Component.translatable("message.lunarminers.rocket_fuel",
                        combustivel(), COMBUSTIVEL_NECESSARIO), true);
                level().playSound(null, blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 1.0F, 0.6F);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide) {
            if (combustivel() < COMBUSTIVEL_NECESSARIO) {
                player.displayClientMessage(Component.translatable("message.lunarminers.rocket_need_fuel",
                        combustivel(), COMBUSTIVEL_NECESSARIO).withStyle(ChatFormatting.YELLOW), true);
            }
            if (canAddPassenger(player)) {
                player.startRiding(this);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved() || decolando() || isInvulnerableTo(source)) {
            return false;
        }
        dano += amount * 4;
        boolean criativo = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
        if (criativo || dano > 80) {
            if (!criativo) {
                spawnAtLocation(new ItemStack(ModItems.ROCKET_KIT.get()));
            }
            discard();
        }
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.ROCKET_KIT.get());
    }

    @Override
    public void tick() {
        super.tick();
        if (dano > 0) {
            dano = Math.max(0, dano - 0.2F);
        }
        Vec3 v = getDeltaMovement();
        if (decolando()) {
            v = new Vec3(0, Math.min(v.y + 0.04, 1.6), 0);
            setDeltaMovement(v);
            setPos(getX(), getY() + v.y, getZ());
        } else {
            v = new Vec3(0, onGround() ? 0 : Math.max(v.y - 0.04, -1.0), 0);
            setDeltaMovement(v);
            move(MoverType.SELF, v);
        }

        if (level().isClientSide) {
            particulas();
            return;
        }
        ServerLevel server = (ServerLevel) level();
        int contador = entityData.get(CONTADOR);
        Entity passageiro = getFirstPassenger();

        if (!decolando() && contador < 0 && passageiro instanceof ServerPlayer && combustivel() >= COMBUSTIVEL_NECESSARIO) {
            entityData.set(CONTADOR, CONTAGEM);
            avisar(Component.translatable("message.lunarminers.rocket_countdown_start").withStyle(ChatFormatting.GOLD));
        } else if (contador >= 0 && !decolando()) {
            if (passageiro == null) {
                entityData.set(CONTADOR, -1);
                return;
            }
            if (contador % 20 == 0 && contador > 0) {
                avisar(Component.translatable("message.lunarminers.rocket_t", contador / 20).withStyle(ChatFormatting.YELLOW));
                server.playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.NEUTRAL, 1.5F, 0.5F + (CONTAGEM - contador) / 400F);
            }
            if (contador == 0) {
                entityData.set(DECOLANDO, true);
                entityData.set(COMBUSTIVEL, 0);
                avisar(Component.translatable("message.lunarminers.rocket_liftoff").withStyle(ChatFormatting.RED));
                server.playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 4.0F, 0.4F);
                server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 2.0F, 0.5F);
            }
            entityData.set(CONTADOR, contador - 1);
        }
        if (decolando() && tickCount % 10 == 0) {
            server.playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 2.5F, 0.3F);
        }
        if (decolando() && getY() > level().getMaxBuildHeight() + 30) {
            viajar(server);
        }
    }

    private void avisar(Component msg) {
        for (Entity e : getPassengers()) {
            if (e instanceof ServerPlayer p) {
                p.displayClientMessage(msg, true);
            }
        }
    }

    private void particulas() {
        int contador = entityData.get(CONTADOR);
        if (decolando()) {
            for (int i = 0; i < 10; i++) {
                double ox = (random.nextDouble() - 0.5) * 1.2;
                double oz = (random.nextDouble() - 0.5) * 1.2;
                level().addParticle(ParticleTypes.FLAME, getX() + ox, getY() - 0.2, getZ() + oz, ox * 0.2, -0.6, oz * 0.2);
                level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX() + ox, getY() - 0.5, getZ() + oz, 0, -0.1, 0);
            }
        } else if (contador >= 0) {
            for (int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.CLOUD, getX() + (random.nextDouble() - 0.5) * 2, getY() + 0.2,
                        getZ() + (random.nextDouble() - 0.5) * 2, 0, 0.05, 0);
            }
        }
    }

    private void viajar(ServerLevel origem) {
        boolean naLua = origem.dimension() == Level.OVERWORLD;
        ServerLevel destino = origem.getServer().getLevel(naLua ? TERRA : Level.OVERWORLD);
        if (destino == null) {
            destino = origem.getServer().overworld();
        }
        int x = getBlockX();
        int z = getBlockZ();
        destino.getChunkAt(new BlockPos(x, 0, z));
        int y = destino.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        List<Entity> passageiros = new ArrayList<>(getPassengers());
        for (Entity e : passageiros) {
            e.stopRiding();
            if (e instanceof ServerPlayer p) {
                p.teleportTo(destino, x + 2.5, y + 1, z + 0.5, p.getYRot(), 0);
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
                p.sendSystemMessage(Component.translatable(naLua ? "message.lunarminers.rocket_arrived_earth"
                        : "message.lunarminers.rocket_arrived_moon").withStyle(ChatFormatting.AQUA));
            }
        }
        RocketEntity novo = ModEntities.ROCKET.get().create(destino);
        if (novo != null) {
            novo.moveTo(x + 0.5, y, z + 0.5, getYRot(), 0);
            destino.addFreshEntity(novo);
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(COMBUSTIVEL, tag.getInt("combustivel"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("combustivel", combustivel());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

package com.lunarminers.item;

import com.lunarminers.entity.RoverEntity;
import com.lunarminers.registry.ModEntities;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Kit do Rover: clique no chão para montar o veículo virado para onde você olha. */
public class RoverKitItem extends Item {
    public RoverKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
            RoverEntity rover = ModEntities.ROVER.get().create(level);
            if (rover == null) {
                return InteractionResult.FAIL;
            }
            float yaw = ctx.getPlayer() != null ? ctx.getPlayer().getYRot() : 0;
            rover.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
            if (!level.noCollision(rover, rover.getBoundingBox())) {
                return InteractionResult.FAIL;
            }
            level.addFreshEntity(rover);
            if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lunarminers.rover_kit").withStyle(ChatFormatting.GRAY));
    }
}

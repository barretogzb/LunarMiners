package com.lunarminers.item;

import com.lunarminers.entity.RocketEntity;
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

/** Kit do Foguete: clique no chão (de preferência numa plataforma de pouso) para montar o foguete. */
public class RocketKitItem extends Item {
    public RocketKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
            RocketEntity foguete = ModEntities.ROCKET.get().create(level);
            if (foguete == null) {
                return InteractionResult.FAIL;
            }
            foguete.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
            if (!level.noCollision(foguete, foguete.getBoundingBox())) {
                if (ctx.getPlayer() != null) {
                    ctx.getPlayer().displayClientMessage(Component.translatable("message.lunarminers.rocket_no_space"), true);
                }
                return InteractionResult.FAIL;
            }
            level.addFreshEntity(foguete);
            if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lunarminers.rocket_kit", RocketEntity.COMBUSTIVEL_NECESSARIO)
                .withStyle(ChatFormatting.GRAY));
    }
}

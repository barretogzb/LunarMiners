package com.lunarminers.item;

import com.lunarminers.world.Bandeiras;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Kit de bandeira: clique no chão para fincar o mastro com a bandeira 3x2 virada para você. */
public class FlagKitItem extends Item {
    private static final int MASTRO = 5;
    private final Bandeiras pais;

    public FlagKitItem(Bandeiras pais, Properties properties) {
        super(properties);
        this.pais = pais;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos base = ctx.getClickedPos().relative(ctx.getClickedFace());
        Direction frente = ctx.getHorizontalDirection().getOpposite();
        Direction direita = frente.getCounterClockWise();

        for (int h = 0; h <= MASTRO; h++) {
            if (!level.getBlockState(base.above(h)).canBeReplaced()) {
                return fail(ctx);
            }
        }
        for (int linha = 0; linha < 2; linha++) {
            for (int coluna = 1; coluna <= 3; coluna++) {
                if (!level.getBlockState(base.above(MASTRO - 1 - linha).relative(direita, coluna)).canBeReplaced()) {
                    return fail(ctx);
                }
            }
        }
        if (!level.isClientSide) {
            pais.fincar((p, s) -> level.setBlock(p, s, 3), base, frente, MASTRO);
            Player player = ctx.getPlayer();
            if (player == null || !player.getAbilities().instabuild) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult fail(UseOnContext ctx) {
        if (ctx.getPlayer() != null && !ctx.getLevel().isClientSide) {
            ctx.getPlayer().displayClientMessage(Component.translatable("message.lunarminers.flag_no_space"), true);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lunarminers.flag_kit").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}

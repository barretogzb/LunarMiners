package com.lunarminers.item;

import com.lunarminers.registry.ModBlocks;
import com.lunarminers.registry.ModItems;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Scanner de Minérios: varre 24 blocos em volta e mostra, para cada minério,
 * quantos achou, a distância e a direção do mais perto. Gasta bateria (repare com silício na bigorna).
 */
public class ScannerItem extends Item {
    private static final int RAIO = 24;

    public ScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        Map<Block, String> alvos = new LinkedHashMap<>();
        alvos.put(ModBlocks.MOON_HELIUM3_ORE.get(), "moon_helium3_ore");
        alvos.put(ModBlocks.METEORITE_ORE.get(), "meteorite_ore");
        alvos.put(ModBlocks.MOON_TITANIUM_ORE.get(), "moon_titanium_ore");
        alvos.put(ModBlocks.MOON_NIOBIUM_ORE.get(), "moon_niobium_ore");
        alvos.put(ModBlocks.MOON_SILICON_ORE.get(), "moon_silicon_ore");
        alvos.put(ModBlocks.MOON_ALUMINUM_ORE.get(), "moon_aluminum_ore");
        alvos.put(ModBlocks.MOON_IRON_ORE.get(), "moon_iron_ore");
        alvos.put(ModBlocks.LUNAR_ICE.get(), "lunar_ice");

        Map<Block, Integer> contagem = new LinkedHashMap<>();
        Map<Block, BlockPos> maisPerto = new LinkedHashMap<>();
        BlockPos centro = player.blockPosition();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -RAIO; dx <= RAIO; dx++) {
            for (int dy = -RAIO; dy <= RAIO; dy++) {
                for (int dz = -RAIO; dz <= RAIO; dz++) {
                    p.set(centro.getX() + dx, centro.getY() + dy, centro.getZ() + dz);
                    Block b = level.getBlockState(p).getBlock();
                    if (!alvos.containsKey(b)) {
                        continue;
                    }
                    contagem.merge(b, 1, Integer::sum);
                    BlockPos atual = maisPerto.get(b);
                    if (atual == null || atual.distSqr(centro) > p.distSqr(centro)) {
                        maisPerto.put(b, p.immutable());
                    }
                }
            }
        }

        player.sendSystemMessage(Component.translatable("message.lunarminers.scanner_title", RAIO).withStyle(ChatFormatting.AQUA));
        if (contagem.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.lunarminers.scanner_none").withStyle(ChatFormatting.GRAY));
        }
        for (Map.Entry<Block, String> e : alvos.entrySet()) {
            Integer n = contagem.get(e.getKey());
            if (n == null) {
                continue;
            }
            BlockPos alvo = maisPerto.get(e.getKey());
            int dist = (int) Math.sqrt(alvo.distSqr(centro));
            int dy = alvo.getY() - centro.getY();
            player.sendSystemMessage(Component.translatable("message.lunarminers.scanner_line",
                    Component.translatable("block.lunarminers." + e.getValue()), n, dist,
                    Component.translatable("direction.lunarminers." + direcao(alvo.getX() - centro.getX(), alvo.getZ() - centro.getZ())),
                    dy).withStyle(ChatFormatting.WHITE));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7F, 1.8F);
        stack.hurtAndBreak(1, player, pl -> pl.broadcastBreakEvent(hand));
        player.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.consume(stack);
    }

    private static String direcao(int dx, int dz) {
        if (dx == 0 && dz == 0) {
            return "here";
        }
        double ang = Math.toDegrees(Math.atan2(dx, -dz));
        if (ang < 0) {
            ang += 360;
        }
        String[] nomes = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};
        return nomes[(int) Math.round(ang / 45.0) % 8];
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ModItems.SILICON.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lunarminers.scanner").withStyle(ChatFormatting.GRAY));
    }
}

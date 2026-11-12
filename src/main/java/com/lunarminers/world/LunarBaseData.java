package com.lunarminers.world;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Guarda no save se a base já foi construída e quais jogadores já chegaram nela. */
public class LunarBaseData extends SavedData {
    private static final String NAME = "lunarminers_base";

    public boolean built;
    public BlockPos center = BlockPos.ZERO;
    public final Set<UUID> welcomed = new HashSet<>();

    public static LunarBaseData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(LunarBaseData::load, LunarBaseData::new, NAME);
    }

    public static LunarBaseData load(CompoundTag tag) {
        LunarBaseData data = new LunarBaseData();
        data.built = tag.getBoolean("built");
        data.center = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        ListTag list = tag.getList("welcomed", Tag.TAG_INT_ARRAY);
        for (Tag t : list) {
            data.welcomed.add(NbtUtils.loadUUID(t));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("built", built);
        tag.putInt("x", center.getX());
        tag.putInt("y", center.getY());
        tag.putInt("z", center.getZ());
        ListTag list = new ListTag();
        for (UUID id : welcomed) {
            list.add(NbtUtils.createUUID(id));
        }
        tag.put("welcomed", list);
        return tag;
    }
}

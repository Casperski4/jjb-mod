package dev.jjb;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Сохраняемые данные.
 * Сюжет общий на весь сервер (кооператив): глава и прогресс одни на всех.
 * У каждого игрока отдельно хранятся Дух и число выданных наград,
 * чтобы зашедший позже получил пропущенные награды.
 */
public class ModData extends PersistentState {
    public final Map<UUID, Integer> spirit = new HashMap<>();
    public final Map<UUID, Integer> rewarded = new HashMap<>();

    public boolean mapBuilt;
    public boolean storyStarted;
    public int storyChapter;
    public int storyProgress;

    public static ModData get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(ModData::fromNbt, ModData::new, JjbMod.ID);
    }

    public int getSpirit(UUID id) {
        return spirit.getOrDefault(id, Balance.SPIRIT_MAX);
    }

    public void setSpirit(UUID id, int v) {
        spirit.put(id, Math.max(0, Math.min(Balance.SPIRIT_MAX, v)));
        markDirty();
    }

    public int getRewarded(UUID id) {
        return rewarded.getOrDefault(id, 0);
    }

    public void setRewarded(UUID id, int v) {
        rewarded.put(id, v);
        markDirty();
    }

    public void setStory(int chapter, int progress) {
        storyChapter = chapter;
        storyProgress = progress;
        markDirty();
    }

    public void resetStory() {
        storyStarted = false;
        storyChapter = 0;
        storyProgress = 0;
        rewarded.clear();
        markDirty();
    }

    public static ModData fromNbt(NbtCompound nbt) {
        ModData d = new ModData();
        d.mapBuilt = nbt.getBoolean("mapBuilt");
        d.storyStarted = nbt.getBoolean("storyStarted");
        d.storyChapter = nbt.getInt("storyChapter");
        d.storyProgress = nbt.getInt("storyProgress");
        NbtList list = nbt.getList("players", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound c = list.getCompound(i);
            UUID id = c.getUuid("id");
            d.spirit.put(id, c.getInt("spirit"));
            d.rewarded.put(id, c.getInt("rewarded"));
        }
        return d;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("mapBuilt", mapBuilt);
        nbt.putBoolean("storyStarted", storyStarted);
        nbt.putInt("storyChapter", storyChapter);
        nbt.putInt("storyProgress", storyProgress);
        NbtList list = new NbtList();
        for (UUID id : spirit.keySet()) {
            NbtCompound c = new NbtCompound();
            c.putUuid("id", id);
            c.putInt("spirit", spirit.get(id));
            c.putInt("rewarded", rewarded.getOrDefault(id, 0));
            list.add(c);
        }
        for (UUID id : rewarded.keySet()) {
            if (spirit.containsKey(id)) continue;
            NbtCompound c = new NbtCompound();
            c.putUuid("id", id);
            c.putInt("spirit", Balance.SPIRIT_MAX);
            c.putInt("rewarded", rewarded.get(id));
            list.add(c);
        }
        nbt.put("players", list);
        return nbt;
    }
}

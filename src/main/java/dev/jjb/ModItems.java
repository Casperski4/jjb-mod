package dev.jjb;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    private ModItems() {}

    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static void init() {
        for (Abilities.Def def : Abilities.ALL.values()) {
            Item item = new AbilityItem(new FabricItemSettings().maxCount(1), def);
            Registry.register(Registries.ITEM, new Identifier(JjbMod.ID, def.id()), item);
            ITEMS.put(def.id(), item);
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries ->
                ITEMS.values().forEach(i -> entries.add(new ItemStack(i))));
    }

    public static ItemStack stack(String id) {
        Item i = ITEMS.get(id);
        return i == null ? ItemStack.EMPTY : new ItemStack(i);
    }
}

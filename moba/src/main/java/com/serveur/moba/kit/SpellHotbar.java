package com.serveur.moba.kit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public final class SpellHotbar {
    public enum SpellTag {
        Q, W, E, R, PASSIVE
    }

    private final NamespacedKey spellKey;


    public static final Map<Integer, Map.Entry<Material, SpellTag>> SLOTS = Map.of(
            0, Map.entry(Material.FEATHER, SpellTag.Q),
            1, Map.entry(Material.BREEZE_ROD, SpellTag.W),
            2, Map.entry(Material.BLAZE_POWDER, SpellTag.E),
            3, Map.entry(Material.NETHER_STAR, SpellTag.R),
            7, Map.entry(Material.GHAST_TEAR, SpellTag.PASSIVE)
    // 4 = épée du kit (existante)
    // 8 = EMERALD (pas utilisé pour l’instant)
    );

    public SpellHotbar(NamespacedKey spellKey) {
        this.spellKey = spellKey;
    }

    public void applyTo(Player p, Map<SpellTag, String> shortDescriptions) {
        var inv = p.getInventory();

        for (var e : SLOTS.entrySet()) {
            int slot = e.getKey();
            Material mat = e.getValue().getKey();
            SpellTag tag = e.getValue().getValue();

            ItemStack it = new ItemStack(mat);
            ItemMeta meta = it.getItemMeta();

            // Nom + description (affichée quand on survole dans l'inventaire)
            meta.displayName(Component.text("Sort " + tag.name(), NamedTextColor.AQUA));
            var loreText = shortDescriptions.getOrDefault(tag, "No description.");
            meta.lore(List.of(
                    Component.text(loreText, NamedTextColor.GRAY),
                    Component.text("Clique droit pour lancer.", NamedTextColor.DARK_GRAY)));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);

            // Tag PersistentDataContainer pour reconnaître quel sort c’est
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(spellKey, PersistentDataType.STRING, tag.name());

            it.setItemMeta(meta);
            inv.setItem(slot, it);
        }
    }

    public static SpellTag readTag(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta())
            return null;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        String val = pdc.get(key, PersistentDataType.STRING);
        if (val == null)
            return null;
        try {
            return SpellTag.valueOf(val);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

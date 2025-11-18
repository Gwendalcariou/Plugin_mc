package com.serveur.moba.shop;

import com.serveur.moba.MobaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ShopService {

    private final NamespacedKey KEY;
    private final Map<UUID, Set<String>> active = new HashMap<>();

    // Titre de la GUI
    private static final String TITLE = "§aBoutique";

    public ShopService(MobaPlugin plugin) {
        this.KEY = new NamespacedKey(plugin, "moba_shop_item");
    }

    /**
     * Ouvre la boutique pour un joueur
     */
    public void openShop(Player p) {
        // 9 cases pour l’instant, tu ajusteras la taille si besoin (18, 27, etc.)
        Inventory inv = Bukkit.createInventory(p, 9, TITLE);

        for (ShopItem si : ShopItem.values()) {
            inv.addItem(createItemStack(si));
        }

        p.openInventory(inv);
    }

    /**
     * Savoir si un inventaire est la GUI de shop
     */
    public boolean isShopInventory(InventoryView view) {
        return view != null && TITLE.equals(view.getTitle());
    }

    /**
     * Retrouver le ShopItem correspondant à un ItemStack de la GUI
     */
    public ShopItem fromItemStack(ItemStack it) {
        if (it == null)
            return null;
        ItemMeta m = it.getItemMeta();
        if (m == null)
            return null;
        String id = m.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        if (id == null)
            return null;

        for (ShopItem si : ShopItem.values()) {
            if (si.id.equals(id)) {
                return si;
            }
        }
        return null;
    }

    // ======================= EXISTANT =======================

    // Create an ItemStack representing the shop item (price=0 for now)
    public ItemStack createItemStack(ShopItem si) {
        ItemStack it = new ItemStack(si.icon);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName("§e" + si.display);

            m.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, si.id);

            List<String> lore = new ArrayList<>();
            lore.add("§7" + si.description); // description de l'effet
            lore.add(""); // ligne vide
            lore.add("§aPrix: §e0"); // pour l'instant
            lore.add("§8Clic gauche: acheter");
            lore.add("§8Clic droit: vendre");

            m.setLore(lore);

            it.setItemMeta(m);
        }
        return it;
    }

    public void giveBoughtItem(Player p, ShopItem item) {
        active.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>()).add(item.id);

        ItemStack stack = createItemStack(item);
        PlayerInventory inv = p.getInventory();

        boolean placed = false;
        for (int slot = 9; slot <= 35; slot++) {
            ItemStack current = inv.getItem(slot);
            if (current == null || current.getType() == Material.AIR) {
                inv.setItem(slot, stack);
                placed = true;
                break;
            }
        }

        if (!placed) {
            // inventaire plein => fallback, on laisse Bukkit gérer
            inv.addItem(stack);
        }
    }

    // remove item effects and unmark
    public void removeBoughtItem(Player p, ShopItem item) {
        Set<String> s = active.get(p.getUniqueId());
        if (s != null) {
            s.remove(item.id);
            if (s.isEmpty())
                active.remove(p.getUniqueId());
        }
        // remove item stacks from inventory
        for (ItemStack it : p.getInventory().getContents()) {
            if (it == null)
                continue;
            ItemMeta m = it.getItemMeta();
            if (m == null)
                continue;
            String id = m.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
            if (item.id.equals(id)) {
                p.getInventory().remove(it);
            }
        }
    }

    public boolean playerHas(Player p, ShopItem item) {
        Set<String> s = active.get(p.getUniqueId());
        return s != null && s.contains(item.id);
    }

    // helper used by listeners to check persistent id on ItemStack if needed
    public boolean isShopItem(ItemStack it, ShopItem item) {
        if (it == null)
            return false;
        ItemMeta m = it.getItemMeta();
        if (m == null)
            return false;
        String id = m.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        return item.id.equals(id);
    }

    // true si l'item porte la clé de la boutique (peu importe lequel)
    public boolean isShopItem(ItemStack it) {
        if (it == null)
            return false;
        var m = it.getItemMeta();
        if (m == null)
            return false;
        return m.getPersistentDataContainer().has(KEY, PersistentDataType.STRING);
    }

    public Set<String> getActiveIds(UUID playerId) {
        return active.getOrDefault(playerId, Collections.emptySet());
    }
}

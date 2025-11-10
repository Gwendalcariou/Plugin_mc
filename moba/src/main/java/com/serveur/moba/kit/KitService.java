package com.serveur.moba.kit;

import com.serveur.moba.MobaPlugin;
import com.serveur.moba.state.PlayerStateService;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class KitService {

    private final MobaPlugin plugin;

    private final NamespacedKey CLASS_ITEM_KEY;

    public KitService(MobaPlugin plugin) {
        this.plugin = plugin;
        this.CLASS_ITEM_KEY = new NamespacedKey(plugin, "class_item");
    }

    public void applyKit(Player p, PlayerStateService.Role role) {
        if (role == PlayerStateService.Role.NONE) {
            clearKit(p);
            p.updateInventory();
            return;
        }
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        switch (role) {
            case TANK -> giveTank(p);
            case BRUISER -> giveBruiser(p);
            case ADC -> giveAdc(p);
            case NONE -> {
            }
        }

        p.updateInventory();

        plugin.giveClassKit(p, role);
    }

    /* ---------------- Kits temporaires ---------------- */

    private void giveTank(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.setHelmet(lock(enchanted(new ItemStack(Material.DIAMOND_HELMET), Enchantment.PROTECTION, 1)));
        inv.setChestplate(lock(enchanted(new ItemStack(Material.DIAMOND_CHESTPLATE), Enchantment.PROTECTION, 1)));
        inv.setLeggings(lock(enchanted(new ItemStack(Material.DIAMOND_LEGGINGS), Enchantment.PROTECTION, 1)));
        inv.setBoots(lock(enchanted(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.PROTECTION, 1)));
        ItemStack sword = unbreakable(new ItemStack(Material.IRON_SWORD));
        inv.setItem(4, lock(sword));
        fillHotbarCommon(inv, sword);
    }

    private void giveBruiser(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.setHelmet(lock(enchanted(new ItemStack(Material.IRON_HELMET), Enchantment.PROTECTION, 2)));
        inv.setChestplate(lock(enchanted(new ItemStack(Material.IRON_CHESTPLATE), Enchantment.PROTECTION, 2)));
        inv.setLeggings(lock(enchanted(new ItemStack(Material.IRON_LEGGINGS), Enchantment.PROTECTION, 2)));
        inv.setBoots(lock(enchanted(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION, 2)));
        ItemStack sword = unbreakable(new ItemStack(Material.DIAMOND_SWORD));
        inv.setItem(4, lock(sword));
        fillHotbarCommon(inv, sword);
    }

    private void giveAdc(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.setHelmet(lock(unbreakable(new ItemStack(Material.IRON_HELMET))));
        inv.setChestplate(lock(unbreakable(new ItemStack(Material.IRON_CHESTPLATE))));
        inv.setLeggings(lock(unbreakable(new ItemStack(Material.IRON_LEGGINGS))));
        inv.setBoots(lock(unbreakable(new ItemStack(Material.IRON_BOOTS))));
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        sword = unbreakable(sword);
        inv.setItem(4, lock(sword));
        fillHotbarCommon(inv, sword);
    }

    /*
     * --------- Hotbar commune (placeholders remplacés ensuite par giveClassKit)
     * ---------
     */
    private void fillHotbarCommon(PlayerInventory inv, ItemStack sword) {

        inv.setItem(0, lock(new ItemStack(Material.FEATHER)));
        inv.setItem(1, lock(new ItemStack(Material.BREEZE_ROD)));
        inv.setItem(2, lock(new ItemStack(Material.BLAZE_POWDER)));
        inv.setItem(3, lock(new ItemStack(Material.NETHER_STAR)));
        inv.setItem(7, lock(new ItemStack(Material.GHAST_TEAR)));
        inv.setItem(8, lock(new ItemStack(Material.EMERALD)));
        inv.setHeldItemSlot(4);
    }

    private ItemStack enchanted(ItemStack it, Enchantment ench, int level) {
        it.addUnsafeEnchantment(ench, level);
        return unbreakable(it);
    }

    private ItemStack unbreakable(ItemStack it) {
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            it.setItemMeta(meta);
        }
        return it;
    }

    public void clearKit(Player p) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.getInventory().setItemInOffHand(null);
    }

    public ItemStack lock(ItemStack it) {
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(CLASS_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // facultatif
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean isClassItem(ItemStack it) {
        if (it == null)
            return false;
        ItemMeta m = it.getItemMeta();
        if (m == null)
            return false;
        Byte b = m.getPersistentDataContainer().get(CLASS_ITEM_KEY, PersistentDataType.BYTE);
        return b != null && b == (byte) 1;
    }

}

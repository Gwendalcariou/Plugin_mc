package com.serveur.moba.kit;

import com.serveur.moba.MobaPlugin;
import com.serveur.moba.state.PlayerStateService;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public final class KitService {

    private final MobaPlugin plugin;

    public KitService(MobaPlugin plugin) {
        this.plugin = plugin;
    }

    public void applyKit(Player p, PlayerStateService.Role role) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        switch (role) {
            case TANK -> giveTank(p);
            case BRUISER -> giveBruiser(p);
            case ADC -> giveAdc(p);
        }

        p.updateInventory();

        plugin.giveClassKit(p, role);
    }

    /* ---------------- Kits temporaires ---------------- */

    private void giveTank(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.setHelmet(enchanted(new ItemStack(Material.DIAMOND_HELMET), Enchantment.PROTECTION, 1));
        inv.setChestplate(enchanted(new ItemStack(Material.DIAMOND_CHESTPLATE), Enchantment.PROTECTION, 1));
        inv.setLeggings(enchanted(new ItemStack(Material.DIAMOND_LEGGINGS), Enchantment.PROTECTION, 1));
        inv.setBoots(enchanted(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.PROTECTION, 1));
        ItemStack sword = unbreakable(new ItemStack(Material.IRON_SWORD));
        inv.setItem(4, sword);
        fillHotbarCommon(inv, sword);
    }

    private void giveBruiser(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.setHelmet(enchanted(new ItemStack(Material.IRON_HELMET), Enchantment.PROTECTION, 2));
        inv.setChestplate(enchanted(new ItemStack(Material.IRON_CHESTPLATE), Enchantment.PROTECTION, 2));
        inv.setLeggings(enchanted(new ItemStack(Material.IRON_LEGGINGS), Enchantment.PROTECTION, 2));
        inv.setBoots(enchanted(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION, 2));
        ItemStack sword = unbreakable(new ItemStack(Material.DIAMOND_SWORD));
        inv.setItem(4, sword);
        fillHotbarCommon(inv, sword);
    }

    private void giveAdc(Player p) {
        PlayerInventory inv = p.getInventory();
        inv.setHelmet(unbreakable(new ItemStack(Material.IRON_HELMET)));
        inv.setChestplate(unbreakable(new ItemStack(Material.IRON_CHESTPLATE)));
        inv.setLeggings(unbreakable(new ItemStack(Material.IRON_LEGGINGS)));
        inv.setBoots(unbreakable(new ItemStack(Material.IRON_BOOTS)));
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        sword = unbreakable(sword);
        inv.setItem(4, sword);
        fillHotbarCommon(inv, sword);
    }

    /*
     * --------- Hotbar commune (placeholders remplacés ensuite par giveClassKit)
     * ---------
     */
    private void fillHotbarCommon(PlayerInventory inv, ItemStack sword) {
        inv.setItem(0, new ItemStack(Material.FEATHER));
        inv.setItem(1, new ItemStack(Material.BREEZE_ROD));
        inv.setItem(2, new ItemStack(Material.BLAZE_POWDER));
        inv.setItem(3, new ItemStack(Material.NETHER_STAR));
        inv.setItem(7, new ItemStack(Material.GHAST_TEAR));
        inv.setItem(8, new ItemStack(Material.EMERALD));
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
}

package com.serveur.moba.shop;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public final class ShopItemLockListener implements Listener {

    private final ShopService shop;

    public ShopItemLockListener(ShopService shop) {
        this.shop = shop;
    }

    // ====== Drop ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent e) {
        if (shop.isShopItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    // ====== Swap main/offhand ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (shop.isShopItem(e.getMainHandItem()) || shop.isShopItem(e.getOffHandItem())) {
            e.setCancelled(true);
        }
    }

    // ====== Clics / déplacements (shift-click, drag, hotbar swap...) ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player p))
            return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        // On bloque dès qu'un item de shop est impliqué
        if (shop.isShopItem(current) || shop.isShopItem(cursor)) {
            e.setCancelled(true);
            return;
        }

        // Empêche "collect to cursor" qui fusionnerait avec un item de shop
        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR && shop.isShopItem(cursor)) {
            e.setCancelled(true);
            return;
        }

        // Empêche HOTBAR_SWAP / MOVE_AND_READD si l'un des deux est un item de shop
        if (e.getAction() == InventoryAction.HOTBAR_SWAP
                || e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            int hotbar = e.getHotbarButton(); // 0..8
            if (hotbar >= 0) {
                ItemStack hot = p.getInventory().getItem(hotbar);
                if (shop.isShopItem(hot) || shop.isShopItem(e.getCurrentItem())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player p))
            return;

        // Si on drag un item de shop -> interdit
        if (shop.isShopItem(e.getOldCursor())) {
            e.setCancelled(true);
            return;
        }

        // Si le drag va déposer sur des slots qui contiennent des items de shop ->
        // interdit
        for (int slot : e.getRawSlots()) {
            ItemStack target = e.getView().getItem(slot);
            if (shop.isShopItem(target)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    // ====== Mort : ne pas drop les items de shop ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        e.getDrops().removeIf(shop::isShopItem);
    }

    // ====== Pickup : ne jamais ramasser un item de shop tombé au sol ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        if (shop.isShopItem(e.getItem().getItemStack())) {
            e.setCancelled(true);
        }
    }
}

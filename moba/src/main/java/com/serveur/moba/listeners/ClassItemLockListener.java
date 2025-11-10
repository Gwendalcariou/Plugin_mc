package com.serveur.moba.listeners;

import com.serveur.moba.kit.KitService;
import com.serveur.moba.state.PlayerStateService;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public final class ClassItemLockListener implements Listener {

    private final PlayerStateService state;
    private final KitService kit;

    public ClassItemLockListener(PlayerStateService state, KitService kit) {
        this.state = state;
        this.kit = kit;
    }

    private boolean inClass(Player p) {
        return state.getRole(p) != PlayerStateService.Role.NONE;
    }

    // ====== Drop ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!inClass(p))
            return;
        if (kit.isClassItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    // ====== Swap main/offhand ======
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!inClass(p))
            return;
        if (kit.isClassItem(e.getMainHandItem()) || kit.isClassItem(e.getOffHandItem())) {
            e.setCancelled(true);
        }
    }

    // ====== Clics / déplacements (shift-click, drag, etc.) ======
    @SuppressWarnings("removal")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player p))
            return;
        if (!inClass(p))
            return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        if (kit.isClassItem(current) || kit.isClassItem(cursor)) {
            e.setCancelled(true);
            return;
        }

        // Empêche (dés)équipement d’armure par glisser/déposer
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            e.setCancelled(true);
        }

        // Empêche "collect to cursor" sur item de classe
        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR && kit.isClassItem(cursor)) {
            e.setCancelled(true);

        }

        if (e.getAction() == InventoryAction.HOTBAR_SWAP || e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            int hotbar = e.getHotbarButton(); // 0..8 ou -1
            if (hotbar >= 0) {
                ItemStack hot = p.getInventory().getItem(hotbar);
                if (kit.isClassItem(hot) || kit.isClassItem(e.getCurrentItem())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player p))
            return;
        if (!inClass(p))
            return;

        // Si on drag un item de classe -> interdit
        if (kit.isClassItem(e.getOldCursor())) {
            e.setCancelled(true);
            return;
        }

        // Si le drag va déposer sur des slots qui contiennent des items de classe ->
        // interdit
        for (int slot : e.getRawSlots()) {
            ItemStack target = e.getView().getItem(slot);
            if (kit.isClassItem(target)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        var p = e.getEntity();
        if (state.getRole(p) == PlayerStateService.Role.NONE)
            return;

        // Retire des drops tout ce qui est item de classe
        e.getDrops().removeIf(kit::isClassItem);

        // Tu fais déjà disableCurrent(p); si tu veux tout nettoyer ici :
        // classService.clearClass(p); // (si tu veux reset complet à la mort)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        if (kit.isClassItem(e.getItem().getItemStack())) {
            e.setCancelled(true);
        }
    }

}

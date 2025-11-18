package com.serveur.moba.shop;

import com.serveur.moba.kit.KitService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ShopOpenListener implements Listener {

    private final ShopService shop;
    private final KitService kits;

    public ShopOpenListener(ShopService shop, KitService kits) {
        this.shop = shop;
        this.kits = kits;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        // pour éviter le double-trigger main/offhand
        if (e.getHand() != EquipmentSlot.HAND)
            return;

        ItemStack it = e.getItem();
        if (it == null || it.getType() != Material.EMERALD)
            return;

        // On vérifie que c'est bien une émeraude "de classe"
        if (!kits.isClassItem(it))
            return;

        Player p = e.getPlayer();
        e.setCancelled(true); // on évite le comportement vanilla
        shop.openShop(p);
    }
}

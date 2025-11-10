package com.serveur.moba.listeners;

import com.serveur.moba.ability.*;
import com.serveur.moba.kit.SpellHotbar;
import com.serveur.moba.state.PlayerStateService;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HotbarSpellListener implements Listener {

    private final Plugin plugin;
    private final AbilityRegistry abilities;
    private final PlayerStateService states;
    private final NamespacedKey spellKey;

    private final int returnSwordSlot = 4;
    private final long autoCastDelayTicks = 15L; // 0.75s
    private final Map<UUID, BukkitTask> pendingAutoCast = new HashMap<>();
    private final Map<SpellHotbar.SpellTag, AbilityKey> map = new EnumMap<>(SpellHotbar.SpellTag.class);

    public HotbarSpellListener(Plugin plugin,
            AbilityRegistry abilities,
            PlayerStateService states,
            NamespacedKey spellKey) {
        this.plugin = plugin;
        this.abilities = abilities;
        this.states = states;
        this.spellKey = spellKey;

        map.put(SpellHotbar.SpellTag.Q, AbilityKey.Q);
        map.put(SpellHotbar.SpellTag.W, AbilityKey.W);
        map.put(SpellHotbar.SpellTag.E, AbilityKey.E);
        map.put(SpellHotbar.SpellTag.R, AbilityKey.R);
        // pas de PASSIVE => pas d’action sur la larme
    }

    // ---------- Helper central : récupère l'ability et la lance ----------
    private boolean cast(Player p, AbilityKey key) {
        var st = states.get(p.getUniqueId());
        if (st == null)
            return false;

        var role = st.role;
        var ability = abilities.get(role, key);
        if (ability == null) {
            p.sendMessage("§cAucune compétence liée à " + key);
            return false;
        }

        return ability.cast(new AbilityContext(plugin, p));
    }

    // ---------- Interactions ----------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND)
            return;

        switch (e.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK, LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                var p = e.getPlayer();
                var item = p.getInventory().getItemInMainHand();
                var tag = SpellHotbar.readTag(item, spellKey);
                if (tag == null)
                    return;

                cancelAutoCast(p);

                var key = map.get(tag);
                if (key == null)
                    return;

                if (cast(p, key)) {
                    e.setCancelled(true); // empêche l’action par défaut (ouvrir coffre, etc.)
                    Bukkit.getScheduler().runTask(plugin, () -> p.getInventory().setHeldItemSlot(returnSwordSlot));
                }
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onHeldChange(PlayerItemHeldEvent e) {
        var p = e.getPlayer();
        cancelAutoCast(p);

        int targetSlot = e.getNewSlot();
        var newItem = p.getInventory().getItem(targetSlot);
        var tag = SpellHotbar.readTag(newItem, spellKey);
        if (tag == null)
            return;

        var key = map.get(tag);
        if (key == null)
            return;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.getInventory().getHeldItemSlot() == targetSlot) {
                if (cast(p, key)) {
                    p.getInventory().setHeldItemSlot(returnSwordSlot);
                }
            }
            pendingAutoCast.remove(p.getUniqueId());
        }, autoCastDelayTicks);

        pendingAutoCast.put(p.getUniqueId(), task);
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player p)
            cancelAutoCast(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cancelAutoCast(e.getPlayer());
    }

    private void cancelAutoCast(Player p) {
        var task = pendingAutoCast.remove(p.getUniqueId());
        if (task != null)
            task.cancel();
    }
}

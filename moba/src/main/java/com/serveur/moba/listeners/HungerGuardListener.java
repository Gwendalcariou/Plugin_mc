package com.serveur.moba.listeners;

import com.serveur.moba.state.PlayerStateService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.UUID;

public final class HungerGuardListener implements Listener {

    private final PlayerStateService state;

    public HungerGuardListener(PlayerStateService state) {
        this.state = state;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        UUID id = p.getUniqueId();
        var role = state.get(id).role;
        if (role == PlayerStateService.Role.NONE)
            return; // hors jeu : on ne force rien

        // Verrouille la faim à 20 et supprime la saturation pour garder la regen
        // "normale"
        e.setFoodLevel(20);
        p.setSaturation(0f);
        p.setExhaustion(0f);

        // On peut annuler pour empêcher les variations dues à l’exhaustion/food
        e.setCancelled(true);
    }
}

package com.serveur.moba.classes;

import com.serveur.moba.classes.adc.AdcPassiveListener;
import com.serveur.moba.state.PlayerStateService;
import java.util.Optional;
import java.util.Locale;

import org.bukkit.entity.Player;

public final class ClassService {
    private final PlayerStateService state;
    private final AdcPassiveListener adcListener; // pour reset compteur

    public ClassService(PlayerStateService state, AdcPassiveListener adcListener) {
        this.state = state;
        this.adcListener = adcListener;
    }

    public Optional<PlayerStateService.Role> parseRole(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "tank" -> Optional.of(PlayerStateService.Role.TANK);
            case "bruiser" -> Optional.of(PlayerStateService.Role.BRUISER);
            case "adc" -> Optional.of(PlayerStateService.Role.ADC);
            default -> Optional.empty();
        };
    }

    public boolean setClass(Player p, PlayerStateService.Role newRole) {
        var st = state.get(p.getUniqueId());
        var old = st.role;
        if (old == newRole)
            return true;

        // --- CLEANUP de l'ancienne classe ---
        switch (old) {
            case BRUISER -> p.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
            case ADC -> adcListener.reset(p.getUniqueId());
            case TANK -> {
                /* pas d'effet persistant à enlever ici */ }
        }

        // set l'état
        state.setRole(p, newRole);

        // --- INIT de la nouvelle classe (si tu veux appliquer quelque chose
        // immédiatement) ---
        if (newRole == PlayerStateService.Role.BRUISER) {
            // optionnel: rien, le listener se chargera d’appliquer le SPEED sur move hors
            // combat
        }
        if (newRole == PlayerStateService.Role.ADC) {
            adcListener.reset(p.getUniqueId()); // démarre propre
        }
        return true;
    }

    public void disableCurrent(Player p) {
        // Appelé sur quit/death: enlever effets volatiles
        switch (state.get(p.getUniqueId()).role) {
            case BRUISER -> p.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
            case ADC -> adcListener.reset(p.getUniqueId());
            case TANK -> {
            }
        }
    }
}

package com.serveur.moba.util;

import org.bukkit.entity.Player;
import org.bukkit.potion.*;

public final class Buffs {
    private Buffs() {
    }

    public static void give(Player p, PotionEffectType type, int amplifier, int seconds) {
        p.addPotionEffect(new PotionEffect(type, seconds * 20, Math.max(0, amplifier - 1), false, false, true));
    }

    /**
     * Donne des coeurs jaunes “comme une pomme dorée”.
     * 
     * @param hearts  nombre de coeurs jaunes souhaités (ex: 4.0 -> équiv.
     *                Absorption II)
     * @param seconds durée en secondes (ex: 120 = 2 minutes comme la golden apple)
     */
    public static void absorption(Player p, double hearts, int seconds) {
        // 2 HP = 1 coeur, Absorption I = 2 coeurs, II = 4, III = 6, …
        int level = (int) Math.max(1, Math.ceil(hearts / 2.0)); // 4 coeurs -> level 2
        give(p, PotionEffectType.ABSORPTION, level, seconds);
    }

    /** Optionnel: remet l’absorption à zéro si tu en as besoin pour un “clean”. */
    public static void clearAbsorption(Player p) {
        p.removePotionEffect(PotionEffectType.ABSORPTION);
        p.setAbsorptionAmount(0.0);
    }
}

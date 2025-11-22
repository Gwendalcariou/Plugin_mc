package com.serveur.moba.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Buffs {

    private Buffs() {
    }

    // ======================
    // Gestion potions simple
    // ======================

    public static void give(Player p, PotionEffectType type, int amplifier, int seconds) {
        p.addPotionEffect(new PotionEffect(
                type,
                seconds * 20,
                Math.max(0, amplifier - 1),
                false,
                false,
                true));
    }

    // ==========================
    // Gestion custom absorption
    // ==========================

    private static Plugin plugin;

    // par joueur, par source → capacité max (en HP, pas en cœurs)
    private static final Map<UUID, Map<String, AbsorbEntry>> ABS = new HashMap<>();

    private static final class AbsorbEntry {
        double hpCap; // ex : 8 cœurs => 16 HP
        int taskId; // -1 si pas de timer
    }

    public static void init(Plugin pl) {
        plugin = pl;
    }

    private static Map<String, AbsorbEntry> getMap(UUID id) {
        return ABS.computeIfAbsent(id, k -> new HashMap<>());
    }

    private static double totalCapHp(Map<String, AbsorbEntry> map) {
        double sum = 0.0;
        for (AbsorbEntry e : map.values()) {
            sum += e.hpCap;
        }
        return sum;
    }

    /**
     * Donne des coeurs jaunes stackables par "source".
     *
     * @param sourceId identifiant unique du buff (ex: "spell_tank_w",
     *                 "item_rookern")
     * @param hearts   nombre de cœurs ajoutés par CETTE source (ex: 8.0)
     * @param seconds  durée du buff (0 ou négatif = pas d'expiration automatique)
     */
    public static void absorption(Player p, String sourceId, double hearts, int seconds) {
        if (plugin == null) {
            throw new IllegalStateException("Buffs.init(plugin) n'a pas été appelé");
        }

        UUID id = p.getUniqueId();
        Map<String, AbsorbEntry> map = getMap(id);

        AbsorbEntry old = map.get(sourceId);
        if (old != null && old.taskId != -1) {
            Bukkit.getScheduler().cancelTask(old.taskId);
        }

        double hpCap = hearts * 2.0; // 1 cœur = 2 HP

        AbsorbEntry entry = new AbsorbEntry();
        entry.hpCap = hpCap;
        entry.taskId = -1;
        map.put(sourceId, entry);

        // Timer d'expiration si seconds > 0
        if (seconds > 0) {
            int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeSource(id, sourceId);
            }, seconds * 20L).getTaskId();
            entry.taskId = taskId;
        }

        double currentHp = p.getAbsorptionAmount();
        double totalCap = totalCapHp(map);

        // On ajoute ce que cette source apporte (au 1er cast)
        double addedHp = hpCap;
        if (old != null) {
            // refresh / changement de cap -> on n'ajoute que le delta positif
            addedHp = Math.max(0.0, hpCap - old.hpCap);
        }

        double newAbs = Math.min(totalCap, currentHp + addedHp);

        if (newAbs > 0.0) {
            // 1) on met un effet ABSORPTION cohérent avec la quantité
            applyAbsorptionEffect(p, newAbs, seconds);
            // 2) on force la valeur exacte côté serveur
            p.setAbsorptionAmount(newAbs);
        } else {
            p.removePotionEffect(PotionEffectType.ABSORPTION);
            p.setAbsorptionAmount(0.0);
        }

    }

    /**
     * Ajoute (ou refresh) un effet ABSORPTION pour afficher les coeurs,
     * en approximant le bon niveau à partir de la quantité (hp).
     */
    private static void applyAbsorptionEffect(Player p, double hp, int secondsHint) {
        if (hp <= 0.0) {
            p.removePotionEffect(PotionEffectType.ABSORPTION);
            return;
        }

        int durationTicks = (secondsHint > 0 ? secondsHint * 20 : 20 * 60); // 60s par défaut

        // 4 HP par niveau d'absorption (approximation vanilla)
        int level = (int) Math.ceil(hp / 4.0) - 1;
        if (level < 0)
            level = 0;

        p.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION,
                durationTicks,
                level, // niveau basé sur la quantité d'absorption
                false,
                false,
                true));
    }

    private static void removeSource(UUID id, String sourceId) {
        Map<String, AbsorbEntry> map = ABS.get(id);
        if (map == null)
            return;

        AbsorbEntry removed = map.remove(sourceId);
        if (removed == null)
            return;

        if (removed.taskId != -1) {
            Bukkit.getScheduler().cancelTask(removed.taskId);
        }

        Player p = Bukkit.getPlayer(id);
        if (p == null) {
            if (map.isEmpty()) {
                ABS.remove(id);
            }
            return;
        }

        double totalCap = totalCapHp(map);
        double currentHp = p.getAbsorptionAmount();
        double newAbs = Math.min(currentHp, totalCap);

        if (map.isEmpty() || newAbs <= 0.0) {
            ABS.remove(id);
            p.removePotionEffect(PotionEffectType.ABSORPTION);
            p.setAbsorptionAmount(0.0);
        } else {
            applyAbsorptionEffect(p, newAbs, 60);
            p.setAbsorptionAmount(newAbs);
        }

    }

    public static void removeSource(Player p, String sourceId) {
        removeSource(p.getUniqueId(), sourceId);
    }

    /**
     * Nettoie tout ce qui vient de NOTRE système (utile sur /reload, déco, etc.).
     */
    public static void clearAllAbsorption(Player p) {
        UUID id = p.getUniqueId();
        Map<String, AbsorbEntry> map = ABS.remove(id);
        if (map != null) {
            for (AbsorbEntry e : map.values()) {
                if (e.taskId != -1) {
                    Bukkit.getScheduler().cancelTask(e.taskId);
                }
            }
        }
        p.removePotionEffect(PotionEffectType.ABSORPTION);
        p.setAbsorptionAmount(0.0);
    }
}

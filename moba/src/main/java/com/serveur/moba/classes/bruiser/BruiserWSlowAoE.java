package com.serveur.moba.classes.bruiser;

import com.serveur.moba.ability.*;
import com.serveur.moba.team.TeamService;
import com.serveur.moba.util.Buffs;
import com.serveur.moba.util.Flags;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class BruiserWSlowAoE implements Ability {
    private final CooldownService cds;
    private final int amp;
    private final int seconds;
    private final double radius;
    private final long cdMs;
    private final TeamService teams;
    private static final String FLAG_CC_IMMUNE = "CC_IMMUNE";
    private final Flags flags;

    public BruiserWSlowAoE(CooldownService cds, TeamService teams, Flags globalFlags, int amp, int seconds,
            double radius, long cdMs) {
        this.cds = cds;
        this.teams = teams;
        this.flags = globalFlags;
        this.amp = amp;
        this.seconds = seconds;
        this.radius = radius;
        this.cdMs = cdMs;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.BRUISER_W, cdMs)) {
            p.sendMessage("§cW en CD.");
            return false;
        }

        // ====== ANIMATION VISUELLE ======
        spawnSoulRing(p, radius);

        // ====== Effet de slow ======
        p.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof Player target && !target.equals(p))
                .map(e -> (Player) e)
                .filter(target -> !teams.areAllies(p, target))
                .filter(target -> !flags.has(target, FLAG_CC_IMMUNE))
                .forEach(target -> Buffs.give(target, PotionEffectType.SLOWNESS, amp, seconds));

        p.sendMessage("§a[Bruiser] W — Slowness AOE");
        return true;
    }

    /**
     * Crée un cercle de particules SOUL autour du joueur.
     */
    private void spawnSoulRing(Player p, double radius) {
        int points = 40; // plus = cercle plus propre
        double height = 0.2;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            p.getWorld().spawnParticle(
                    org.bukkit.Particle.SOUL,
                    p.getLocation().add(x, height, z),
                    2,
                    0.04, 0.02, 0.04,
                    0);
        }
    }

}

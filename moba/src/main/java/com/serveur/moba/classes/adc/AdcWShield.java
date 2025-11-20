package com.serveur.moba.classes.adc;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Flags;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class AdcWShield implements Ability {
    private final CooldownService cds;
    private final Flags flags;
    private final long cdMs;
    private final long shieldMs;

    public AdcWShield(CooldownService cds, Flags flags, long cdMs, long shieldMs) {
        this.cds = cds;
        this.flags = flags;
        this.cdMs = cdMs;
        this.shieldMs = shieldMs;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.ADC_W, cdMs)) {
            p.sendMessage("§cW en CD.");
            return false;
        }

        flags.set(p, "GOD_SHIELD", true);

        long totalTicks = shieldMs / 50L;

        // ----------------------------
        // Burst d'activation (flash)
        // ----------------------------
        p.getWorld().spawnParticle(
                Particle.END_ROD,
                p.getLocation().add(0, 1, 0),
                60,
                0.4, 0.8, 0.4,
                0.05);

        p.getWorld().spawnParticle(
                Particle.ENCHANT,
                p.getLocation().add(0, 1, 0),
                100,
                0.7, 0.7, 0.7,
                0.15);

        // ----------------------------
        // Animation continue du shield
        // ----------------------------
        new BukkitRunnable() {
            long lived = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead() || lived > totalTicks || !flags.has(p, "GOD_SHIELD")) {
                    cancel();

                    // Dissipation visuelle (verre qui éclate)
                    p.getWorld().spawnParticle(
                            Particle.TOTEM_OF_UNDYING,
                            p.getLocation().add(0, 1, 0),
                            40,
                            0.6, 0.8, 0.6,
                            0.2,
                            org.bukkit.Material.GLASS.createBlockData());
                    return;
                }

                Location center = p.getLocation().add(0, 1.0, 0);

                // ----- Dôme circulaire -----
                double radius = 1.2;
                int points = 24;

                // Cercle du bas
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location loc = center.clone().add(x, -0.4, z);

                    p.getWorld().spawnParticle(
                            Particle.GLOW,
                            loc, 1, 0, 0, 0, 0);
                }

                // Cercle du haut (dôme)
                /*
                 * for (int i = 0; i < points; i++) {
                 * double angle = 2 * Math.PI * i / points;
                 * double x = Math.cos(angle) * radius * 0.7;
                 * double z = Math.sin(angle) * radius * 0.7;
                 * Location loc = center.clone().add(x, 0.6, z);
                 * 
                 * p.getWorld().spawnParticle(
                 * Particle.END_ROD,
                 * loc, 1, 0, 0, 0, 0);
                 * }
                 */

                // Flux verticaux (montées de particules)
                for (int i = 0; i < 6; i++) {
                    double x = (Math.random() - 0.5) * 1.2;
                    double z = (Math.random() - 0.5) * 1.2;

                    Location loc = center.clone().add(x, -0.2, z);
                    p.getWorld().spawnParticle(
                            Particle.PALE_OAK_LEAVES,
                            loc,
                            1,
                            0, 0.5, 0,
                            0);
                }

                lived += 2;
            }
        }.runTaskTimer(ctx.plugin(), 0L, 2L);

        // ----------------------------
        // Fin du shield
        // ----------------------------
        ctx.plugin().getServer().getScheduler().runTaskLater(
                ctx.plugin(),
                () -> flags.set(p, "GOD_SHIELD", false),
                totalTicks);

        p.sendMessage("§a[ADC] W — Bouclier actif");
        return true;
    }
}

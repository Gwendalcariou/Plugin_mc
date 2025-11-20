package com.serveur.moba.classes.tank;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Buffs;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;

public class TankWAbsorb implements Ability {
    private final CooldownService cds;
    private final double hearts;
    private final long cdMs;

    private static final Particle.DustOptions GOLD_AURA = new Particle.DustOptions(Color.fromRGB(255, 215, 80), 1.7f);

    public TankWAbsorb(CooldownService cds, double hearts, long cdMs) {
        this.cds = cds;
        this.hearts = hearts;
        this.cdMs = cdMs;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.TANK_W, cdMs)) {
            long r = cds.remaining(p, CooldownIds.TANK_W, cdMs) / 1000;
            p.sendMessage("§cW en CD (" + r + "s).");
            return false;
        }

        // Applique l'absorption
        Buffs.absorption(p, hearts, 20);

        // Lance l'aura dorée
        startGoldAura(ctx, p);

        p.sendMessage("§a[Tank] W — Absorption +" + hearts + "❤");
        return true;
    }

    private void startGoldAura(AbilityContext ctx, Player p) {
        final long durationTicks = 20 * 20L; // 20 secondes = 400 ticks
        final long period = 2L;

        new BukkitRunnable() {
            long lived = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead() || lived > durationTicks) {
                    cancel();
                    return;
                }

                Location base = p.getLocation().add(0, 0.8, 0);
                double radius = 1.0;
                int points = 20;
                double angleOffset = lived * 0.15;

                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + angleOffset;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    p.getWorld().spawnParticle(
                            Particle.DUST,
                            base.clone().add(x, 0, z),
                            1,
                            0.03, 0.02, 0.03,
                            0,
                            GOLD_AURA);
                }

                lived += period;
            }
        }.runTaskTimer(ctx.plugin(), 0L, period);
    }
}

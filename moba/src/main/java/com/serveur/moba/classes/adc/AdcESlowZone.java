package com.serveur.moba.classes.adc;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Buffs;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class AdcESlowZone implements Ability {
    private final CooldownService cds;
    private final int amp;
    private final double radius;
    private final long zoneMs;
    private final long cdMs;

    public AdcESlowZone(CooldownService cds, int amp, double radius, long zoneMs, long cdMs) {
        this.cds = cds;
        this.amp = amp;
        this.radius = radius;
        this.zoneMs = zoneMs;
        this.cdMs = cdMs;
    }

    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.ADC_E, cdMs)) {
            p.sendMessage("§cE en CD.");
            return false;
        }

        Location center = p.getLocation().clone();

        final long stepTicks = 10L;

        final long iterations = Math.max(1, zoneMs / (stepTicks * 50L));

        var task = new Object() {
            long i = 0;
            int id = -1;
        };

        task.id = ctx.plugin().getServer().getScheduler().scheduleSyncRepeatingTask(ctx.plugin(), () -> {
            task.i++;
            // visuel
            p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, center, 8, radius / 2, 0.1, radius / 2, 0.0);
            // slow (rafraîchi)
            for (var e : center.getWorld().getNearbyPlayers(center, radius)) {
                if (e.equals(p))
                    continue;
                Buffs.give(e, PotionEffectType.SLOWNESS, amp, 1); // 1s, réappliqué toutes les 0.5s
            }
            if (task.i >= iterations) {
                ctx.plugin().getServer().getScheduler().cancelTask(task.id);
            }
        }, 0L, stepTicks);

        p.sendMessage("§a[ADC] E — Zone de ralentissement déployée");
        return true;
    }
}

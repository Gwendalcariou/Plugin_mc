package com.serveur.moba.classes.adc;

import com.serveur.moba.ability.*;
import com.serveur.moba.util.Buffs;

import org.bukkit.Particle;
import org.bukkit.attribute.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;

import java.util.UUID;

public class AdcRAllSteroid implements Ability {
    private final CooldownService cds;
    private final long cdMs;
    private final long durationMs;
    private final double bonusMaxHearts; // +X coeurs (max health)
    private final double absorbHearts; // +X coeurs d’absorption

    private final UUID MAXHP_MOD = UUID.fromString("2c3d98e6-4c47-4d7f-9f0a-aa2e0e11a111");

    public AdcRAllSteroid(CooldownService cds, long cdMs, long durationMs, double bonusMaxHearts, double absorbHearts) {
        this.cds = cds;
        this.cdMs = cdMs;
        this.durationMs = durationMs;
        this.bonusMaxHearts = bonusMaxHearts;
        this.absorbHearts = absorbHearts;
    }

    @SuppressWarnings("removal")
    @Override
    public boolean cast(AbilityContext ctx) {
        Player p = ctx.player();
        if (!cds.ready(p, CooldownIds.ADC_R, cdMs)) {
            p.sendMessage("§cR en CD.");
            return false;
        }

        // Max health+
        AttributeInstance maxHp = p.getAttribute(Attribute.MAX_HEALTH);
        if (maxHp != null) {
            double plus = bonusMaxHearts * 2.0;
            AttributeModifier mod = new AttributeModifier(MAXHP_MOD, "adc_r_maxhp", plus,
                    AttributeModifier.Operation.ADD_NUMBER);
            maxHp.removeModifier(MAXHP_MOD);
            maxHp.addModifier(mod);
            p.setHealth(Math.min(p.getHealth() + plus, maxHp.getValue())); // heal pour profiter
        }

        // Absorption
        Buffs.absorption(p, absorbHearts, 20);

        // Strength I, Speed I, Fire Resistance

        // Durée en secondes pour les potions (Buffs.give semble prendre des secondes)
        int durationSeconds = (int) (durationMs / 1000L);
        Buffs.give(p, PotionEffectType.STRENGTH, 1, durationSeconds);
        Buffs.give(p, PotionEffectType.SPEED, 1, durationSeconds);
        Buffs.give(p, PotionEffectType.FIRE_RESISTANCE, 1, durationSeconds);

        // -------------------------
        // Effets visuels du R
        // -------------------------

        // 1) Petit burst à l'activation
        p.getWorld().spawnParticle(
                Particle.ENCHANT,
                p.getLocation().add(0, 1, 0),
                80,
                0.6, 0.8, 0.6, // spread
                0.1);

        // 2) Aura tournante pendant toute la durée du R
        long totalTicks = durationMs / 50L;

        new BukkitRunnable() {
            long lived = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead() || lived > totalTicks) {
                    cancel();
                    return;
                }

                Location base = p.getLocation().add(0, 0.6, 0);

                double radius = 0.7;
                int points = 12;
                double angleOffset = lived * 0.3;

                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI * i / points) + angleOffset;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = base.clone().add(x, 0, z);

                    p.getWorld().spawnParticle(
                            Particle.FALLING_OBSIDIAN_TEAR,
                            particleLoc,
                            1,
                            0, 0, 0,
                            0);
                }

                lived += 2;
            }
        }.runTaskTimer(ctx.plugin(), 0L, 2L);

        // Cleanup synchro avec les PV & particules
        ctx.plugin().getServer().getScheduler().runTaskLater(ctx.plugin(), () -> {
            AttributeInstance mh = p.getAttribute(Attribute.MAX_HEALTH);
            if (mh != null)
                mh.removeModifier(MAXHP_MOD);
            p.sendMessage("§e[ADC] R — expiré");
        }, totalTicks);

        p.sendMessage("§a[ADC] R — Stéroïde activé");
        return true;
    }
}

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
        p.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof Player target && !target.equals(p))
                .map(e -> (Player) e)
                .filter(target -> !teams.areAllies(p, target))
                .filter(target -> !flags.has(target, FLAG_CC_IMMUNE))
                .forEach(target -> Buffs.give(target, PotionEffectType.SLOWNESS, amp, seconds));

        p.sendMessage("§a[Bruiser] W — Slowness AOE");
        return true;
    }
}

package com.serveur.moba.team;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class TeamService {

    private final Map<UUID, TeamId> teams = new HashMap<>();

    private final Scoreboard scoreboard;
    private final org.bukkit.scoreboard.Team redSbTeam;
    private final org.bukkit.scoreboard.Team blueSbTeam;

    public TeamService() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getMainScoreboard();

        this.redSbTeam = getOrCreateTeam("RED");
        this.blueSbTeam = getOrCreateTeam("BLUE");

        setupTeamVisuals(redSbTeam, TeamId.RED);
        setupTeamVisuals(blueSbTeam, TeamId.BLUE);
    }

    private org.bukkit.scoreboard.Team getOrCreateTeam(String name) {
        org.bukkit.scoreboard.Team t = scoreboard.getTeam(name);
        if (t == null) {
            t = scoreboard.registerNewTeam(name);
        }
        return t;
    }

    private void setupTeamVisuals(org.bukkit.scoreboard.Team team, TeamId id) {
        // Couleur du pseudo en tab & au-dessus de la tête
        switch (id) {
            case RED -> team.setColor(org.bukkit.ChatColor.RED);
            case BLUE -> team.setColor(org.bukkit.ChatColor.BLUE);
        }

        // Pas de friendly fire Vanilla
        team.setAllowFriendlyFire(false);
        // Juste pour confort
        team.setCanSeeFriendlyInvisibles(true);
        team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
    }

    public Optional<TeamId> getTeam(Player p) {
        return Optional.ofNullable(teams.get(p.getUniqueId()));
    }

    public NamedTextColor getColor(Player p) {
        return getTeam(p).map(TeamId::color).orElse(NamedTextColor.WHITE);
    }

    public void join(Player p, TeamId id) {
        leave(p); // clean ancien état

        teams.put(p.getUniqueId(), id);

        org.bukkit.scoreboard.Team sbTeam = (id == TeamId.RED) ? redSbTeam : blueSbTeam;
        sbTeam.addEntry(p.getName());

        // Pour que le joueur voie les couleurs
        p.setScoreboard(scoreboard);
    }

    public void leave(Player p) {
        teams.remove(p.getUniqueId());
        redSbTeam.removeEntry(p.getName());
        blueSbTeam.removeEntry(p.getName());
    }

    public void handleQuit(Player p) {
        leave(p);
    }

    public boolean areAllies(Player a, Player b) {
        TeamId ta = teams.get(a.getUniqueId());
        TeamId tb = teams.get(b.getUniqueId());
        return ta != null && ta == tb;
    }
}

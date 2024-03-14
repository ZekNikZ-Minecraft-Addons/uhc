package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.gametools.scoreboard.GameScoreboard
import io.zkz.mc.gametools.util.BukkitUtils.forEachPlayer
import org.bukkit.Statistic
import java.util.*

@Injectable
object UhcStats {
    private val scoreboards: MutableList<GameScoreboard> = ArrayList()
    private val playerEliminationOrder: MutableList<UUID> = ArrayList()
    private val teamEliminationOrder: MutableList<String> = ArrayList()

    fun resetStats() {
        scoreboards.clear()
        playerEliminationOrder.clear()
        teamEliminationOrder.clear()

        forEachPlayer {
            it.setStatistic(Statistic.MOB_KILLS, 0)
            it.setStatistic(Statistic.PLAYER_KILLS, 0)

            it.setStatistic(Statistic.DAMAGE_TAKEN, 0)

            it.setStatistic(Statistic.WALK_ONE_CM, 0)
            it.setStatistic(Statistic.BOAT_ONE_CM, 0)
            it.setStatistic(Statistic.FALL_ONE_CM, 0)
            it.setStatistic(Statistic.CLIMB_ONE_CM, 0)
            it.setStatistic(Statistic.SWIM_ONE_CM, 0)
            it.setStatistic(Statistic.MINECART_ONE_CM, 0)
            it.setStatistic(Statistic.FLY_ONE_CM, 0)
            it.setStatistic(Statistic.PIG_ONE_CM, 0)
            it.setStatistic(Statistic.HORSE_ONE_CM, 0)
        }
    }

// TODO: implement
//
//    public void startStatScoreboards(JavaPlugin plugin) {
//        ScoreboardService.getInstance().resetAllScoreboards();
//
//        // Rankings scoreboard
//        GameScoreboard rankings = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.GOLD + ChatColor.BOLD + "Rankings");
//        if (SettingsManager.getInstance().teamGame().get() == TeamStatus.TEAM_GAME) {
//            for (int i = this.teamEliminationOrder.size() - 1; i >= 0; i--) {
//                String teamId = this.teamEliminationOrder.get(i);
//                GameTeam team = TeamService.getInstance().getTeam(teamId);
//                if (team == null) {
//                    continue;
//                }
//                rankings.addEntry(team.getFormatCode() + team.getName());
//            }
//        } else {
//            for (int i = this.playerEliminationOrder.size() - 1; i >= 0; i--) {
//                UUID playerUUID = this.playerEliminationOrder.get(i);
//                OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
//                if (player == null) {
//                    continue;
//                }
//                rankings.addEntry(player.getName());
//            }
//        }
//        this.scoreboards.add(rankings);
//
//        // Player kills
//        GameScoreboard playerKills = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.RED + ChatColor.BOLD + "Player Kills");
//        Bukkit.getOnlinePlayers().stream()
//            .sorted(Comparator.comparing((Player player) -> player.getStatistic(Statistic.PLAYER_KILLS)).reversed())
//            .forEach(player ->
//                playerKills.addEntry(new IntValueEntry(
//                    playerKills,
//                    player.getName().substring(0, Math.min(14, player.getName().length())) + ": ",
//                    ValueEntry.ValuePos.SUFFIX,
//                    player.getStatistic(Statistic.PLAYER_KILLS)
//                ))
//            );
//        this.scoreboards.add(playerKills);
//
//        // Mob kills
//        GameScoreboard mobKills = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.AQUA + ChatColor.BOLD + "Mob Kills");
//        Bukkit.getOnlinePlayers().stream()
//            .sorted(Comparator.comparing((Player player) -> player.getStatistic(Statistic.MOB_KILLS)).reversed())
//            .forEach(player ->
//                mobKills.addEntry(new IntValueEntry(
//                    mobKills,
//                    player.getName().substring(0, Math.min(14, player.getName().length())) + ": ",
//                    ValueEntry.ValuePos.SUFFIX,
//                    player.getStatistic(Statistic.MOB_KILLS)
//                ))
//            );
//        this.scoreboards.add(mobKills);
//
//        // Damage taken
//        GameScoreboard damageTaken = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.YELLOW + ChatColor.BOLD + "Damage Taken");
//        Bukkit.getOnlinePlayers().stream()
//            .sorted(Comparator.comparing((Player player) -> player.getStatistic(Statistic.DAMAGE_TAKEN)).reversed())
//            .forEach(player ->
//                damageTaken.addEntry(new IntValueEntry(
//                    damageTaken,
//                    player.getName().substring(0, Math.min(14, player.getName().length())) + ": ",
//                    ValueEntry.ValuePos.SUFFIX,
//                    player.getStatistic(Statistic.DAMAGE_TAKEN)
//                ))
//            );
//        this.scoreboards.add(damageTaken);
//
//        // Distance traveled
//        GameScoreboard distanceTraveled = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.GREEN + ChatColor.BOLD + "Distance Traveled");
//        Bukkit.getOnlinePlayers().stream()
//            .map(player -> new Pair<>(
//                player,
//                player.getStatistic(Statistic.WALK_ONE_CM) +
//                    player.getStatistic(Statistic.BOAT_ONE_CM) +
//                    player.getStatistic(Statistic.FALL_ONE_CM) +
//                    player.getStatistic(Statistic.CLIMB_ONE_CM) +
//                    player.getStatistic(Statistic.SWIM_ONE_CM) +
//                    player.getStatistic(Statistic.MINECART_ONE_CM) +
//                    player.getStatistic(Statistic.FLY_ONE_CM) +
//                    player.getStatistic(Statistic.PIG_ONE_CM) +
//                    player.getStatistic(Statistic.HORSE_ONE_CM)
//            ))
//            .sorted(Comparator.comparing((Function<Pair<? extends Player, Integer>, Integer>) Pair::second).reversed())
//            .forEach(pair -> {
//                Player player = pair.getKey();
//                int distance = pair.getValue() / 100;
//                distanceTraveled.addEntry(new IntValueEntry(
//                    distanceTraveled,
//                    player.getName().substring(0, Math.min(14, player.getName().length())) + ": ",
//                    ValueEntry.ValuePos.SUFFIX,
//                    distance
//                ));
//            });
//        this.scoreboards.add(distanceTraveled);
//
//        // Display scoreboards
//        new BukkitRunnable() {
//            int i = 0;
//
//            @Override
//            public void run() {
//                if (scoreboards.isEmpty()) {
//                    this.cancel();
//                }
//
//                ScoreboardService.getInstance().setGlobalScoreboard(scoreboards.get(i), false);
//
//                i = ++i % scoreboards.size();
//            }
//        }.runTaskTimer(plugin, 0, 120);
//    }

    fun trackPlayerElimination(playerUUID: UUID) {
        playerEliminationOrder.add(playerUUID)
    }

    fun trackTeamElimination(teamId: String) {
        teamEliminationOrder.add(teamId)
    }
}

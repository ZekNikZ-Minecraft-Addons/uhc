package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.injection.InjectionComponent
import io.zkz.mc.gametools.injection.inject
import io.zkz.mc.gametools.scoreboard.GameScoreboard
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.sound.StandardSounds
import io.zkz.mc.gametools.sound.playSound
import io.zkz.mc.gametools.team.TeamService
import io.zkz.mc.gametools.timer.AbstractTimer
import io.zkz.mc.gametools.timer.GameCountdownTimer
import io.zkz.mc.gametools.timer.GameCountupTimer
import io.zkz.mc.gametools.util.BukkitUtils.forEachPlayer
import io.zkz.mc.gametools.util.BukkitUtils.runNextTick
import io.zkz.mc.gametools.util.Chat
import io.zkz.mc.gametools.util.ChatType
import io.zkz.mc.gametools.util.WorldSyncUtils
import io.zkz.mc.gametools.util.getHighestBlock
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.minigamemanager.minigame.Round
import io.zkz.mc.minigamemanager.state.DefaultStates
import io.zkz.mc.uhc.overrides.RecipeOverrides
import io.zkz.mc.uhc.settings.UhcSettingsMixin
import io.zkz.mc.uhc.settings.enums.TeamStatus
import io.zkz.mc.uhc.settings.enums.TimeCycle
import io.zkz.mc.uhc.settings.enums.WeatherCycle
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.DurationUnit

class UHCRound : Round(), InjectionComponent, UhcSettingsMixin {
    private val minigameService by inject<MinigameService>()
    private val teamService by inject<TeamService>()
    private val scoreboardService by inject<ScoreboardService>()
    private val uhcService by inject<UhcService>()

    internal val LOBBY_TP_LOCATION = Vector(0, 201, 0)
    private var centerLocation: Location? = null
    var currentWorldBorderSpeed = 0.0
        private set
    var currentWorldBorderTarget = 0.0
        private set
    internal val assignedSpawnLocations = mutableMapOf<UUID, Location>()
    private lateinit var gameEventTimer: AbstractTimer

    override fun onSetup() {
        handleEnter(DefaultStates.PRE_ROUND) { onEnterPreRound() }
        handleEnter(UhcStates.WB_CLOSING_1) { onGameStart() }
        handleEnter(UhcStates.WB_CLOSING_2) { onPhase2Start() }
        handleEnter(UhcStates.SUDDEN_DEATH) { onSuddenDeathStart() }
        handleEnter(DefaultStates.POST_ROUND) { onGameEnd() }
    }

    override fun onSelected() {
        // Determine center location
        centerLocation = getHighestBlock(Bukkit.getWorlds()[0], 0, 0)

        // Setup lobby
        SchematicLoader.loadLobby()

        // Teleport players and set their spawns
        val world = Bukkit.getWorlds()[0]
        forEachPlayer(::setupLobbyPlayer)

        // Time and weather
        world.time = 0
        world.setStorm(false)

        // Spawnpoint
        world.setSpawnLocation(LOBBY_TP_LOCATION.blockX, LOBBY_TP_LOCATION.blockY, LOBBY_TP_LOCATION.blockZ)

        // Gamerules
        WorldSyncUtils.setGameRule(GameRule.NATURAL_REGENERATION, false)
        WorldSyncUtils.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false)
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        WorldSyncUtils.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        WorldSyncUtils.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true)
        forEachPlayer { it.allowFlight = false }

        // Worldborder
        WorldSyncUtils.setWorldBorderCenter(0.5, 0.5)
        WorldSyncUtils.worldBorderSize = worldBorderDistance1.toDouble()
        WorldSyncUtils.setWorldBorderWarningTime(60)

        // Difficulty
        WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL)

        // Set up default teams
        teamService.setupDefaultTeams()

        // Timer on scoreboard
        minigameService.setGlobalTimer(null, mm("Game starts in:"))
    }

    override fun onPreRoundTimerTick(secondsLeft: Int) {
        if (secondsLeft > 0) {
            Chat.sendMessage(ChatType.ACTIVE_INFO, mm("Game starting in $secondsLeft seconds..."))
        }
    }

    private fun onEnterPreRound() {
        // Setup world border
        val initialBorderSize = worldBorderDistance1
        WorldSyncUtils.setWorldBorderCenter(0.5, 0.5)
        WorldSyncUtils.worldBorderSize = initialBorderSize.toDouble()
        WorldSyncUtils.setWorldBorderWarningTime(180)
        WorldSyncUtils.setWorldBorderWarningDistance(10)
        WorldSyncUtils.setWorldBorderDamageBuffer(2.0)
        WorldSyncUtils.setWorldBorderDamageAmount(0.2)

        // Setup gamerules
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, timeCycle == TimeCycle.NORMAL)
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, weatherCycle == WeatherCycle.NORMAL)
        WorldSyncUtils.setGameRule(GameRule.DO_INSOMNIA, spawnPhantoms)

        // Assign spread player locations
        assignedSpawnLocations.clear()
        val world = Bukkit.getWorlds()[0]
        val competitors = alivePlayers.toList()
        val numCompetitors = competitors.size
        val spreadRadius = initialBorderSize / 2.0 - 150
        if (teamGame == TeamStatus.INDIVIDUAL_GAME || !teamsSpawnTogether) {
            for (i in 0 until numCompetitors) {
                var x: Int
                var z: Int
                var loc: Location
                var thisRadius = spreadRadius
                do {
                    x = (thisRadius * cos(2 * Math.PI * i / numCompetitors)).toInt()
                    z = (thisRadius * sin(2 * Math.PI * i / numCompetitors)).toInt()
                    loc = getHighestBlock(world, x, z)
                    thisRadius -= 1
                } while (loc.block.isLiquid)
                assignedSpawnLocations[competitors[i]] = loc.add(.5, 100.0, .5)
            }
        } else {
            val teams = aliveTeams.toList()
            val numTeams = teams.size
            for (i in 0 until numTeams) {
                var thisRadius = spreadRadius
                teams[i].members.forEach {
                    var x: Int
                    var z: Int
                    var loc: Location
                    do {
                        x = (thisRadius * cos(2 * Math.PI * i / numTeams)).toInt()
                        z = (thisRadius * sin(2 * Math.PI * i / numTeams)).toInt()
                        loc = getHighestBlock(world, x, z)
                        thisRadius -= 1
                    } while (loc.block.isLiquid)
                    assignedSpawnLocations[it] = loc.add(.5, 100.0, .5)
                }
            }
        }

        // Setup & TP players
        forEachPlayer(::setupPreRoundPlayer)

        // Clear lobby
        SchematicLoader.clearLobby()
    }

    private fun onGameStart() {
        // Effects
        onlineAlivePlayers.forEach { player ->
            player.inventory.clear()
            player.gameMode = GameMode.SURVIVAL
            player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 300 * 20, 1))
            player.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 1, 1))
            player.health = 20.0
            player.foodLevel = 20
            player.saturation = 20f

            // TODO: give team-tracking compasses for proximity chat
        }

        // World stuff
        WorldSyncUtils.setDifficulty(Difficulty.HARD)
        WorldSyncUtils.setTime(0)
        WorldSyncUtils.setWeatherClear()
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_SPAWNING, true)

        // Start world border
        this.currentWorldBorderTarget = worldBorderDistance2.toDouble()
        this.currentWorldBorderSpeed = (worldBorderDistance1 - worldBorderDistance2) / 2.0 / (worldBorderTime1 * 60)
        WorldSyncUtils.setWorldBorderSize(worldBorderDistance2.toDouble(), worldBorderTime1 * 60L)
        Chat.sendMessage(
            ChatType.ACTIVE_INFO,
            mm(
                "The world border will now shrink to <legacy_aqua><0></legacy_aqua> blocks in diameter over <legacy_aqua><1></legacy_aqua> minutes (<legacy_yellow>about 1 block every <2> second(s)</legacy_yellow>).",
                currentWorldBorderTarget,
                worldBorderTime1,
                String.format("%.1f", 1 / currentWorldBorderSpeed),
            ),
        )

        // Elapsed time timer
        minigameService.setGlobalTimer(GameCountupTimer(uhcService.plugin, 10), mm("Game time:"))
        gameEventTimer = GameCountupTimer(uhcService.plugin, 5)
        if (permadayTime > 0) {
            gameEventTimer.scheduleEvent((permadayTime - 5) * 60000L) {
                Chat.sendMessage(
                    ChatType.GAME_INFO,
                    mm("Permaday will be enabled in <legacy_aqua>5</legacy_aqua> minutes."),
                )
            }
            gameEventTimer.scheduleEvent((permadayTime - 1) * 60000L) {
                Chat.sendMessage(
                    ChatType.GAME_INFO,
                    mm("Permaday will be enabled in <legacy_aqua>1</legacy_aqua> minute."),
                )
            }
            gameEventTimer.scheduleEvent(permadayTime * 60000L) {
                WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
                WorldSyncUtils.setTime(6000)
            }
        }
        if (peacefulTime > 0) {
            this.gameEventTimer.scheduleEvent((peacefulTime - 5) * 60000L) {
                Chat.sendMessage(
                    ChatType.GAME_INFO,
                    mm("Peaceful mode will be enabled in <legacy_aqua>5</legacy_aqua> minutes."),
                )
            }
            this.gameEventTimer.scheduleEvent((peacefulTime - 1) * 60000L) {
                Chat.sendMessage(
                    ChatType.GAME_INFO,
                    mm("Peaceful mode will be enabled in <legacy_aqua>1</legacy_aqua> minute."),
                )
            }
            this.gameEventTimer.scheduleEvent(peacefulTime * 60000L) {
                WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL)
            }
        }
        if (shieldlessTime > 0) {
            this.gameEventTimer.scheduleEvent((shieldlessTime - 5) * 60000L) {
                Chat.sendMessage(
                    ChatType.GAME_INFO,
                    mm("Shields will be disabled in <legacy_aqua>5</legacy_aqua> minutes."),
                )
            }
            this.gameEventTimer.scheduleEvent((shieldlessTime - 1) * 60000L) {
                Chat.sendMessage(
                    ChatType.GAME_INFO,
                    mm("Shields will be disabled in <legacy_aqua>1</legacy_aqua> minute."),
                )
            }
            this.gameEventTimer.scheduleEvent(shieldlessTime * 60000L) {
                forEachPlayer { it.inventory.remove(Material.SHIELD) }
                RecipeOverrides.removeShieldRecipe(uhcService.plugin)
            }
        }
        this.gameEventTimer.start()

        // Sounds
        playSound(Bukkit.getOnlinePlayers(), StandardSounds.GAME_OVER, 1f, 1f)
    }

    private fun onPhase2Start() {
        if (worldBorderDistance3 == 0 || worldBorderTime2 == 0) {
            runNextTick(::onWorldBorderStoppedMoving)
            return
        }

        // Start second world border
        currentWorldBorderTarget = worldBorderDistance3.toDouble()
        currentWorldBorderSpeed = (worldBorderDistance2 - worldBorderDistance3) / 2.0 / (worldBorderTime2 * 60)
        WorldSyncUtils.setWorldBorderSize(worldBorderDistance3.toDouble(), worldBorderTime2 * 60L)
        Chat.sendMessage(
            ChatType.ACTIVE_INFO,
            mm(
                "The world border will now shrink to <legacy_aqua><0></legacy_aqua> blocks in diameter over <legacy_aqua><1></legacy_aqua> minutes (<legacy_yellow>about 1 block every <2> second(s)</legacy_yellow>).",
                currentWorldBorderTarget.toInt(),
                worldBorderTime2,
                String.format("%.1f", 1 / currentWorldBorderSpeed),
            ),
        )
    }

    private fun onSuddenDeathStart() {
        minigameService.setGlobalTimer()
        WorldSyncUtils.setWorldBorderSize(1.0, suddenDeathTime * 60L)
        Chat.sendMessage(
            ChatType.ACTIVE_INFO,
            mm("The world border is now a cube and will shrink to <legacy_aqua>1</legacy_aqua> block in diameter over <legacy_aqua>$suddenDeathTime</legacy_aqua> minutes."),
        )
    }

    private fun onGameEnd() {
        WorldSyncUtils.stopWorldBorder()
        forEachPlayer { it.allowFlight = true }
    }

    fun onWorldBorderStoppedMoving() {
        minigameService.setState(UhcStates.PARLAY)

        if (suddenDeathEnabled) {
            minigameService.setGlobalTimer(
                GameCountdownTimer(
                    uhcService.plugin,
                    20,
                    parlayTime.toLong(),
                    DurationUnit.MINUTES,
                ) { minigameService.setState(UhcStates.SUDDEN_DEATH) },
                mm("Sudden death in:"),
            )
            Chat.sendMessage(
                ChatType.ACTIVE_INFO,
                mm("Sudden death will begin in <legacy_aqua><0></legacy_aqua> minutes.", parlayTime),
            )
        }
    }

    override fun setDead(playerId: UUID) {
        super.setDead(playerId)

        runNextTick { Bukkit.getPlayer(playerId)?.let(::setupInGamePlayer) }

        scoreboardService.allScoreboards.forEach(GameScoreboard::redraw)
        UhcStats.trackPlayerElimination(playerId)
        Chat.sendMessage(
            ChatType.ELIMINATION,
            mm(
                "<0> has been eliminated! ${alivePlayers.size} players remain.",
                Bukkit.getPlayer(playerId)!!.displayName(),
            ),
        )
        playSound(Bukkit.getOnlinePlayers(), StandardSounds.PLAYER_ELIMINATION, 1f, 1f)

        if (teamGame == TeamStatus.TEAM_GAME) {
            val team = teamService.getTeamOfPlayer(playerId)
            if (team != null) {
                UhcStats.trackTeamElimination(team.id)
                Chat.sendMessage(
                    ChatType.TEAM_ELIMINATION,
                    mm("<0> has been eliminated! ${aliveTeams.size} teams remain.", team.displayName),
                )
            }

            // End game
            if (aliveTeams.size <= 1) {
                minigameService.setState(DefaultStates.POST_ROUND)
            }
        } else {
            // End game
            if (alivePlayers.size <= 1) {
                minigameService.setState(DefaultStates.POST_ROUND)
            }
        }
    }

    fun setupLobbyPlayer(player: Player) {
        // Clear inventory
        player.inventory.clear()

        // Health & saturation
        player.health = 20.0
        player.foodLevel = 20
        player.fireTicks = 0

        // Game mode
        player.gameMode = GameMode.ADVENTURE

        // Teleport
        player.teleport(LOBBY_TP_LOCATION.toLocation(player.world))
    }

    fun setupPreRoundPlayer(player: Player) {
        // Clear inventory
        player.inventory.clear()

        // Health & saturation
        player.health = 20.0
        player.foodLevel = 20
        player.fireTicks = 0

        // Game mode and teleport
        if (isAlive(player)) {
            player.gameMode = GameMode.ADVENTURE
            player.teleport(this.assignedSpawnLocations[player.uniqueId] ?: LOBBY_TP_LOCATION.toLocation(player.world))
        } else {
            player.gameMode = GameMode.SPECTATOR
            player.teleport(LOBBY_TP_LOCATION.toLocation(player.world))
        }
    }

    fun setupInGamePlayer(player: Player) {
        if (isAlive(player)) {
            player.gameMode = GameMode.SURVIVAL
        } else {
            player.gameMode = GameMode.SPECTATOR
            player.teleport(LOBBY_TP_LOCATION.toLocation(player.world))
        }
    }

    fun setupPostGamePlayer(player: Player) {
        if (isAlive(player)) {
            player.gameMode = GameMode.ADVENTURE
        } else {
            player.gameMode = GameMode.SPECTATOR
        }
    }
}

val MinigameService.uhcRound
    get() = getCurrentRoundTyped<UHCRound>()

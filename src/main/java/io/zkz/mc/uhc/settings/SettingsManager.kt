package io.zkz.mc.uhc.settings

import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.gametools.scoreboard.GameScoreboard
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.settings.GameSettingCategory
import io.zkz.mc.gametools.settings.SettingsManager
import io.zkz.mc.gametools.settings.impl.BooleanSetting
import io.zkz.mc.gametools.settings.impl.EnumSetting
import io.zkz.mc.gametools.settings.impl.IntegerSetting
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.WorldSyncUtils.setGameRule
import io.zkz.mc.gametools.util.WorldSyncUtils.setTime
import io.zkz.mc.gametools.util.WorldSyncUtils.setWeatherClear
import io.zkz.mc.gametools.util.WorldSyncUtils.setWeatherRain
import io.zkz.mc.gametools.util.WorldSyncUtils.setWeatherStorm
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.minigamemanager.state.DefaultStates
import io.zkz.mc.uhc.UhcPlugin
import io.zkz.mc.uhc.settings.enums.CompassBehavior
import io.zkz.mc.uhc.settings.enums.GameDifficulty
import io.zkz.mc.uhc.settings.enums.SpectatorMode
import io.zkz.mc.uhc.settings.enums.TeamStatus
import io.zkz.mc.uhc.settings.enums.TimeCycle
import io.zkz.mc.uhc.settings.enums.WeatherCycle
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.potion.PotionType

@Injectable
class SettingsManager(
    plugin: UhcPlugin,
    private val minigameService: MinigameService,
    private val scoreboardService: ScoreboardService,
) : SettingsManager<UhcPlugin>(plugin) {
    // ==============
    // WORLD BEHAVIOR
    // ==============
    private val worldBehaviorCategory = GameSettingCategory(
        mm("World Behavior"),
        mm("Time and weather settings."),
        ISB.fromMaterial(Material.CLOCK),
    )

    val timeCycle = setting(
        worldBehaviorCategory,
        EnumSetting.from(
            mm("Time Cycle"),
            mm("Determine how time works in the game."),
            ISB.fromMaterial(Material.CLOCK),
            { TimeCycle.NORMAL },
        ),
    ) {
        if (minigameService.currentState.isInGame) {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, it.value == TimeCycle.NORMAL)

            when (it.value) {
                TimeCycle.DAY_ONLY -> {
                    setTime(6000)
                }

                TimeCycle.NIGHT_ONLY -> {
                    setTime(18000)
                }

                else -> {}
            }
        }
    }

    val weatherCycle: EnumSetting<WeatherCycle> = setting(
        worldBehaviorCategory,
        EnumSetting.from(
            mm("Weather Cycle"),
            mm("Determines how weather works in the game."),
            ISB.fromMaterial(Material.DEAD_BUSH),
            { WeatherCycle.CLEAR_ONLY },
        ),
    ) {
        if (minigameService.currentState.isInGame) {
            when (it.value) {
                WeatherCycle.CLEAR_ONLY -> {
                    setWeatherClear()
                }

                WeatherCycle.RAIN_ONLY -> {
                    setWeatherRain()
                }

                WeatherCycle.STORM_ONLY -> {
                    setWeatherStorm()
                }

                else -> {}
            }
        }
    }

    val spawnPhantoms: BooleanSetting = setting(
        worldBehaviorCategory,
        BooleanSetting(
            mm("Allow Phantom Spawning"),
            mm("Determines if phantoms will be allowed to spawn."),
            ISB.fromMaterial(Material.PHANTOM_MEMBRANE),
            { false },
        ),
    )

    val spawnHostileMobs: BooleanSetting = setting<Boolean, BooleanSetting>(
        worldBehaviorCategory,
        BooleanSetting(
            mm("Allow Hostile Mob Spawning"),
            mm("Determines if hostile mobs will be allowed to spawn."),
            ISB.fromMaterial(Material.ZOMBIE_HEAD),
            { true },
        ),
    )

    val difficulty: EnumSetting<GameDifficulty> = setting(
        worldBehaviorCategory,
        EnumSetting.from(
            mm("Game Difficulty"),
            mm("Determines the game difficulty"),
            ISB.fromMaterial(Material.NETHERITE_SWORD),
            { GameDifficulty.HARD },
        ),
    )

    // =====
    // ITEMS
    // =====
    private val itemsCategory = GameSettingCategory(
        mm("Items"),
        mm("Item settings."),
        ISB.fromMaterial(Material.COMPASS),
    )

    val compassBehavior: EnumSetting<CompassBehavior> = setting(
        itemsCategory,
        EnumSetting.from(
            mm("Compass Player Tracking"),
            mm("Determines which player(s) compasses track, if any."),
            ISB.fromMaterial(Material.COMPASS),
            { CompassBehavior.TRACK_ENEMIES },
        ),
    )

    val throwableFireballs: BooleanSetting = setting(
        itemsCategory,
        BooleanSetting(
            mm("Throwable Fireballs"),
            mm("Determines if fire charges can be thrown via right-click."),
            ISB.fromMaterial(Material.FIRE_CHARGE),
            { true },
        ),
    )

    val regenerationPotions: BooleanSetting = setting(
        itemsCategory,
        BooleanSetting(
            mm("Regeneration Potions"),
            mm("Determines if regeneration potions will be allowed."),
            ISB.fromMaterial(Material.POTION) {
                potion(PotionType.REGEN)
            },
            { false },
        ),
    )

    val goldenHeads: BooleanSetting = setting(
        itemsCategory,
        BooleanSetting(
            mm("Golden Heads"),
            mm("Determines if golden heads will be craftable."),
            ISB.fromMaterial(Material.PLAYER_HEAD),
            { true },
        ),
    )

    // =====
    // TEAMS
    // =====
    private val teamsCategory = GameSettingCategory(
        mm("Teams"),
        mm("Team gameplay settings."),
        ISB.fromMaterial(Material.IRON_CHESTPLATE),
    )

    val teamGame: EnumSetting<TeamStatus> = setting(
        teamsCategory,
        EnumSetting.from(
            mm("Team Mode"),
            mm("Determines if players will compete in teams or solo."),
            ISB.fromMaterial(Material.LEATHER_CHESTPLATE),
            { TeamStatus.INDIVIDUAL_GAME },
        ),
    ) {
        if (minigameService.currentState == DefaultStates.WAITING_FOR_PLAYERS) {
            scoreboardService.allScoreboards.forEach(GameScoreboard::redraw)
        }
    }

    val teamsSpawnTogether: BooleanSetting = setting(
        teamsCategory,
        BooleanSetting(
            mm("Teams Spawn Together"),
            mm("Determines if teams will spawn together at the beginning of the game."),
            ISB.fromMaterial(Material.GRASS_BLOCK),
            { true },
        ),
    )

    val spectatorInventories: EnumSetting<SpectatorMode> = setting(
        teamsCategory,
        EnumSetting.from(
            mm("Spectator Inventories"),
            mm("Determines if specators can see the inventories of players."),
            ISB.fromMaterial(Material.PHANTOM_MEMBRANE),
            { SpectatorMode.SPECTATORS_SEE_INVENTORIES },
        ),
    )

    // ============
    // WORLD BORDER
    // ============
    private val worldBorderCategory = GameSettingCategory(
        mm("Worldborder and Timings"),
        mm("Worldborder and game timing settings."),
        ISB.fromMaterial(Material.CLOCK),
    )

    val worldBorderDistance1: IntegerSetting = setting(
        worldBorderCategory,
        IntegerSetting(
            mm("World Border Diameter (Start)"),
            mm("Determines the initial diameter of the world border."),
            ISB.fromMaterial(Material.IRON_BARS),
            { 100 },
            { 5000 },
            { 100 },
            { 3000 },
        ),
    )

    val worldBorderTime1: IntegerSetting = setting(
        worldBorderCategory,
        IntegerSetting(
            mm("Phase 1 Time (Minutes)"),
            mm("Determines the time it takes for the world border\nto shrink from setting 1 to 2."),
            ISB.fromMaterial(Material.CLOCK),
            { 30 },
            { 120 },
            { 5 },
            { 60 },
        ),
    )

    val worldBorderDistance2: IntegerSetting = setting(
        worldBorderCategory,
        IntegerSetting(
            mm("World Border Diameter (Phase 2 Start)"),
            mm("Determines the diameter of the world border when the phase changes."),
            ISB.fromMaterial(Material.IRON_BARS, 2),
            { 100 },
            worldBorderTime1::value,
            { 100 },
            { 1000 },
        ),
    )

    val worldBorderTime2: IntegerSetting = setting(
        worldBorderCategory,
        IntegerSetting(
            mm("Phase 2 Time (Minutes)"),
            mm("Determines the time it takes for the world border\nto shrink from setting 2 to 3.\nSet to 0 to disable phase 2."),
            ISB.fromMaterial(Material.CLOCK, 2),
            { 0 },
            { 120 },
            { 5 },
            { 30 },
        ),
    )

    val worldBorderDistance3: IntegerSetting = setting<Int, IntegerSetting>(
        worldBorderCategory,
        IntegerSetting(
            mm("World Border Diameter (End)"),
            mm("Determines the final diameter of the world border."),
            ISB.fromMaterial(Material.IRON_BARS, 3),
            { 0 },
            worldBorderDistance2::value,
            { 100 },
            { 100 },
        ),
    )

    val suddenDeathEnabled: BooleanSetting = setting(
        worldBorderCategory,
        BooleanSetting(
            mm("Sudden Death"),
            mm("Determines if sudden death should be enabled."),
            ISB.fromMaterial(Material.CAMPFIRE),
            { true },
        ),
    )

    val parlayTime: IntegerSetting = setting(
        worldBorderCategory,
        IntegerSetting(
            mm("Parlay Time (Minutes)"),
            mm("Determines the time it takes for sudden death to start once the world border stops moving."),
            ISB.fromMaterial(Material.CLOCK, 3),
            { 1 },
            { 30 },
            { 1 },
            { 10 },
        ),
    )

    val suddenDeathTime: IntegerSetting = setting(
        worldBorderCategory,
        IntegerSetting(
            mm("Parlay Time (Minutes)"),
            mm("Determines the time it takes for sudden death to start once the world border stops moving."),
            ISB.fromMaterial(Material.CLOCK, 4),
            { 1 },
            { 20 },
            { 1 },
            { 5 },
        ),
    )

    // ================
    // LATE GAME EVENTS
    // ================
    private val lateGameEventsCategory = GameSettingCategory(
        mm("Late-Game Events"),
        mm("Settings to enable late-game events."),
        ISB.fromMaterial(Material.NETHERITE_INGOT),
    )

    val permadayTime: IntegerSetting = setting(
        lateGameEventsCategory,
        IntegerSetting(
            mm("Time to Enable Permaday (Minutes)"),
            mm("Determines the time it takes to enable permaday. Set to 0 to disable."),
            ISB.fromMaterial(Material.SUNFLOWER),
            { 0 },
            { 180 },
            { 1 },
            { 60 },
        ),
    )

    val peacefulTime: IntegerSetting = setting(
        lateGameEventsCategory,
        IntegerSetting(
            mm("Time to Enable Peaceful Mode (Minutes)"),
            mm("Determines the time it takes to enable peaceful mode. Set to 0 to disable."),
            ISB.fromMaterial(Material.POPPY),
            { 0 },
            { 180 },
            { 1 },
            { 60 },
        ),
    )

    val shieldlessTime: IntegerSetting = setting(
        lateGameEventsCategory,
        IntegerSetting(
            mm("Time to Enable No Shield Mode (Minutes)"),
            mm("Determines the time it takes to enable no shield mode. Set to 0 to disable."),
            ISB.fromMaterial(Material.SHIELD),
            { 0 },
            { 180 },
            { 1 },
            { 60 },
        ),
    )
}

package io.zkz.mc.uhc.game

import io.zkz.mc.gametools.hud.ActionBarService
import io.zkz.mc.gametools.injection.Injectable
import io.zkz.mc.gametools.scoreboard.ScoreboardService
import io.zkz.mc.gametools.service.PluginService
import io.zkz.mc.gametools.team.TeamService
import io.zkz.mc.gametools.util.ISB
import io.zkz.mc.gametools.util.isHostile
import io.zkz.mc.gametools.util.mm
import io.zkz.mc.minigamemanager.minigame.MinigameService
import io.zkz.mc.uhc.UhcPlugin
import io.zkz.mc.uhc.settings.SettingsManager
import io.zkz.mc.uhc.settings.enums.CompassBehavior
import io.zkz.mc.uhc.settings.enums.TeamStatus
import io.zkz.mc.uhc.settings.enums.WeatherCycle
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Fireball
import org.bukkit.entity.Ghast
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.min

@Injectable
class UhcEventsListenter(
    plugin: UhcPlugin,
    private val minigameService: MinigameService,
    private val teamService: TeamService,
    private val scoreboardService: ScoreboardService,
    private val actionBarService: ActionBarService,
    private val settingsManager: SettingsManager,
) : PluginService<UhcPlugin>(plugin) {
    @EventHandler
    fun onWeatherChange(event: WeatherChangeEvent) {
        val weatherState = settingsManager.weatherCycle.value
        val toRain = event.toWeatherState()

        if (weatherState === WeatherCycle.CLEAR_ONLY && toRain) {
            event.isCancelled = true
        } else if ((weatherState === WeatherCycle.RAIN_ONLY || weatherState === WeatherCycle.STORM_ONLY) && !toRain) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onThunderChange(event: ThunderChangeEvent) {
        val weatherState = settingsManager.weatherCycle.value
        val toStorm = event.toThunderState()

        if ((weatherState === WeatherCycle.CLEAR_ONLY || weatherState === WeatherCycle.RAIN_ONLY) && toStorm) {
            event.isCancelled = true
        } else if (weatherState === WeatherCycle.STORM_ONLY && !toStorm) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onSpawnMob(event: CreatureSpawnEvent) {
        if (!settingsManager.spawnHostileMobs.value && event.entity.isHostile()) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val action = event.action
        val material = event.getMaterial()
        val player = event.player

        // Fireball
        if (material == Material.FIRE_CHARGE && settingsManager.throwableFireballs.value && action == Action.RIGHT_CLICK_AIR) {
            if (player.hasCooldown(Material.FIRE_CHARGE)) {
                return
            }
            val fireball = player.launchProjectile(Fireball::class.java)
            fireball.velocity = fireball.velocity.multiply(2)
            fireball.yield *= 2.5f
            if (player.gameMode !== GameMode.CREATIVE) {
                player.inventory.itemInMainHand.amount -= 1
            }
            player.setCooldown(Material.FIRE_CHARGE, 20)
        }

        // Compass
        else if (material == Material.COMPASS) {
            if (player.hasCooldown(Material.COMPASS)) {
                event.setCancelled(true)
                return
            }

            if (settingsManager.compassBehavior.value != CompassBehavior.NORMAL) {
                var location: Location? = null
                var minDistance = Double.MAX_VALUE
                for (onlinePlayerUUID in UhcGame.alivePlayers) {
                    val onlinePlayer = Bukkit.getPlayer(onlinePlayerUUID)
                    if (onlinePlayer == null || onlinePlayer.uniqueId == player.uniqueId) {
                        continue
                    }

                    if (settingsManager.compassBehavior.value == CompassBehavior.TRACK_ENEMIES &&
                        settingsManager.teamGame.value == TeamStatus.TEAM_GAME &&
                        teamService.getTeamOfPlayer(player)?.id == teamService.getTeamOfPlayer(onlinePlayer)?.id
                    ) {
                        continue
                    }

                    if (onlinePlayer.location.getWorld() !== player.location.getWorld()) {
                        continue
                    }

                    val distance: Double = player.location.distance(onlinePlayer.location)
                    if (location == null || distance < minDistance) {
                        location = onlinePlayer.location
                        minDistance = min(distance, minDistance)
                    }
                }

                if (location == null) {
                    player.sendMessage("Could not track any player.")
                } else {
                    player.sendMessage("Updated tracking")
                    player.compassTarget = location
                    player.setCooldown(Material.COMPASS, 100)
                }
            }

            event.setCancelled(true)
        }
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) {
            return
        }

        // Prevent explosion damage from fireballs
        if (event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && event.damager is Fireball) {
            event.isCancelled = true
        }
    }

    // TODO: replace with PauseUtils (need a lockYpos parameter)
//    @EventHandler
//    fun onPlayerMove(event: PlayerMoveEvent) {
//        val to: Location = event.from
//        when (MinigameService.getInstance().getCurrentState()) {
//            PRE_ROUND -> {
//                to.y = event.to.y
//                to.pitch = event.to.pitch
//                to.yaw = event.to.yaw
//                event.setTo(to)
//            }
//
//            PAUSED -> {
//                to.pitch = event.to.pitch
//                to.yaw = event.to.yaw
//                event.setTo(to)
//            }
//
//            else -> {}
//        }
//    }

    @EventHandler
    fun onChestOpen(event: InventoryOpenEvent) {
        if (event.inventory.holder !is Chest && event.inventory.holder !is DoubleChest) {
            return
        }

        for (stack in event.inventory.contents) {
            if (stack != null && stack.type === Material.ENCHANTED_GOLDEN_APPLE) {
                event.inventory.removeItem(stack)
            }
        }
    }

    @EventHandler
    fun onEat(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item

        if (item.type == Material.ENCHANTED_GOLDEN_APPLE) {
            event.isCancelled = true
            player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 120 * 20, 0))
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1))
            player.foodLevel = min(player.foodLevel + 4, 20)

            val itemInHand: ItemStack = player.inventory.itemInMainHand
            if (itemInHand.amount > 1) {
                itemInHand.amount -= 1
            } else {
                player.inventory.setItemInMainHand(null)
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: EntityDeathEvent) {
        if (event.entity !is Player) {
            return
        }

        if (settingsManager.goldenHeads.value) {
            val player = event.entity as Player
            val killer = player.killer

            if (killer != null) {
                val location: Location = player.location
                location.getWorld().dropItemNaturally(
                    location,
                    ISB.fromMaterial(Material.PLAYER_HEAD) {
                        skullOwner(player)
                        name(mm("<0>'s Head", player.displayName()))
                    },
                )
            }
        }
    }

    @EventHandler
    fun onGhastDeath(event: EntityDeathEvent) {
        if (event.entity !is Ghast) {
            return
        }

        if (!settingsManager.regenerationPotions.value) {
            event.drops.clear()
        }
    }

    @EventHandler
    private fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) {
            return
        }

        if (!minigameService.currentState.isInGame) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (event.entity !is Player) {
            return
        }

        if (!minigameService.currentState.isInGame) {
            event.isCancelled = true
        }
    }
}

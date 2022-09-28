package me.itoncek.rays.hideandrayseeksyou;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class HideAndRaySeeksYou extends JavaPlugin {
	
	public static ArrayList<Player> hunters = new ArrayList<>();
	public static ArrayList<Player> alive = new ArrayList<>();
	public static HideAndRaySeeksYou plugin;
	public static FileConfiguration config;
	public static BukkitRunnable gameManager = new BukkitRunnable() {
		@Override
		public void run() {
			plugin.getServer().getPluginManager().registerEvents(new KOListener(), plugin);
			hunters.forEach((p) -> {
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
			});
			for (Player p : hunters) {
				addHunter(p, false);
			}
		}
	};
	public static BukkitRunnable endmanager = new BukkitRunnable() {
		@Override
		public void run() {
			if(alive.size() <= 1) {
				stop(alive.get(0));
				cancel();
			}
		}
	};
	public static BukkitRunnable graceManager = new BukkitRunnable() {
		@Override
		public void run() {
			hunters.forEach((p) -> {
				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, true));
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, true));
				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 255, true));
			});
			gameManager.runTaskLater(plugin, config.getInt("grace") * 20L);
		}
	};
	
	public static void startManager() {
		if(hunters.size() < 1) {
			announce(ChatColor.RED + "Start is not allowed, no hunters tagged");
		} else {
			pregraceManager.runTask(plugin);
		}
	}
	
	public static void announce(@NotNull String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.showTitle(Title.title(Component.text("⚠"), Component.text(message), Title.DEFAULT_TIMES));
		}
	}
	
	public static BukkitRunnable pregraceManager = new BukkitRunnable() {
		@Override
		public void run() {
			TreeMap<String, PlayerRecord> spawns = new TreeMap<>();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.setGameMode(GameMode.ADVENTURE);
				p.getInventory().clear();
				for (PotionEffect effect : p.getActivePotionEffects()) {
					p.removePotionEffect(effect.getType());
				}
				Location spawn = new Location(Bukkit.getWorld(config.getString("border.world", "world")), 0.0, 0, 0);
				Integer max = Math.toIntExact(Math.round(config.getDouble("border.starting-size") / 2 - (config.getDouble("border.starting-size") / 5)));
				if(!hunters.contains(p)) {
					spawns.put(p.getName(), new PlayerRecord(p, center(highestRandom(max))));
				} else {
					p.teleportAsync(center(spawn.toHighestLocation(HeightMap.WORLD_SURFACE)));
				}
			}
			for (World world : Bukkit.getWorlds()) {
				world.getWorldBorder().setSize(config.getDouble("border.starting-size"));
				world.setDifficulty(Difficulty.PEACEFUL);
			}
			new BukkitRunnable() {
				@Override
				public void run() {
					for (World world : Bukkit.getWorlds()) {
						world.setDifficulty(Difficulty.NORMAL);
					}
				}
			}.runTaskLater(plugin, 200L);
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, true));
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, true));
				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 255, true));
				p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 255, true));
				p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 255, true));
				p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, Integer.MAX_VALUE, 255, true));
			}
			spawns.forEach((k, v) -> v.p().teleportAsync(v.l()));
			spawns.forEach((k, v) -> {
				for (PotionEffect effect : v.p().getActivePotionEffects()) {
					v.p().removePotionEffect(effect.getType());
				}
				alive.add(v.p());
			});
			endmanager.runTaskTimer(plugin, 0L, 10L);
			graceManager.runTask(plugin);
		}
	};
	
	public static void addHunter(Player p) {
		addHunter(p, false);
	}
	
	public static void addHunter(Player p, boolean b) {
		if(config.getBoolean("after-elimination-hunter")) {
			ItemStack stick = new ItemStack(Material.STICK);
			ItemStack rockets = new ItemStack(Material.FIREWORK_ROCKET, 64);
			
			//Stick params
			stick.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
			
			//Rockets
			FireworkMeta rocketsMeta = (FireworkMeta) rockets.getItemMeta();
			rocketsMeta.setPower(5);
			rocketsMeta.clearEffects();
			rockets.setItemMeta(rocketsMeta);
			
			
			p.getInventory().clear();
			p.getInventory().addItem(stick);
			for (int i = 0; i < 20; i++) {
				p.getInventory().addItem(rockets);
			}
			
			//armor
			ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
			ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
			ItemStack elytra = new ItemStack(Material.ELYTRA);
			ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
			
			HashMap<Enchantment, Integer> enchantments = new HashMap<>(Map.of(Enchantment.DURABILITY, 5,
			                                                                  Enchantment.PROTECTION_ENVIRONMENTAL, 5,
			                                                                  Enchantment.PROTECTION_EXPLOSIONS, 5,
			                                                                  Enchantment.PROTECTION_FALL, 5,
			                                                                  Enchantment.PROTECTION_FIRE, 5,
			                                                                  Enchantment.PROTECTION_PROJECTILE, 5,
			                                                                  Enchantment.BINDING_CURSE, 1,
			                                                                  Enchantment.VANISHING_CURSE, 1));
			
			//boots
			boots.addUnsafeEnchantments(enchantments);
			//leggings
			leggings.addUnsafeEnchantments(enchantments);
			//elytra
			elytra.addUnsafeEnchantments(enchantments);
			//helmet
			helmet.addUnsafeEnchantments(enchantments);
			
			p.getInventory().setBoots(boots);
			p.getInventory().setLeggings(leggings);
			p.getInventory().setChestplate(elytra);
			p.getInventory().setHelmet(helmet);
			
			p.teleportAsync(new Location(Bukkit.getWorld(config.getString("border.world", "world")), 0.0, 0, 0).toHighestLocation(HeightMap.WORLD_SURFACE), PlayerTeleportEvent.TeleportCause.PLUGIN);
			
			if(!b) hunters.add(p);
		} else {
			p.teleportAsync(new Location(Bukkit.getWorld(config.getString("border.world", "world")), 0.0, 0, 0).toHighestLocation(HeightMap.WORLD_SURFACE), PlayerTeleportEvent.TeleportCause.PLUGIN);
			p.setGameMode(GameMode.SPECTATOR);
		}
		alive.remove(p);
	}
	
	public static void stop(Player player) {
		try {
			pregraceManager.cancel();
			graceManager.cancel();
			gameManager.cancel();
			endmanager.cancel();
		} catch (IllegalStateException ignored) {
		}
		
		Bukkit.getOnlinePlayers().forEach((p) -> p.teleportAsync(player.getLocation()));
		announce("Victory: " + player.getName());
		
		new BukkitRunnable() {
			@Override
			public void run() {
				announce("Restarting server in 20 seconds");
			}
		}.runTaskLater(plugin, 7 * 20L);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.kick(Component.text(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Game ended, watch Rays stream for info..."));
				}
				Bukkit.getServer().spigot().restart();
			}
		}.runTaskLater(plugin, 27 * 20L);
	}
	
	private static Location highestRandom(Integer max) {
		SecureRandom rnd = new SecureRandom();
		Location loc = new Location(Bukkit.getWorld("world"), rnd.nextInt(-max, max), 0, rnd.nextInt(-max, max));
		if(!loc.toHighestLocation(HeightMap.WORLD_SURFACE).getBlock().isSolid()) {
			return highestRandom(max);
		} else {
			return loc.toHighestLocation(HeightMap.WORLD_SURFACE);
		}
	}
	
	private static Location center(Location l) {
		return new Location(l.getWorld(), l.getBlockX() + .5, l.getBlockY() + .5, l.getBlockZ() + .5);
	}
	
	@Override
	public void onEnable() {
		update();
		// Plugin startup logic
		saveDefaultConfig();
		plugin = this;
		config = getConfig();
		Objects.requireNonNull(getCommand("start")).setExecutor(new StartCommand());
		Objects.requireNonNull(getCommand("tag")).setExecutor(new TagCommand());
	}
	
	private void update() {
		try {
			URL url = new URL("https://api.github.com/repos/IToncek/RaysHideAndSeek/releases/latest");
			Scanner sc = new Scanner(url.openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.next());
			}
			sc.close();
			JSONObject versions = new JSONObject(sb.toString());
			AtomicReference<String> versionData = new AtomicReference<>("");
			AtomicReference<String> downloadLink = new AtomicReference<>("");
			
			versions.getJSONArray("assets").forEach(o -> {
				JSONObject obj = (JSONObject) o;
				if(obj.getString("name").equals("pom.properties")) {
					versionData.set(obj.getString("browser_download_url"));
				} else if(obj.getString("name").endsWith(".jar")) {
					downloadLink.set(obj.getString("browser_download_url"));
				}
			});
			
			URL url1 = new URL(versionData.get());
			Properties props = new Properties();
			props.load(url1.openStream());
			
			String version = (String) props.get("version");
			Bukkit.getLogger().info(version);
			Bukkit.getLogger().info(getDescription().getVersion());
			if(!version.equals(getDescription().getVersion())) {
				if(!new File("./plugins/HideAndRaySeeksYou-" + version + ".jar").exists()) {
					if(new File("./plugins/HideAndRaySeeksYou-" + getDescription().getVersion() + ".jar").exists()) {
						new File("./plugins/HideAndRaySeeksYou-" + getDescription().getVersion() + ".jar").deleteOnExit();
					} else {
						Bukkit.getLogger().info("Didn't find the old plugin file");
					}
					try (FileOutputStream fileOutputStream = new FileOutputStream("./plugins/HideAndRaySeeksYou-" + version + ".jar")) {
						fileOutputStream.getChannel().transferFrom(Channels.newChannel(new URL(downloadLink.get()).openStream()), 0, Long.MAX_VALUE);
						getServer().spigot().restart();
					}
				}
			}
		} catch (Exception e) {
			Bukkit.getLogger().throwing("HideAndRaySeeksYou", "update()", e);
		}
	}
	
	@Override
	public void onDisable() {
		try {
			pregraceManager.cancel();
			graceManager.cancel();
			gameManager.cancel();
			endmanager.cancel();
		} catch (IllegalStateException ignored) {
		}
		// Plugin shutdown logic
	}
	
	
}
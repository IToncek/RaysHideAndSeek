package me.itoncek.rays.hideandrayseeksyou;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Objects;

import static me.itoncek.rays.hideandrayseeksyou.HideAndRaySeeksYou.*;

public class KOListener implements Listener {
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player) {
			if(event.getEntity() instanceof Player) {
				if(hunters.contains((Player) event.getDamager())) {
					addHunter((Player) event.getEntity());
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if(config.getBoolean("border.eliminates", true)) {
			if(event.getEntity() instanceof Player) {
				if(event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
					if(hunters.contains((Player) event.getEntity())) {
						double size = Objects.requireNonNull(Bukkit.getWorld(config.getString("border.world", "world"))).getWorldBorder().getSize() / 2;
						if((event.getEntity().getLocation().getX() > size || event.getEntity().getLocation().getX() < size) || (event.getEntity().getLocation().getZ() > size || event.getEntity().getLocation().getZ() < size)) {
							addHunter((Player) event.getEntity());
						}
					}
				}
			}
		}
	}
}

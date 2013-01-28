/*
 * Modifyworld - PermissionsEx ruleset plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.modifyworld.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.plugin.Plugin;

import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.modifyworld.PlayerInformer;

/**
 * 
 * @author t3hk0d3
 */
public class VehicleListener extends ModifyworldListener {
	
	public VehicleListener(final Plugin plugin, final ConfigurationSection config, final PlayerInformer informer) {
	
		super(plugin, config, informer);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onVehicleDamage(final VehicleDamageEvent event) {
	
		if (!(event.getAttacker() instanceof Player)) {
			return;
		}
		
		final Player player = (Player) event.getAttacker();
		if (permissionDenied(player, "modifyworld.vehicle.destroy", event.getVehicle())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onVehicleEnter(final VehicleEnterEvent event) {
	
		if (!(event.getEntered() instanceof Player)) {
			return;
		}
		
		final Player player = (Player) event.getEntered();
		if (permissionDenied(player, "modifyworld.vehicle.enter", event.getVehicle())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onVehicleEntityCollision(final VehicleEntityCollisionEvent event) {
	
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		
		final Player player = (Player) event.getEntity();
		if (_permissionDenied(player, "modifyworld.vehicle.collide", event.getVehicle())) {
			event.setCancelled(true);
			event.setCollisionCancelled(true);
			event.setPickupCancelled(true);
		}
	}
}

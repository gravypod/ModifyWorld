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
package ru.tehkode.modifyworld;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * 
 * @author t3hk0d3
 */
public abstract class ModifyworldListener implements Listener {
	
	protected PlayerInformer informer;
	
	protected ConfigurationSection config;
	
	protected boolean informPlayers = false;
	
	protected boolean useMaterialNames = true;
	
	protected boolean checkMetadata = false;
	
	protected boolean checkItemUse = false;
	
	protected boolean enableWhitelist = false;
	
	public ModifyworldListener(final Plugin plugin, final ConfigurationSection config, final PlayerInformer informer) {
	
		this.informer = informer;
		this.config = config;
		
		registerEvents(plugin);
		
		informPlayers = config.getBoolean("informPlayers", informPlayers);
		
		useMaterialNames = config.getBoolean("use-material-names", useMaterialNames);
		
		checkMetadata = config.getBoolean("check-metadata", checkMetadata);
		
		checkItemUse = config.getBoolean("item-use-check", checkItemUse);
		
		enableWhitelist = config.getBoolean("whitelist", enableWhitelist);
		
	}
	
	private String getEntityName(final Entity entity) {
	
		if (entity instanceof ComplexEntityPart) {
			return getEntityName(((ComplexEntityPart) entity).getParent());
		}
		
		String entityName = formatEnumString(entity.getType().toString());
		
		if (entity instanceof Item) {
			entityName = getItemPermission(((Item) entity).getItemStack());
		}
		
		if (entity instanceof Player) {
			return "player." + ((Player) entity).getName();
		} else if (entity instanceof Tameable) {
			final Tameable animal = (Tameable) entity;
			
			return "animal." + entityName + (animal.isTamed() ? "." + animal.getOwner().getName() : "");
		}
		
		final EntityCategory category = EntityCategory.fromEntity(entity);
		
		if (category == null) {
			return entityName; // category unknown (ender crystal)
		}
		
		return category.getNameDot() + entityName;
	}
	
	private String getInventoryTypePermission(final InventoryType type) {
	
		return formatEnumString(type.name());
	}
	
	private String getMaterialPermission(final Material type) {
	
		return useMaterialNames ? formatEnumString(type.name()) : Integer.toString(type.getId());
	}
	
	private String getMaterialPermission(final Material type, final byte metadata) {
	
		return getMaterialPermission(type) + (metadata > 0 ? ":" + metadata : "");
	}
	
	private String getBlockPermission(final Block block) {
	
		return getMaterialPermission(block.getType(), block.getData());
	}
	
	public String getItemPermission(final ItemStack item) {
	
		return getMaterialPermission(item.getType(), item.getData().getData());
	}
	
	protected boolean permissionDenied(final Player player, final String basePermission, final Object ... arguments) {
	
		final String permission = assemblePermission(basePermission, arguments);
		final boolean isDenied = !has(player, permission);
		
		if (isDenied) {
			informer.informPlayer(player, permission, arguments);
		}
		
		return isDenied;
		
	}
	
	protected boolean _permissionDenied(final Player player, final String permission, final Object ... arguments) {
	
		return !has(player, assemblePermission(permission, arguments));
	}
	
	protected String assemblePermission(final String permission, final Object ... arguments) {
	
		final StringBuilder builder = new StringBuilder(permission);
		
		if (arguments != null) {
			for (final Object obj : arguments) {
				if (obj == null) {
					continue;
				}
				
				builder.append('.');
				builder.append(getObjectPermission(obj));
			}
		}
		
		return builder.toString();
	}
	
	protected String getObjectPermission(final Object obj) {
	
		if (obj instanceof Entity) {
			return getEntityName((Entity) obj);
		} else if (obj instanceof EntityType) {
			return formatEnumString(((EntityType) obj).name());
		} else if (obj instanceof BlockState) {
			return getBlockPermission(((BlockState) obj).getBlock());
		} else if (obj instanceof ItemStack) {
			return getItemPermission((ItemStack) obj);
		} else if (obj instanceof Material) {
			return getMaterialPermission((Material) obj);
		} else if (obj instanceof Block) {
			return getBlockPermission((Block) obj);
		} else if (obj instanceof InventoryType) {
			return getInventoryTypePermission((InventoryType) obj);
		}
		
		return obj.toString();
	}
	
	private void registerEvents(final Plugin plugin) {
	
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private String formatEnumString(final String enumName) {
	
		return enumName.toLowerCase().replace("_", "");
	}
	
	public boolean has(Player player, String node) {
		
		synchronized(player.getServer()) { // TODO: Review 
			
			if (player.isOp())
				return true;
			
			if (player.isPermissionSet(node))
				return player.hasPermission(node);
			
			final String[] parts = node.split("\\.");
			
			final StringBuilder builder = new StringBuilder(node.length());
			
			for (String part : parts) {
				
				builder.append('*');
				
				if (player.hasPermission("-" + builder.toString())) {
					return false;
				}
				
				if (player.hasPermission(builder.toString())) {
					return true;
				}
				
				builder.deleteCharAt(builder.length() - 1);
				
				builder.append(part).append('.');
				
			}
			
			return false;
			
		}
		
	}
	
}

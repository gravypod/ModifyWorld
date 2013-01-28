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
package ru.tehkode.modifyworld.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.modifyworld.PlayerInformer;
import ru.tehkode.modifyworld.handlers.BlockListener;
import ru.tehkode.modifyworld.handlers.EntityListener;
import ru.tehkode.modifyworld.handlers.PlayerListener;
import ru.tehkode.modifyworld.handlers.VehicleListener;

/**
 * 
 * @author t3hk0d3
 */
public class Modifyworld extends JavaPlugin {
	
	protected PlayerInformer informer;
	
	protected File configFile;
	
	protected FileConfiguration config;
	
	@Override
	public void onLoad() {
	
		configFile = new File(getDataFolder(), "config.yml");
	}
	
	@Override
	public void onEnable() {
	
		config = getConfig();
		
		if (!config.isConfigurationSection("messages")) {
			getLogger().severe("Deploying default config");
			initializeConfiguration(config);
		}
		
		informer = new PlayerInformer(config);
		
		registerListeners();
		getLogger().info("Modifyworld enabled!");
		
		saveConfig();
	}
	
	@Override
	public void onDisable() {
	
		config = null;
		
		getLogger().info("Modifyworld successfully disabled!");
	}
	
	protected void initializeConfiguration(final FileConfiguration config) {
	
		// Flags
		config.set("item-restrictions", false);
		config.set("inform-players", false);
		config.set("whitelist", false);
		config.set("use-material-names", true);
		config.set("drop-restricted-item", false);
		config.set("item-use-check", false);
		
		// Messages
		config.set("messages/message-format", PlayerInformer.DEFAULT_MESSAGE_FORMAT);
		config.set("messages/default-message", PlayerInformer.PERMISSION_DENIED);
		
		// Predefined messages
		config.set("messages/modifyworld.login", PlayerInformer.WHITELIST_MESSAGE);
		config.set("messages/modifyworld.items.have", PlayerInformer.PROHIBITED_ITEM);
	}
	
	protected void registerListeners() {
	
		new VehicleListener(this, getConfig(), informer);
		new PlayerListener(this, getConfig(), informer);
		new EntityListener(this, getConfig(), informer);
		new BlockListener(this, getConfig(), informer);
	}
	
	@Override
	public FileConfiguration getConfig() {
	
		if (config == null) {
			reloadConfig();
		}
		
		return config;
	}
	
	@Override
	public void saveConfig() {
	
		try {
			
			config.save(configFile);
			
		} catch (final IOException e) {
			
			getLogger().severe("Failed to save configuration file: " + e.getMessage());
			
		}
		
	}
	
	@Override
	public void reloadConfig() {
	
		config = new YamlConfiguration();
		
		config.options().pathSeparator('/');
		
		try {
			
			config.load(configFile);
			
		} catch (final FileNotFoundException e) {
			
			getLogger().severe("Configuration file not found - deploying default one");
			
			final InputStream defConfigStream = getResource("config.yml");
			
			if (defConfigStream != null) {
				
				try {
					
					config.load(defConfigStream);
					
				} catch (final Exception de) {
					
					getLogger().severe("Default config file is broken. Please tell this to Modifyworld author.");
					
				}
				
			}
			
		} catch (final Exception e) {
			getLogger().severe("Failed to load configuration file: " + e.getMessage());
		}
		
		final InputStream defConfigStream = getResource("config.yml");
		
		if (defConfigStream != null) {
			config.setDefaults(YamlConfiguration.loadConfiguration(defConfigStream));
		}
		
	}
	
}

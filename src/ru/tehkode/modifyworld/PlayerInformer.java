package ru.tehkode.modifyworld;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerInformer {
	
	public final static String PERMISSION_DENIED = "Sorry, you don't have enough permissions";
	
	public final static String WHITELIST_MESSAGE = "You are not allowed to join this server. Goodbye!";
	
	public final static String PROHIBITED_ITEM = "Prohibited item \"%s\" has been removed from your inventory.";
	
	public final static String DEFAULT_MESSAGE_FORMAT = "&f[&2Modifyworld&f]&4 %s";
	
	// Default message format
	protected String messageFormat = PlayerInformer.DEFAULT_MESSAGE_FORMAT;
	
	protected Map<String, String> messages = new HashMap<String, String>();
	
	// Flags
	protected boolean enabled = false;
	
	protected boolean individualMessages = false;
	
	protected String defaultMessage = PlayerInformer.PERMISSION_DENIED;
	
	public PlayerInformer(final ConfigurationSection config) {
	
		enabled = config.getBoolean("inform-players", enabled);
		
		loadConfig(config.getConfigurationSection("messages"));
	}
	
	private void loadConfig(final ConfigurationSection config) {
	
		defaultMessage = config.getString("default-message", defaultMessage);
		
		messageFormat = config.getString("message-format", messageFormat);
		
		individualMessages = config.getBoolean("individual-messages", individualMessages);
		
		importMessages(config);
		
		for (final String permission : config.getKeys(true)) {
			if (!config.isString(permission)) {
				continue;
			}
			
			setMessage(permission, config.getString(permission.replace("/", ".")));
		}
	}
	
	public void setMessage(final String permission, final String message) {
	
		messages.put(permission, message);
	}
	
	public String getMessage(final String permission) {
	
		if (messages.containsKey(permission)) {
			return messages.get(permission);
		}
		
		String perm = permission;
		int index;
		
		while((index = perm.lastIndexOf(".")) != -1) {
			perm = perm.substring(0, index);
			
			if (messages.containsKey(perm)) {
				final String message = messages.get(perm);
				messages.put(permission, message);
				return message;
			}
		}
		
		return defaultMessage;
	}
	
	public String getMessage(final Player player, final String permission) {
	
		return getMessage(permission);
	}
	
	public void informPlayer(final Player player, final String permission, final Object ... args) {
	
		if (!enabled) {
			return;
		}
		
		String message = getMessage(player, permission).replace("$permission", permission);
		
		for (int i = 0; i < args.length; i++) {
			message = message.replace("$" + (i + 1), describeObject(args[i]));
		}
		
		if (message != null && !message.isEmpty()) {
			player.sendMessage(String.format(messageFormat, message).replaceAll("&([a-z0-9])", "\u00A7$1"));
		}
	}
	
	protected String describeObject(final Object obj) {
	
		if (obj instanceof ComplexEntityPart) { // Complex entities
			return describeObject(((ComplexEntityPart) obj).getParent());
		} else if (obj instanceof Item) { // Dropped items
			return describeMaterial(((Item) obj).getItemStack().getType());
		} else if (obj instanceof ItemStack) { // Items
			return describeMaterial(((ItemStack) obj).getType());
		} else if (obj instanceof Entity) { // Entities
			return ((Entity) obj).getType().toString().toLowerCase().replace("_", " ");
		} else if (obj instanceof Block) { // Blocks
			return describeMaterial(((Block) obj).getType());
		} else if (obj instanceof Material) { // Just material
			return describeMaterial((Material) obj);
		}
		
		return obj.toString();
	}
	
	private String describeMaterial(final Material material) {
	
		// TODO: implement data id
		
		if (material == Material.INK_SACK) {
			return "dye";
		}
		
		return material.toString().toLowerCase().replace("_", " ");
	}
	
	// For backward compatibility
	private void importMessages(final ConfigurationSection config) {
	
		// This should NOT be refactored, because it would be stupid :D
		if (config.isString("whitelistMessage")) {
			setMessage("modifyworld.login", config.getString("whitelistMessage"));
			config.set("whitelistMessage", null);
		}
		
		if (config.isString("prohibitedItem")) {
			setMessage("modifyworld.items.have", config.getString("prohibitedItem"));
			config.set("prohibitedItem", null);
		}
		
		if (config.isString("permissionDenied")) {
			setMessage("modifyworld", config.getString("permissionDenied"));
			config.set("permissionDenied", null);
		}
	}
}

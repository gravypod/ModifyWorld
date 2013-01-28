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

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.Plugin;

import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.modifyworld.PlayerInformer;

/**
 * @author t3hk0d3
 */
public class PlayerListener extends ModifyworldListener {
	
	protected boolean checkInventory = false;
	
	protected boolean dropRestrictedItem = false;
	
	public PlayerListener(final Plugin plugin, final ConfigurationSection config, final PlayerInformer informer) {
	
		super(plugin, config, informer);
		
		checkInventory = config.getBoolean("item-restrictions", checkInventory);
		
		dropRestrictedItem = config.getBoolean("drop-restricted-item", dropRestrictedItem);
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerSneak(final PlayerToggleSneakEvent event) {
	
		final Player player = event.getPlayer();
		
		if (event.isSneaking() && _permissionDenied(player, "modifyworld.sneak")) {
			event.setCancelled(true);
			event.getPlayer().setSneaking(false);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerSprint(final PlayerToggleSprintEvent event) {
	
		final Player player = event.getPlayer();
		
		if (event.isSprinting() && _permissionDenied(player, "modifyworld.sprint")) {
			event.setCancelled(true);
			event.getPlayer().setSprinting(false);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(final PlayerLoginEvent event) {
	
		if (!enableWhitelist) {
			return;
		}
		
		final Player player = event.getPlayer();
		
		if (_permissionDenied(player, "modifyworld.login")) {
			// String whiteListMessage = user.getOption("kick-message", worldName, this.whitelistKickMessage);
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, informer.getMessage(player, "modifyworld.login"));
			Logger.getLogger("Minecraft").info("Player \"" + player.getName() + "\" were kicked by Modifyworld - lack of 'modifyworld.login' permission");
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
	
		if (permissionDenied(event.getPlayer(), "modifyworld.usebeds")) {
			event.setCancelled(true);
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
	
		final String bucketName = event.getBucket().toString().toLowerCase().replace("_bucket", ""); // WATER_BUCKET -> water
		if (permissionDenied(event.getPlayer(), "modifyworld.bucket.empty", bucketName)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
	
		String materialName = event.getBlockClicked().getType().toString().toLowerCase().replace("stationary_", ""); // STATIONARY_WATER -> water
		
		if ("air".equals(materialName)) { // This should be milk
			materialName = "milk";
		}
		
		if (permissionDenied(event.getPlayer(), "modifyworld.bucket.fill", materialName)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
	
		if (event.getMessage().startsWith("/tell") && permissionDenied(event.getPlayer(), "modifyworld.chat.private")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		
		if (permissionDenied(event.getPlayer(), "modifyworld.chat")) {
			
			event.setCancelled(true);
			
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
	
		// No inform to avoid spam
		if (_permissionDenied(event.getPlayer(), "modifyworld.items.pickup", event.getItem().getItemStack())) {
			event.setCancelled(true);
		}
		
		checkPlayerInventory(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
	
		if (permissionDenied(event.getPlayer(), "modifyworld.items.drop", event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
		
		checkPlayerInventory(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemHeldChange(final PlayerItemHeldEvent event) {
	
		final Player player = event.getPlayer();
		final ItemStack item = player.getInventory().getItem(event.getNewSlot());
		
		if (item != null && item.getType() != Material.AIR && permissionDenied(player, "modifyworld.items.hold", item)) {
			final int freeSlot = getFreeSlot(player.getInventory());
			
			if (freeSlot != 0) {
				player.getInventory().setItem(freeSlot, item);
			} else {
				player.getWorld().dropItemNaturally(player.getLocation(), item);
			}
			
			player.getInventory().setItem(event.getNewSlot(), new ItemStack(Material.AIR));
		}
		
		checkPlayerInventory(player);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInventoryClick(final InventoryClickEvent event) {
	
		final InventoryHolder holder = event.getInventory().getHolder();
		
		if (holder instanceof Player || // do not track inter-inventory stuff
		event.getRawSlot() >= event.getView().getTopInventory().getSize() || // top inventory only
		event.getSlotType() == InventoryType.SlotType.OUTSIDE || // do not track drop
		event.getSlot() == -999) { // temporary fix for bukkit bug (BUKKIT-2768)
			return;
		}
		
		final ItemStack take = event.getInventory().getItem(event.getSlot());
		
		String action;
		ItemStack item;
		
		if (take == null) {
			action = "put";
			item = event.getCursor();
		} else {
			action = "take";
			item = take;
		}
		
		final Player player = (Player) event.getWhoClicked();
		
		if (permissionDenied(player, "modifyworld.items", action, item, "of", event.getInventory().getType())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInventoryEvent(final InventoryClickEvent event) {
	
		final ItemStack item = event.getCursor();
		
		if (item == null || item.getType() == Material.AIR || event.getSlotType() != InventoryType.SlotType.QUICKBAR) {
			return;
		}
		
		final Player player = (Player) event.getWhoClicked();
		
		final int targetSlot = player.getInventory().getHeldItemSlot();
		
		if (event.getSlot() == targetSlot && permissionDenied(player, "modifyworld.items.hold", item)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
	
		if (checkItemUse) {
			if (permissionDenied(event.getPlayer(), "modifyworld.items.use", event.getPlayer().getItemInHand(), "on.entity", event.getRightClicked())) {
				event.setCancelled(true);
			}
			
			return;
		}
		
		if (permissionDenied(event.getPlayer(), "modifyworld.interact", event.getRightClicked())) {
			event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
	
		final Action action = event.getAction();
		
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) { // item restriction check
			checkPlayerInventory(event.getPlayer());
		}
		
		final Player player = event.getPlayer();
		final ItemStack itemInHand = player.getItemInHand();
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) { //RIGHT_CLICK_AIR is cancelled by default.
			switch(itemInHand.getType()) {
				case POTION: //Only check splash potions.
					if ((itemInHand.getDurability() & 0x4000) != 0x4000) {
						break;
					}
				case EGG:
				case SNOW_BALL:
				case EXP_BOTTLE:
					if (permissionDenied(player, "modifyworld.items.throw", itemInHand)) {
						event.setUseItemInHand(Result.DENY);
						//Denying a potion works fine, but the client needs to be updated because it already reduced the item.
						if (itemInHand.getType() == Material.POTION) {
							event.getPlayer().updateInventory();
						}
					}
					return; // no need to check further
				case MONSTER_EGG: // don't add MONSTER_EGGS here
					if (permissionDenied(player, "modifyworld.spawn", ((SpawnEgg) itemInHand.getData()).getSpawnedType())) {
						event.setUseItemInHand(Result.DENY);
					}
					return; // no need to check further
			}
		}
		
		if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK && action != Action.PHYSICAL) {
			return;
		}
		
		if (checkItemUse && action != Action.PHYSICAL) {
			if (permissionDenied(event.getPlayer(), "modifyworld.items.use", player.getItemInHand(), "on.block", event.getClickedBlock())) {
				event.setCancelled(true);
			}
			
			return;
		}
		
		if (permissionDenied(player, "modifyworld.blocks.interact", event.getClickedBlock())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemEnchant(final EnchantItemEvent event) {
	
		if (permissionDenied(event.getEnchanter(), "modifyworld.items.enchant", event.getItem())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemCraft(final CraftItemEvent event) {
	
		final Player player = (Player) event.getWhoClicked();
		
		if (permissionDenied(player, "modifyworld.items.craft", event.getRecipe().getResult())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onFoodLevelChange(final FoodLevelChangeEvent event) {
	
		final Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
		
		if (player == null) {
			return;
		}
		
		if (_permissionDenied(player, "modifyworld.digestion")) {
			event.setCancelled(true);
		}
	}
	
	protected void checkPlayerInventory(final Player player) {
	
		if (!checkInventory) {
			return;
		}
		
		final Inventory inventory = player.getInventory();
		for (final ItemStack stack : inventory.getContents()) {
			if (stack != null && permissionDenied(player, "modifyworld.items.have", stack)) {
				inventory.remove(stack);
				
				if (dropRestrictedItem) {
					player.getWorld().dropItemNaturally(player.getLocation(), stack);
				}
			}
		}
	}
	
	private int getFreeSlot(final Inventory inventory) {
	
		for (int i = 9; i <= 35; i++) {
			if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
				return i;
			}
		}
		
		return 0;
	}
}

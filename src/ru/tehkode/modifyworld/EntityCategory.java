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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;

public enum EntityCategory {
	PLAYER("player", Player.class.getName()),
	ITEM("item", Item.class.getName()),
	ANIMAL("animal", Animals.class.getName(), Squid.class.getName()),
	MONSTER("monster", Monster.class.getName(), Slime.class.getName(), EnderDragon.class.getName(), Ghast.class.getName()),
	NPC("npc", NPC.class.getName()),
	PROJECTILE("projectile", Projectile.class.getName());
	
	private String name;
	
	private String entityNames[];
	
	private final static Map<String, EntityCategory> map = new HashMap<String, EntityCategory>();
	
	static {
		for (final EntityCategory cat : EntityCategory.values()) {
			for (final String catClass : cat.getClasses()) {
				EntityCategory.map.put(catClass, cat);
			}
		}
	}
	
	private EntityCategory(final String name, final String ... entityNames) {
	
		this.name = name;
		
		this.entityNames = entityNames;
		
	}
	
	public String getName() {
	
		return name;
		
	}
	
	public String getNameDot() {
	
		return getName() + ".";
		
	}
	
	public String[] getClasses() {
	
		return entityNames;
	}
	
	public static EntityCategory fromEntity(final Entity entity) {
	
		String classID = entity.getClass().getName();
		
		for (String entityClass : EntityCategory.map.keySet()) {
			
			if (entityClass == classID) {
				
				return EntityCategory.map.get(entityClass);
				
			}
			
		}
		
		return null;
		
	}
	
}

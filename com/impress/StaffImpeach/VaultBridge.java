package com.impress.StaffImpeach;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

public class VaultBridge {
	public static Permission perms = null;
	public VaultBridge(Server server) {
		setupPermissions(server);
	}
	private boolean setupPermissions(Server server) {
        RegisteredServiceProvider<Permission> rsp = server.getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	String getPrimaryGroup(Player player) {
		return perms.getPrimaryGroup(player);
	}
	String getPrimaryGroup(String player, World world) {
		return perms.getPrimaryGroup(world, player);
	}
	String[] getGroups(Player player) {
		return perms.getPlayerGroups(player);
	}
	String[] getGroups(String player, World world) {
		return perms.getPlayerGroups(world, player);
	}
	
	void removeGroup(String player, String group, World world) {
		perms.playerRemoveGroup(world, player, group);
	}
	
	boolean clearGroups(Player player) {
		if (perms.getPlayerGroups(player) == null) return false;
		for (String group : perms.getPlayerGroups(player))
			perms.playerRemoveGroup(player, group);
		return true;
	}
	
	boolean isGroupGlobal(String player, String group) {
		return perms.playerInGroup((World)null, player, group);
	}
	
	boolean addGroup(Player player, String group) {
		return perms.playerAddGroup(player, group);
	}
	boolean addGroup(String player, String group, World world) {
		return perms.playerAddGroup(world, player, group);
	}
	boolean addGroups(Player player, String[] groups) {
		if (groups == null) return false;
		boolean result = true;
		for (String group : groups)
			if (!addGroup(player, group)) result = false;
		return result;
	}
	boolean addGroups(String player, String[] groups, World world) {
		if (groups == null || world == null) return false;
		boolean result = true;
		for (String group : groups)
			if (!addGroup(player, group, world)) result = false;
		return result;
	}
	
	boolean hasPermission(String player, String permission, World world) {
		return perms.playerHas(world, player, permission);
	}
}
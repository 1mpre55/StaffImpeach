package com.impress.StaffImpeach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StaffImpeach extends JavaPlugin {
	private List<String> groups;
	private HashMap<String, Integer> groupImpRequrement;
	private HashMap<String, List<String>> impeachments;
	private HashMap<String, Integer> staffCounter;
	
	private int impLimit;
	
	private String viewVotesMessage;
	
	private boolean useVault;
	private VaultBridge vault;
	private List<String> cmd;
	private int impToDemote;
	
	private ImpeachLoader iLoader;
	private StaffLoader sLoader;
	
	@Override
	public void onEnable() {
		load();
		getLogger().info(getName() + " enabled");
	}
	@Override
	public void onDisable() {
		cleanUp();
		save();
		getLogger().info(getName() + " disabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("staffimpeach"))
			if (sender instanceof Player) {
				if (args.length < 1)
					return false;
				if (!sender.hasPermission("staffimpeach.impeach")) {
					sender.sendMessage("You don't have permission to use that!");
					return true;
				}
				Player staff = getServer().getPlayer(args[0]);
				String staffName = staff == null? args[0] : staff.getName();
				if (staff == null && (!useVault || !getServer().getOfflinePlayer(args[0]).hasPlayedBefore())) {
					sender.sendMessage("Player " + args[0] + " was not found");
					return true;
				}
				if (!impeachments.containsKey(sender.getName()))
					impeachments.put(sender.getName(), new ArrayList<String>());
				if (impeachments.get(sender.getName()).contains(staffName.toLowerCase())) {
					sender.sendMessage("You already voted to impeach that player");
					return true;
				}
				if (impeachments.get(sender.getName()).size() >= impLimit) {
					sender.sendMessage("You don't have any votes left");
					return true;
				}
				
				World world = ((Player)sender).getWorld();
				if (useVault) {
					if (vault.hasPermission(staffName, "staffimpeach.impeachable", world) &&
							impeach(sender.getName().toLowerCase(), staffName.toLowerCase(), world)) {
						sender.sendMessage("You voted to impeach " + staffName);
						return true;
					}
				} else
					if (staff != null && staff.hasPermission("staffimpeach.impeachable") && impeach(sender.getName().toLowerCase(), staffName.toLowerCase(), null)) {
						sender.sendMessage("You voted to impeach " + staffName);
						return true;
					}
				sender.sendMessage("You cannot impeach that player");
				return true;
			} else {
				sender.sendMessage("Only players can use this command");
				return true;
			}
		else if (command.getName().equalsIgnoreCase("staffimpeachvotes")) {
			String player;
			String name;
			String group = null;
			if ((args.length == 0 || args[0].equalsIgnoreCase(sender.getName())) && sender instanceof Player)
				if (sender.hasPermission("staffimpeach.viewvotes.self")) {
					player = sender.getName();
					name = "you";
					if (!sender.hasPermission("staffimpeach.impeachable") ||
							(useVault && (group = getGroupBestMatch(player, ((Player)sender).getWorld())) == null)) {
						sender.sendMessage(name + " cannot be impeached");
						return true;
					}
				} else {
					sender.sendMessage("You don't have permission to use that!");
					return true;
				}
			else {
				if (sender.hasPermission("staffimpeach.viewvotes.others")) {
					Player p = getServer().getPlayer(args[0]);
					name = player = p == null? args[0] : p.getName();
					if ((p != null && !p.hasPermission("staffimpeach.impeachable")) || (p == null && useVault &&
							vault.hasPermission(player, "staffimpeach.impeachable", sender instanceof Player? ((Player)sender).getWorld() : null)) ||
							(useVault && (group = getGroupBestMatch(player, ((Player)sender).getWorld())) == null)) {
						// Known BUG: if use-vault is true, the command is sent from the console AND if staffimpeach.impeachable
						// is a world-specific permission this will execute even though it shouldn't
						sender.sendMessage(name + " cannot be impeached");
						return true;
					}
				} else {
					sender.sendMessage("You don't have permission to use that!");
					return true;
				}
			}
			if (useVault && group == null)
				group = getGroupBestMatch(player, sender instanceof Player? ((Player)sender).getWorld() : null);
			int votes = staffCounter.containsKey(player)? staffCounter.get(player) : 0;
			int votesReq = group == null? impToDemote : groupImpRequrement.get(group).intValue();
			sender.sendMessage(viewVotesMessage
					.replaceAll("<PLAYER>", name)
					.replaceAll("<VOTES>", Integer.toString(votes))
					.replaceAll("<VOTESREQ>", Integer.toString(votesReq))
					.replaceAll("<VOTESLEFT>", Integer.toString(votesReq = votes)));
			return true;
		}
		else
			return false;
	}
	
	private boolean impeach(String impeacher, String impeached, World world) {
		if (impeachments.containsKey(impeacher))
			impeachments.get(impeacher).add(impeached);
		else {
			List<String> list = new ArrayList<String>();
			list.add(impeached);
			impeachments.put(impeacher, list);
		}
		if (staffCounter.containsKey(impeached))
			staffCounter.put(impeached, staffCounter.get(impeached) + 1);
		else
			staffCounter.put(impeached, 1);
		
		if (useVault) {
			String group = getGroupBestMatch(impeached, world);
			if (group == null || !groupImpRequrement.containsKey(group))
				return false;
			if (groupImpRequrement.get(group) <= staffCounter.get(impeached)) {
				boolean isGlobal = vault.isGroupGlobal(impeached, group);
				int index = groups.lastIndexOf(group) + 1;
				if (index < groups.size())
					vault.addGroup(impeached, groups.get(index), isGlobal? null : world);
				vault.removeGroup(impeached, group, isGlobal? null : world);
				staffCounter.put(impeached, staffCounter.get(impeached) - groupImpRequrement.get(group));
			}
			return true;
		} else {
			if (impToDemote < staffCounter.get(impeached)) {
				for (String c : cmd.toArray(new String[cmd.size()]))
					getServer().dispatchCommand(getServer().getConsoleSender(), c.replaceAll("<IMPEACHED_PLAYER>", impeached));
				staffCounter.put(impeached, staffCounter.get(impeached) - impToDemote);
			}
			return true;
		}
	}
	
	private void load() {
		List<String> list;
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		
		useVault = config.getBoolean("use-vault");
		
		if (useVault && !getServer().getPluginManager().isPluginEnabled("Vault")) {
			getLogger().warning("Vault not found, falling back to demote commands");
			useVault = false;
		}
		
		if (useVault && config.isList("groups") && !(list = config.getStringList("groups")).isEmpty()) {
			groups = new ArrayList<String>();
			groupImpRequrement = new HashMap<String, Integer>();
			vault = new VaultBridge(getServer());
			for (int i = 0; i < list.size(); i++)
				try {
					String[] group = list.get(i).split(",", 2);
					group[0] = group[0].trim();
					if (groups.contains(group[0])) {
						groups.remove(group[0]);
						groupImpRequrement.remove(group[0]);
					}
					groupImpRequrement.put(group[0], Math.max(Integer.parseInt(group[1].trim()), 1));
					groups.add(group[0]);
				} catch (IllegalArgumentException | NullPointerException e) {
					getLogger().warning("Error reading group from config, line: " + list.get(i));
				}
		} else {
			if (useVault) {
				getLogger().warning("Permission groups not specified, falling back to demote commands");
				useVault = false;
			}
			cmd = config.getStringList("impeach-commands");
			impToDemote = Math.max(config.getInt("votes-to-impeach"), 1);
		}
		impLimit = Math.max(config.getInt("votes-limit", 5), 0);
		
		viewVotesMessage = config.getString("view-votes-message", "");
		
		iLoader = new ImpeachLoader(this, "votes.yml");
		impeachments = iLoader.load(true);
		
		sLoader = new StaffLoader(this, "staff.yml");
		staffCounter = sLoader.load(true);
		
		for (List<?> impeacher : impeachments.values().toArray(new List<?>[0]))
			for (String staffPlayer : impeacher.toArray(new String[0]))
				if (!staffCounter.containsKey(staffPlayer))
					staffCounter.put(staffPlayer, 0);
	}
	private void save() {
		iLoader.save(impeachments);
		sLoader.save(staffCounter);
	}
	
	private String getGroupBestMatch(String player, World world) {
		if (useVault) {
			String mainGroup = vault.getPrimaryGroup(player, world);
			if (mainGroup != null)
				for (String group : groups.toArray(new String[groups.size()]))
					if (mainGroup.equals(group))
						return mainGroup;
		}
		return getHighestGroup(vault.getGroups(player, world));
	}
	private String getHighestGroup(String[] groups) {
		if (this.groups == null) return null;
		for (String g1 : this.groups.toArray(new String[this.groups.size()]))
			for (String g2 : groups)
				if (g1.equals(g2))
					return g2;
		return null;
	}
	
	private void cleanUp() {
		if (impeachments != null)
			for (String player : impeachments.keySet().toArray(new String[0]))
				if (!getServer().getOfflinePlayer(player).hasPlayedBefore())
					impeachments.remove(player);
		if (staffCounter != null)
			for (String player : staffCounter.keySet().toArray(new String[0]))
				if (!getServer().getOfflinePlayer(player).hasPlayedBefore())
					staffCounter.remove(player);
	}
}
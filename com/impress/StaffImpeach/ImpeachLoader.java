package com.impress.StaffImpeach;

import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public class ImpeachLoader extends ConfigManager {

	public ImpeachLoader(StaffImpeach plugin, String fileName) {
		super(plugin, fileName);
	}
	
	public HashMap<String, List<String>> load(boolean reload) {
		if (reload) reloadYaml();
		FileConfiguration config = getConfig();
		
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
		for (String player : config.getKeys(false).toArray(new String[0]))
			if (config.isList(player))
				result.put(player, config.getStringList(player));
		
		return result;
	}
	public void save(HashMap<String, List<String>> data) {
		FileConfiguration config = getConfig();
		clearConfigurationSection(config, true);
		
		for (String player : data.keySet().toArray(new String[data.size()]))
			config.set(player, data.get(player));
		
		saveYaml();
		
		if (data.isEmpty())
			configFile.delete();
	}
}
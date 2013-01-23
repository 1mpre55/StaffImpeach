package com.impress.StaffImpeach;

import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;

public class StaffLoader extends ConfigManager {

	public StaffLoader(StaffImpeach plugin, String fileName) {
		super(plugin, fileName);
	}
	
	public HashMap<String, Integer> load(boolean reload) {
		if (reload) reloadYaml();
		FileConfiguration config = getConfig();
		
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (String staff : config.getKeys(false).toArray(new String[0])) {
			if (config.isInt(staff))
				result.put(staff, config.getInt(staff));
		}
		
		return result;
	}
	public void save(HashMap<String, Integer> data) {
		FileConfiguration config = getConfig();
		clearConfigurationSection(config, true);
		
		for (String player : data.keySet().toArray(new String[data.size()]))
			config.set(player, data.get(player));
		
		saveYaml();
		
		if (data.isEmpty())
			configFile.delete();
	}
}
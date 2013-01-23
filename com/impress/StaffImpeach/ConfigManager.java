package com.impress.StaffImpeach;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	protected StaffImpeach plugin;
	protected String fileName;
	protected File configFile;
	protected FileConfiguration fileConfiguration;
	
	public ConfigManager(StaffImpeach plugin, String fileName) {
		if (plugin == null)
			throw new IllegalArgumentException("plugin cannot be null");
		if (!plugin.isInitialized())
			throw new IllegalArgumentException("plugin must be initiaized");
		this.plugin = plugin;
		this.fileName = fileName;
		configFile = new File(plugin.getDataFolder(), this.fileName);
	}
	
	FileConfiguration getConfig() {
		if (fileConfiguration == null)
			reloadYaml();
		return fileConfiguration;
	}
	void reloadYaml() {
		if (configFile == null) {
			File dataFolder = plugin.getDataFolder();
			if (dataFolder == null)
				throw new IllegalStateException();
			configFile = new File(dataFolder, fileName);
		}
		fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

		// Look for defaults in the jar
		InputStream defConfigStream = plugin.getResource(fileName);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			fileConfiguration.setDefaults(defConfig);
		}
	}
	void saveYaml() {
		if (fileConfiguration == null || configFile == null)
			return;
		else
			try {
				getConfig().save(configFile);
			} catch (IOException ex) {
				plugin.getLogger().severe("Could not save " + configFile.getName());
			}
	}
	public void saveDefaultYaml() {
		if (!configFile.exists())
			plugin.saveResource(fileName, false);
	}
	
	protected static ConfigurationSection getConfigurationSection(ConfigurationSection parent, String section) {
		if (parent.isConfigurationSection(section))
			return parent.getConfigurationSection(section);
		else return parent.createSection(section);
	}
	protected static void clearConfigurationSection(ConfigurationSection config, boolean deep) {
		String[] keys = Tools.reverseList(Tools.orderByOccurrence(config.getKeys(deep).toArray(new String[0]), '.')).toArray(new String[0]);
		for (String key : keys)
			config.set(key, null);
	}
}
package di.internal.controller;

import java.io.File;

import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;

public interface InternalController {

	/**
	 * @return The driver for the plugin configuration file.
	 */
	ConfigManager getConfigManager();

	/**
	 * @return The driver for the plugin lang file.
	 */
	YamlManager getLangManager();
	
	/**
	 * @return Plugin folder.
	 */
	File getDataFolder();
}

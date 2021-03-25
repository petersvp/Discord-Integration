package di.internal.controller.impl;

import java.io.File;

import org.bukkit.plugin.Plugin;

import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.controller.file.impl.ConfigManagerImpl;
import di.internal.controller.file.impl.FileControllerSpigotImpl;
import di.internal.controller.file.impl.YamlManagerImpl;
import lombok.Getter;

/**
 * This controller is in charge of configuring and obtaining the default files
 * of the plugin.
 */
@Getter
public class InternalControllerSpigotImpl implements InternalController {

	/**
	 * The driver for the plugin configuration file.
	 */
	private ConfigManager configManager;

	/**
	 * The driver for the plugin lang file.
	 */
	private YamlManager langManager;

	/**
	 * Path of the plugin folder.
	 */
	private File dataFolder;

	/**
	 * Main Class Constructor.
	 * 
	 * @param plugin         Bukkit plugin.
	 * @param coreController Core controller.
	 * @param classLoader    Class loader.
	 */
	public InternalControllerSpigotImpl(Plugin plugin, CoreController coreController, ClassLoader classLoader) {
		this.dataFolder = getInternalPluginDataFolder(plugin.getName(), coreController);
		FileControllerSpigotImpl fileController = new FileControllerSpigotImpl(plugin);
		this.configManager = new ConfigManagerImpl(fileController, dataFolder);
		this.langManager = new YamlManagerImpl(fileController, "lang.yml", dataFolder, classLoader);
	}

	/**
	 * Gets the plugin folder located in the DI folder
	 * 
	 * @param name           Bukkit plugin name.
	 * @param coreController Core controller.
	 * @return Plugin folder.
	 */
	private File getInternalPluginDataFolder(String name, CoreController coreController) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.valueOf(coreController.getDataFolder().getAbsolutePath()));
		stringBuilder.append("/");
		stringBuilder.append(name);
		return new File(stringBuilder.toString());
	}
}

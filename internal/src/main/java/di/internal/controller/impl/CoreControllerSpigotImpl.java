package di.internal.controller.impl;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import di.internal.controller.CoreController;
import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.controller.file.impl.ConfigManagerImpl;
import di.internal.controller.file.impl.FileControllerSpigotImpl;
import di.internal.controller.file.impl.YamlManagerImpl;
import di.internal.entity.DiscordBot;
import di.internal.exception.NoApiException;
import lombok.Getter;

/**
 * Internal Controller of Plugin.
 */
@Getter
public class CoreControllerSpigotImpl implements CoreController {

	/**
	 * The driver for the plugin configuration file.
	 */
	private ConfigManager configManager;

	/**
	 * The driver for the plugin lang file.
	 */
	private YamlManager langManager;

	/**
	 * The bukkit plugin.
	 */
	private Plugin plugin;

	/**
	 * Contains the bot config information.
	 */
	private DiscordBot bot;

	/**
	 * Path of the main plugin folder.
	 */
	private File dataFolder;

	/**
	 * Main constructor;
	 * 
	 * @param plugin      Bukkit plugin.
	 * @param classLoader Class loader.
	 * @throws NoApiException
	 */
	public CoreControllerSpigotImpl(Plugin plugin, ClassLoader classLoader) {
		this.plugin = plugin;
		this.dataFolder = plugin.getDataFolder();
		FileControllerSpigotImpl fileController = new FileControllerSpigotImpl(plugin);
		this.configManager = new ConfigManagerImpl(fileController, plugin.getDataFolder());
		this.langManager = new YamlManagerImpl(fileController, "lang.yml", plugin.getDataFolder(), classLoader);
		this.bot = initBot();
	}

	/**
	 * Init Discord Bot.
	 * 
	 * @return Finished bot object.
	 */
	private DiscordBot initBot() {
		String token = configManager.getString("bot_token");
		long serverid = configManager.getLong("discord_server_id");
		String prefix = configManager.getString("discord_server_prefix");

		if (token == null || prefix == null || serverid == 0L) {
			this.plugin.getLogger().log(Level.SEVERE,
					"Failed to load the data required to start the bot. Did you enter the server ID, token and prefix correctly?");
			this.plugin.getPluginLoader().disablePlugin((Plugin) this);
		}

		this.plugin.getLogger().info("Starting Bot");
		return new DiscordBot(prefix, serverid, token, plugin.getLogger());
	}

}
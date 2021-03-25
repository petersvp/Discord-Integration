package di.internal.controller.impl;

import java.io.File;
import java.util.logging.Level;

import di.internal.controller.CoreController;
import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.controller.file.impl.ConfigManagerImpl;
import di.internal.controller.file.impl.FileControllerBungeeImpl;
import di.internal.controller.file.impl.YamlManagerImpl;
import di.internal.entity.DiscordBot;
import di.internal.exception.NoApiException;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;

/**
 * Internal Controller of Plugin.
 */
@Getter
public class CoreControllerBungeeImpl implements CoreController {

	/**
	 * The driver for the plugin configuration file.
	 */
	private ConfigManager configManager;

	/**
	 * The driver for the plugin lang file.
	 */
	private YamlManager langManager;

	/**
	 * The bungee proxy.
	 */
	private ProxyServer proxyServer;

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
	public CoreControllerBungeeImpl(ProxyServer proxyServer, ClassLoader classLoader) {
		try {
		this.proxyServer = proxyServer;
		this.dataFolder = new File(proxyServer.getPluginsFolder().getAbsolutePath() + "/DICore");
		FileControllerBungeeImpl fileController = new FileControllerBungeeImpl(proxyServer);
		this.configManager = new ConfigManagerImpl(fileController, dataFolder);
		this.langManager = new YamlManagerImpl(fileController, "lang.yml", dataFolder, classLoader);
		this.bot = initBot();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			this.proxyServer.getLogger().log(Level.SEVERE,
					"Failed to load the data required to start the bot. Did you enter the server ID, token and prefix correctly?");
			System.exit(-1);
		}

		this.proxyServer.getLogger().info("Starting Bot");
		return new DiscordBot(prefix, serverid, token, proxyServer.getLogger());
	}

}
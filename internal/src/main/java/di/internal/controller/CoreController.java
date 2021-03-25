package di.internal.controller;

import java.io.File;

import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.entity.DiscordBot;
import net.dv8tion.jda.api.JDA;

public interface CoreController {

	/**
	 * @return The driver for the plugin configuration file.
	 */
	ConfigManager getConfigManager();

	/**
	 * @return The driver for the plugin lang file.
	 */
	YamlManager getLangManager();


	/**
	 * @return Bot config information.
	 */
	DiscordBot getBot();

	/**
	 * @return Plugin folder.
	 */
	File getDataFolder();
	
	/**
	 * @return Discord Api.
	 */
	default JDA getDiscordApi() {
		return getBot().getApi();
	}

}

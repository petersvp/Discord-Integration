package di.dicore;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import di.internal.controller.CoreController;

public class BukkitApplication extends JavaPlugin {

	private static CoreController internalController;

	@Override
	public void onEnable() {
		getLogger().info("Plugin started");
		Plugin plugin = getPlugin(getClass());
		internalController = new CoreController(plugin, this.getClassLoader());
		BotStatus.init();
	}

	@Override
	public void onDisable() {
		internalController.getBot().getApi().shutdownNow();
		getLogger().info("Plugin disabled");
	}

	public static CoreController getInternalController() {
		return internalController;
	}

}

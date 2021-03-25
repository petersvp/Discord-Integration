package di.dicore;

import di.internal.controller.CoreController;
import di.internal.controller.impl.CoreControllerBungeeImpl;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeApplication extends Plugin {
	
	private static CoreController internalController;
	
	@Override
    public void onEnable() {
		getLogger().info("Plugin started");
		internalController = new CoreControllerBungeeImpl(getProxy(), this.getClass().getClassLoader());
    }

	public static CoreController getInternalController() {
		return internalController;
	}
}

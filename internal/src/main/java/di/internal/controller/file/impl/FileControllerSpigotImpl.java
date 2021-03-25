package di.internal.controller.file.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import di.internal.controller.file.FileController;

public class FileControllerSpigotImpl implements FileController {

	private Plugin plugin;

	public FileControllerSpigotImpl(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public InputStream getResource(String filename) {
		if (filename == null)
			throw new IllegalArgumentException("Filename cannot be null");
		try {
			URL url = plugin.getClass().getClassLoader().getResource(filename);
			if (url == null)
				return null;
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			return connection.getInputStream();
		} catch (IOException ex) {
			return null;
		}
	}

	@Override
	public Logger getLogger() {
		return plugin.getLogger();
	}
}

package di.internal.controller.file.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import di.internal.controller.file.FileController;
import net.md_5.bungee.api.ProxyServer;

public class FileControllerBungeeImpl implements FileController {

	private ProxyServer proxyServer;

	public FileControllerBungeeImpl(ProxyServer proxyServer) {
		this.proxyServer = proxyServer;
	}

	@Override
	public InputStream getResource(String filename) {
		if (filename == null)
			throw new IllegalArgumentException("Filename cannot be null");
		try {
			URL url = proxyServer.getClass().getClassLoader().getResource(filename);
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
		return proxyServer.getLogger();
	}
}

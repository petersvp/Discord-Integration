package di.internal.controller.file.impl;

import java.io.File;
import java.util.Map;

import di.internal.controller.file.FileController;
import di.internal.controller.file.YamlManager;

/**
 * Language file driver.
 */
public class YamlManagerImpl implements YamlManager {

	/**
	 * Name of the file to be controlled.
	 */
	private String fileName;

	/**
	 * File configuration.
	 */
	private File customConfigFile;

	/**
	 * Map of the data obtained from the yaml.
	 */
	private Map<String, Object> yamlData;

	/**
	 * Main constructor.
	 * 
	 * @param controller  Plugin driver
	 * @param filename    The name of the file
	 * @param dataFolder  The folder where it is located
	 * @param classLoader Class loader.
	 */
	public YamlManagerImpl(FileController controller, String filename, File dataFolder,
			ClassLoader classLoader) {
		this.fileName = filename;
		this.customConfigFile = new File(dataFolder, this.fileName);
		if (!this.customConfigFile.exists()) {
			this.customConfigFile.getParentFile().mkdirs();
			controller.saveResource(dataFolder, filename, false);
		}

		this.yamlData = getYamlContent(fileName, this.customConfigFile, classLoader);

	}

	@Override
	public String getString(String path) {
		try {
			char specialChar = (char) 167;
			return yamlData.get(path).toString().replace('&', specialChar);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}
}
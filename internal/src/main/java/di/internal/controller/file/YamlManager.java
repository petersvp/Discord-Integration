package di.internal.controller.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import di.internal.utils.Utils;

public interface YamlManager {

	/**
	 * @param path The value you want to obtain
	 * @return The content of the sought value.
	 */
	String getString(String path);

	@SuppressWarnings("unchecked")
	default Map<String, Object> getYamlContent(String fileName, File customFile, ClassLoader classLoader) {
		try {
			InputStream file = Utils.getFileFromResourceAsStream(classLoader, fileName);
			Map<String, Object> custom = (Map<String, Object>) new Yaml().load(new FileInputStream(customFile));
			Map<String, Object> original = (Map<String, Object>) new Yaml().load(file);
			if (original == null)
				return null;

			original.forEach((path, content) -> {
				if (!custom.containsKey(path))
					custom.put(path, content);
			});
			original.clear();
			file.close();
			return custom;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

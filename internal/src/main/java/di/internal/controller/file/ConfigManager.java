package di.internal.controller.file;

public interface ConfigManager {
	
	public String getString(String path);

	public long getLong(String path);

	public boolean getBoolean(String path);

	public int getInt(String path);
}

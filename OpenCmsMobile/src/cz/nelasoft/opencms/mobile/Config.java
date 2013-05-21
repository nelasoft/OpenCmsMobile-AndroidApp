package cz.nelasoft.opencms.mobile;

import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

public class Config {

	private static String configUrl;
	private static String configContext;
	private static int splashDisplayLength;

	private static String defaultConfigJson = "";

	private static JSONObject mainConfig;

	public static void initConfig(Resources resources) throws Exception {
		Properties prop = new Properties();
		prop.load(resources.openRawResource(R.raw.config));
		configUrl = prop.getProperty("configUrl");
		configContext = prop.getProperty("configContext");
		splashDisplayLength = Integer.parseInt(prop.getProperty("splashDisplayLength"));
	}

	public static JSONObject getMainConfig() {
		return mainConfig;
	}

	public static String getConfigUrl() {
		return configUrl;
	}

	public static String getConfigContext() {
		return configContext;
	}

	public static String getDefaultConfigJson() {
		return defaultConfigJson;
	}

	public static int getSplashDisplayLength() {
		return splashDisplayLength;
	}

	public static void setDefaultConfigJson(String defaultConfigJson) throws JSONException {
		mainConfig = new JSONObject(defaultConfigJson);
		mainConfig.getJSONObject("MobileApplication");
	}
}

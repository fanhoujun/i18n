package storage.tools_i18n.util;

import java.io.File;
import java.util.Properties;

import storage.tools_i18n.model.Country;

public class Configuration {

	private static final String CONFIG_FILE = "config.na.properties";// na

	// private static final String CONFIG_FILE="config.storage.properties";//stroage

	public static final String SHEET_METADATA_NAME = "meta-data";

	public static final String METADATA_FILE;

	public static final String EXPORT_EXCEL_NAME;

	public static final String DEFAULT_BRANCH;

	public static final String GIT_URL;

	public static final String TRANSLATED_SPREADSHEET;

	public static final String METADATA_CREATE_BY;

	static {

		Properties pps = new Properties();
		try {
			pps.load(Configuration.class.getClassLoader()
					.getResourceAsStream(CONFIG_FILE));
		} catch (Exception e) {
			throw new RuntimeException("Read config.properties failed: " + e);
		}

		GIT_URL = pps.getProperty("GIT_URL");
		METADATA_FILE = pps.getProperty("METADATA_FILE");
		EXPORT_EXCEL_NAME = pps.getProperty("EXPORT_EXCEL_NAME");
		DEFAULT_BRANCH = pps.getProperty("DEFAULT_BRANCH");
		String translated = pps.getProperty("TRANSLATED_SPREADSHEET");

		if (new File(translated).exists()) {
			TRANSLATED_SPREADSHEET = translated;
		} else {
			TRANSLATED_SPREADSHEET = GIT_URL + File.separator + translated;
		}
		METADATA_CREATE_BY = pps.getProperty("METADATA_CREATE_BY");

		readCountries(pps);
	}

	private static void readCountries(Properties pps) {
		Country.ENGLISH = new Country("ENGLISH_US", pps.getProperty("en_us"));
		for (int i = 0; i < 400; i++) {
			String property = pps.getProperty("local" + i);
			if (StringUtil.isEmpty(property)) {
				continue;
			}
			String[] ss = property.split(",");
			Country.addLocal(ss[0].trim(), ss[1].trim());
		}
	}

}
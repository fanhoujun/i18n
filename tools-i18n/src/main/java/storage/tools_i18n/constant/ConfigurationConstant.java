package storage.tools_i18n.constant;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigurationConstant {
	
	private static Logger log = Logger.getLogger(ConfigurationConstant.class.getName());
	
	public static String SHEET_METADATA_NAME;

	public static String METADATA_FILE;
	
	public static String EXPORT_EXCEL_NAME;
	
	public static String DEFAULT_BRANCH;
	
	public static String GIT_URL;
	
	public static String SHEET_STORAGE_NAME;

	public static String TRANSLATED_SPREADSHEET;
	
	public static String METADATA_CREATE_BY;
	
	static {
		String propName = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
		propName = propName.substring(0, propName.indexOf("target")) 
				+ File.separator + "src" 
				+ File.separator + "main" 
				+ File.separator + "resources" 
				+ File.separator + "config.properties";
		System.out.println(propName);
		Properties pps = new Properties();
		try {
			pps.load(new FileInputStream(propName));
		} catch (Exception e) {
			log.severe("Read config.properties failed: " + e);
			throw new RuntimeException("Read config.properties failed: " + e);
		}
		
		GIT_URL = pps.getProperty("GIT_URL");
		SHEET_METADATA_NAME = pps.getProperty("SHEET_METADATA_NAME");
		METADATA_FILE = pps.getProperty("METADATA_FILE");
		EXPORT_EXCEL_NAME = pps.getProperty("EXPORT_EXCEL_NAME");
		DEFAULT_BRANCH = pps.getProperty("DEFAULT_BRANCH");
		SHEET_STORAGE_NAME = pps.getProperty("SHEET_STORAGE_NAME");
		TRANSLATED_SPREADSHEET = pps.getProperty("TRANSLATED_SPREADSHEET");
		METADATA_CREATE_BY = pps.getProperty("METADATA_CREATE_BY");
	}
	
}

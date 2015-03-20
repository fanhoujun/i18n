package storage.tools_i18n;


public class Constant {

	public static final String GIT_URL = "git://";
	
	public static final String EXPORT_EXCEL_NAME="need-translated.xls";
	
	public static final String BASE_RESOURCE_PATH = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
	
	public static final String Directory_Previous_Version= BASE_RESOURCE_PATH + "previous";
	
	public static final String Directory_Current_Version= BASE_RESOURCE_PATH + "current";
	
	public static final String SHEET_STORAGE= "STORAGE (ALL)";
	
	public static final String DELIMETER="-------------------------------------------------";

	public static final String SHEET_METADATA = "meta-data";

	public static final String METADATA_FILE = "metadata.json";
	
	public static final String DEFAULT_BRANCH = "origin/development";
}

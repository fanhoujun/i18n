package storage.tools_i18n.constant;


public class Constant {

	public static final String BASE_RESOURCE_PATH = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
	
	public static final String Directory_Previous_Version= BASE_RESOURCE_PATH + "previous";
	
	public static final String Directory_Current_Version= BASE_RESOURCE_PATH + "current";
	
	public static final String DELIMETER="-------------------------------------------------";
	
	public static final String LOCALE_EN = "locale_en.json";

}

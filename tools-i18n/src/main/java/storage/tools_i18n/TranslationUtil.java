package storage.tools_i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationUtil {
	
	
	
	/**
	 * download specific version files
	 * 
	 */
	public static void downloadPreviousCodes(String gitUrl, String commitId){
		
	}
	/**
	 * download latest necessary files, including metaData.json
	 */
	public static MetaData downloadLatestCodes(String gitUrl){
		
		return new MetaData();
	}
	public static Map<String, String> readJSON(String jsonFilePath){
		Map<String, String> map=new HashMap<String, String>();
		return map;
	}
	/**
	 * 
	 * @param excelFilePath excel file path including file name
	 * @return map Map<language, Map<keys, values>>
	 */
	public static List<Message> readExcelFromTranslateTeam(String excelFilePath){
		List<Message> allTranslatedMessages = new ArrayList<Message>();
		return allTranslatedMessages;
	}
	/**
	 * 
	 * @param outputFilePath
	 * @param modifiedMessages
	 * @param newMessages
	 * @param deletedMessages
	 * @param noChangeMessages
	 * @param excelMetaData
	 * @return String log info, warning message
	 */
	public static void generateNeedTranslateExcel(
			String outputFilePath, 
			List<Message> modifiedMessages,
			List<Message> newMessages,
			List<Message> deletedMessages,
			List<Message> noChangeMessages,
			MetaData excelMetaData){
		// poi component generate excel
	}
	public static void generateJsonFile(Map<String, String> pairs){
		
	}
}

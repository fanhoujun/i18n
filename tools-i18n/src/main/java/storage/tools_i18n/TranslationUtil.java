package storage.tools_i18n;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class TranslationUtil {
	
	private static Logger log = Logger.getLogger(TranslationUtil.class.getName());
	
	/**
	 * download specific version files
	 * @param gitUrl
	 * @param commitId
	 * @return String the folder path for the download codes
	 */
	public static String downloadPreviousCodes(String gitUrl, String commitId){
		log.log(Level.INFO, "Start donwloading files into folder [] from "+gitUrl+"[commitId="+commitId+"]");
		
		
		log.log(Level.INFO, Constant.DELIMETER);
		return "Folder Path";
	}
	/**
	 * download latest necessary files, including metaData.json
	 */
	public static MetaData downloadLatestCodes(String gitUrl){
		
		return new MetaData();
	}
	public static Map<String, String> readJSON(String jsonFilePath){
		log.log(Level.INFO, "Start Parsing file: "+jsonFilePath);
		//log.log(Level.WARNING, jsonFilePath+" not found. Supported numbers of locale(Please check Country.java) might greater than last translated numbers of locale");
		Map<String, String> map=new HashMap<String, String>();
		
		log.log(Level.INFO, Constant.DELIMETER);
		return map;
	}
	/**
	 * 
	 * @param excelFilePath excel file path including file name
	 * @return map Map<language, Map<keys, values>>
	 */
	public static List<Message> readExcelFromTranslateTeam(String excelFilePath){
		//log.log(Level.SEVERE, "File "+excelFilePath+" not found, please check"+);
		List<Message> allTranslatedMessages = new ArrayList<Message>();
		return allTranslatedMessages;
	}
	/**
	 * there are multiple josn_xx.json(json_en.json), read all of the keys in the files and return them as a Map
	 * @param folders
	 * @param country
	 * @return Map all the keys of the same country code
	 */
	public static Map<String, String> readAllKeys(List<String> folders, Country country){
		Map<String, String> keys = new HashMap<String, String>();
		log.log(Level.INFO, "Start parsing all locale_"+country.getCounrtyCode()+".json files......");
		for(String folderPath : folders){
			keys.putAll(readJSON(folderPath+"\\locale_"+country.getCounrtyCode()+".json"));
		}
		log.log(Level.INFO, "End parsing. "+keys.size()+" keys found In total.");
		log.log(Level.INFO, Constant.DELIMETER);
		return keys;
	}
	
	public static List<String> scanJsonFolders(String rootFolder){
		List<String> folderPaths = new ArrayList<String>();
		log.log(Level.INFO, "Start searching folders in "+rootFolder+" which store locale json files......");
		
		log.log(Level.INFO, "End searching folders. "+folderPaths.size()+" folders found: \n");
		for(String path : folderPaths){
			log.log(Level.INFO, path+"\n");
		}
		log.log(Level.INFO, Constant.DELIMETER);
		return folderPaths;
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
	 * @throws IOException 
	 */
	public static void generateNeedTranslateExcel(
			String outputFilePath, String sheetName,
			List<Message> modifiedMessages,
			List<Message> newMessages,
			List<Message> deletedMessages,
			List<Message> noChangeMessages,
			MetaData excelMetaData){
		log.log(Level.INFO, "Generating spreadsheet "+outputFilePath+"......");
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet(sheetName);
		
		int rowNum = 0;
		rowNum = NeedTranslationExcel.generateModifiedMessages(sheet, rowNum, modifiedMessages);
		NeedTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		rowNum = NeedTranslationExcel.generateNewMessages(sheet, rowNum, newMessages);
		NeedTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		rowNum = NeedTranslationExcel.generateDeletedMessages(sheet, rowNum, deletedMessages);
		NeedTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		rowNum = NeedTranslationExcel.generateNoChangeMessages(sheet, rowNum, noChangeMessages);
		NeedTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		NeedTranslationExcel.generateMetaDataSheet(workbook, excelMetaData);
		
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(outputFilePath);
			try {
				workbook.write(fileOut);
				fileOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.log(Level.INFO, Constant.DELIMETER);
	}
	public static void generateJsonFile(Map<String, String> pairs, String path){
		
	}
}

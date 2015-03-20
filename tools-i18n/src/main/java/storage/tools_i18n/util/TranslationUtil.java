package storage.tools_i18n.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

import storage.tools_i18n.constant.ConfigurationConstant;
import storage.tools_i18n.constant.Constant;
import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;

public class TranslationUtil {
	
	private static Logger log = Logger.getLogger(TranslationUtil.class.getName());
	
	/**
	 * download specific version files
	 * @throws IOException 
	 * @throws GitAPIException 
	 * @throws TransportException 
	 * @throws InvalidRemoteException 
	 */
	public static String downloadPreviousCodes(String repoURL, String commitId) {
		log.log(Level.INFO, "Start donwloading files into folder [] from "+repoURL+"[commitId="+commitId+"]");
		File localPath = new File(Constant.Directory_Previous_Version);
		checkoutProject(repoURL, localPath, commitId);
		log.log(Level.INFO, Constant.DELIMETER);
		return Constant.Directory_Previous_Version;
	}
	
	/**
	 * download latest necessary files, including metaData.json
	 * @throws IOException 
	 * @throws GitAPIException 
	 * @throws TransportException 
	 * @throws InvalidRemoteException 
	 */
	public static MetaData downloadLatestCodes(String repoURL, String commitId) {
		log.log(Level.INFO, "Start donwloading files into folder [] from "+repoURL+"[commitId="+commitId+"]");
		MetaData metadata = new MetaData();
		File localPath = new File(Constant.Directory_Current_Version);
		String currentCommitId = checkoutProject(repoURL, localPath, commitId);
		
		String metadataFilePath = Constant.Directory_Current_Version + File.separator + ConfigurationConstant.METADATA_FILE;
		File metadataFile = new File(metadataFilePath);
		if(metadataFile.exists()) {
			Map<String, String> map = readJSON(metadataFilePath);
			metadata.setLastTranslatedCommitId(map.get(MetaData.META_LAST_TRANSLATED_COMMIT_ID));
			metadata.setCurrentCommitId(map.get(MetaData.META_CURRENT_COMMIT_ID));
			metadata.setCreateDate(map.get(MetaData.META_CREATE_DATE));
			metadata.setCreatedBy(map.get(MetaData.META_CREATE_BY));
			
		} else {
			metadata.setCurrentCommitId(currentCommitId);
		}
		log.log(Level.INFO, Constant.DELIMETER);
		return metadata;
	}
	
	/**
	 * Read Json content from Json file and translate it to the Map
	 * @param jsonFilePath
	 * @return
	 */
	public static Map<String, String> readJSON(String jsonFilePath){
		log.log(Level.INFO, "Start Parsing file: "+jsonFilePath);
		File file = new File(jsonFilePath);
		Map<String, String> result = new LinkedHashMap<String, String>();
		if (!file.exists()) {
			return result;
		}
		String content = IoUtil.readText(file);
		try {
			JSONObject object = JSONObject.fromObject(content);
			parseJsonObjectToMap(object, "", result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		log.log(Level.INFO, Constant.DELIMETER);
		return result;
	}
	
	private static void parseJsonObjectToMap(JSONObject obj, String keyStr, Map<String, String> map) {
		Iterator iterator = obj.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			Object value = obj.get(key);
			if(!StringUtil.isEmpty(keyStr)) {
				key = keyStr + "." + key;
			}
			if(value instanceof JSONObject) {
				parseJsonObjectToMap((JSONObject) value, key, map);
			} else {
				map.put(key, value.toString());
			}
		}
	}
	
	/**
	 * 
	 * @param excelFilePath excel file path including file name
	 * @return map Map<language, Map<keys, values>>
	 */
	public static List<Message> readExcelFromTranslateTeam(String excelFilePath){
		//log.log(Level.SEVERE, "File "+excelFilePath+" not found, please check"+);
		List<Message> allTranslatedMessages = new ArrayList<Message>();
		
		Workbook wb=null;
		try {
			wb = WorkbookFactory.create(new FileInputStream(excelFilePath));
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Sheet sheet = wb.getSheet(ConfigurationConstant.SHEET_STORAGE_NAME);
		Map<String, Integer> languageColumeMap=new HashMap<String, Integer>();
		
		int languagesRow=-1, keyColume=-1;
		for(int i=sheet.getFirstRowNum();i<sheet.getLastRowNum();i++){
			Row r = sheet.getRow(i);
			int lastColumn = r.getLastCellNum();
			for (int cn = 0; cn < lastColumn; cn++) {
				if("KEY".equals(getCellValue(r.getCell(cn)))){
					languagesRow=i;
					keyColume=cn;
					break;
				}
			}
		}
		Row row = sheet.getRow(languagesRow);
		int lastColumn = row.getLastCellNum();
		for (int cn = 0; cn < lastColumn; cn++) {
			for(Country country : Country.values()){
				if(country.getCtryName().equals(getCellValue(row.getCell(cn)))){
					languageColumeMap.put(country.getCounrtyCode(), cn);
					break;
				}
			}
		}
		if(languageColumeMap.size()!=Country.values().length){
			log.log(Level.SEVERE, "Country Name defined in Country.java not match names in the spreadsheet at row#"+(languagesRow+1));
		}
		List<Country> otherCountries = Country.otherCountries();
		for(int i=languagesRow+1, len=sheet.getLastRowNum() ;i<len;i++){
			Row r = sheet.getRow(i);
			String key = getCellValue(r.getCell(keyColume));
			if(key==null || "".equals(key.trim())){
				continue;
			}
			Message message = new Message();
			message.setKey(key);
			message.setEnVal(getCellValue(
					r.getCell(
							languageColumeMap.get(Country.ENGLISH.getCounrtyCode()))));
			Map<String, String> otherTranslatedValues=new HashMap<String, String>();
			for(Country country : otherCountries){
				otherTranslatedValues.put(country.getCounrtyCode(), getCellValue(
						r.getCell(languageColumeMap.get(country.getCounrtyCode()))));
			}
			message.setLanguagesVal(otherTranslatedValues);
			allTranslatedMessages.add(message);
		}
		return allTranslatedMessages;
	}
	private static String getCellValue(Cell cell){
		if(cell==null){return "";}
		
		switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return cell.getRichStringCellValue().getString();
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toString();
            } else {
                return cell.getNumericCellValue()+"";
            }
        case Cell.CELL_TYPE_BOOLEAN:
           return String.valueOf(cell.getBooleanCellValue());
        case Cell.CELL_TYPE_FORMULA:
            return cell.getCellFormula();
        default:
        	return "";
    }
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
		if(keys.isEmpty()){
			log.log(Level.WARNING, "locale_"+country.getCounrtyCode()+".json is empty");
		}
		log.log(Level.INFO, Constant.DELIMETER);
		return keys;
	}
	
	/**
	 * Scan the folders that contains locale_en.json file
	 * @param rootFolder
	 * @return
	 */
	public static List<String> scanJsonFolders(String rootFolder){
		List<String> folderPaths = new ArrayList<String>();
		log.log(Level.INFO, "Start searching folders in "+rootFolder+" which store locale json files......");
		traverseFileInDirectory(rootFolder, "locale_en.json", folderPaths);
		
		String lastKey = "";
		for(int i=0; i<folderPaths.size(); i++) {
			Pattern pattern = Pattern.compile("\\\\v\\d+\\\\");
			Matcher matcher = pattern.matcher(folderPaths.get(i));
			
			if(matcher.find()) {
				int index = matcher.start();
				String key = folderPaths.get(2).substring(0, index);
				if(lastKey.equals(key)) {
					folderPaths.remove(i-1);
					i--;
				} else {
					lastKey = key;
				}
			}
		}
		
		for(String path : folderPaths) {
			log.log(Level.INFO, path+"\n");
		}
		log.log(Level.INFO, "End searching folders. "+folderPaths.size()+" folders found: \n");
		log.log(Level.INFO, Constant.DELIMETER);
		return folderPaths;
	}
	
	public static void traverseFileInDirectory(String filePath, String fileName, List<String> folderPaths) {
		File pFile = new File(filePath);
		File[] files = pFile.listFiles();
		for(File file : files) {
			if(file.isDirectory()) {
				traverseFileInDirectory(file.toString(), fileName, folderPaths);
			}
			if(fileName.equals(file.getName())) {
				folderPaths.add(filePath);
			}
		}
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
		Workbook workbook = new XSSFWorkbook();
		GenerateTranslationExcel.generateMetaDataSheet(workbook, excelMetaData);
		
		Sheet sheet = workbook.createSheet(sheetName);
		sheet.setDefaultColumnWidth(0x24);
		int rowNum = 0;
		rowNum = GenerateTranslationExcel.generateModifiedMessages(sheet, rowNum, modifiedMessages);
		GenerateTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		rowNum = GenerateTranslationExcel.generateNewMessages(sheet, rowNum, newMessages);
		GenerateTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		rowNum = GenerateTranslationExcel.generateDeletedMessages(sheet, rowNum, deletedMessages);
		GenerateTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		rowNum = GenerateTranslationExcel.generateNoChangeMessages(sheet, rowNum, noChangeMessages);
		GenerateTranslationExcel.createEmptyRow(sheet, rowNum++);
		
		
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
	
	/**
	 * Translate the Map to Json style content and put it to the Json file
	 * @param pairs
	 * @param jsonFilePath
	 */
	public static void generateJsonFile(Map<String, String> pairs, String jsonFilePath){
		log.log(Level.INFO, "Start to generate " + jsonFilePath);
		JSONObject json = new JSONObject();
		for(Entry<String, String> entry : pairs.entrySet()) {
			String key = entry.getKey();
			String[] splittedKey = key.split("\\.");
			JSONObject nestedObject  = json;
			
			for (int i = 0; i < splittedKey.length - 1; i++) {
				if (!nestedObject.has(splittedKey[i])) {
					nestedObject.accumulate(splittedKey[i], new JSONObject());
				}
				nestedObject = nestedObject.getJSONObject(splittedKey[i]);
			}
			nestedObject.accumulate(splittedKey[splittedKey.length - 1], entry.getValue());
		}
		IoUtil.write(json.toString(4), new File(jsonFilePath));
		log.log(Level.INFO, Constant.DELIMETER);
	}
	
	/**
	 * Clone a Git repo to a specified path and checkout to a specified branch or commit ID
	 * @param repoURL
	 * @param localPath
	 * @param commitId
	 * @return
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static String checkoutProject(String repoURL, File localPath, String commitId) {
		log.log(Level.INFO, "Start to clone " + repoURL + " to " + localPath);
		if(localPath.exists()) {
			deleteAll(localPath);
		}
		localPath.mkdir();
		
		//Clone Git repo to localPath
		Git git = null;
		try {
			git = Git.cloneRepository()
					.setURI(repoURL)
					.setDirectory(localPath)
					.call();
		} 
		/*catch (JGitInternalException e) {
			System.out.println(e);
			try {
				git = Git.open(localPath);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			git.fetch();
			git.rebase();
		} */catch (Exception e) {
			log.log(Level.SEVERE, "Clone repo failed! Error: " + e);
			throw new RuntimeException("Clone repo failed! Error: " + e);
		}
		
		try {
			//Checkout, commitId can be branch name or commit ID
			CheckoutCommand cc = null;
			if(!StringUtil.isEmpty(commitId)) {
				log.log(Level.INFO, "checkout " + commitId);
				cc = git.checkout();
				cc.setName(commitId);
			}
			cc.call();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Checkout failed! Error: " + e);
			throw new RuntimeException("Checkout failed! Error: " + e);
		}
		
		//If the commitId is empty(means the current branch is master) or a branch name, get the latest commit ID
		try {
			if(StringUtil.isEmpty(commitId) || commitId.contains("origin")) {
				Iterable<RevCommit> logs = git.log().call();
				RevCommit rc = logs.iterator().next();
				commitId = rc.getName();
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Get commit ID failed! Error: " + e);
			throw new RuntimeException("Get commit ID failed! Error: " + e);
		}
		log.log(Level.INFO, Constant.DELIMETER);
		return commitId;
	}
	
	public static void deleteAll(File file) {
		if(file.isFile() || file.list().length ==0) {
			file.delete();
		} else {
			File[] files = file.listFiles();  
			for (int i = 0; i < files.length; i++) {
				deleteAll(files[i]);
				files[i].delete();
			}
			
			if(file.exists())
				file.delete();
		}
	}
	
	public static void main(String[] args) {
//		String repoURL = "ssh://Wen-Qiang.Jia@c0040528.itcs.hp.com:8087/ECS-CC-NA-UI";
//		String commitId = "origin/development";
//		try {
//			TranslationUtil.downloadPreviousCodes(repoURL, commitId);
//			System.out.println(TranslationUtil.downloadLatestCodes(repoURL, commitId));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
//		String jsonFilePath = Constant.CURRENT_CODE_PATH
//				+ File.separator + "app/neo/static/lib/neo-1.1/i18n/locale_en.json";
//		Map<String, String> map = TranslationUtil.readJSON(jsonFilePath);
//		for(Entry<String, String> entry : map.entrySet()) {
//			System.out.print(entry.getKey()+"=");
//			System.out.println(entry.getValue());
//		}
//		String jsonFilePath_new = "C:/SVN/locale_en.json";
//		TranslationUtil.generateJsonFile(map, jsonFilePath_new);
		
//		TranslationUtil.scanJsonFolders(Constant.Directory_Current_Version);
	}
}
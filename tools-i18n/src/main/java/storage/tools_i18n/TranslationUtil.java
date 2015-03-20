package storage.tools_i18n;

import java.io.File;
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

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

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
		
		String metadataFilePath = Constant.Directory_Current_Version + File.separator + Constant.METADATA_FILE;
		File metadataFile = new File(metadataFilePath);
		if(metadataFile.exists()) {
			Map<String, String> map = readJSON(metadataFilePath);
			metadata.setLastTranslatedCommitId(map.get("lastTranslatedCommitId"));
			metadata.setCurrentCommitId(map.get("currentCommitId"));
			metadata.setCreateDate(map.get("createDate"));
			metadata.setCreatedBy(map.get("createdBy"));
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
//		} catch (JGitInternalException e) {
//			System.out.println(e);
//			git = Git.open(localPath);
//			git.fetch();
//			git.rebase();
		} catch (Exception e) {
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
		String repoURL = "ssh://Wen-Qiang.Jia@c0040528.itcs.hp.com:8087/ECS-CC-NA-UI";
		String commitId = "origin/development";
		try {
//			TranslationUtil.downloadPreviousCodes(repoURL, commitId);
			System.out.println(TranslationUtil.downloadLatestCodes(repoURL, commitId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		String jsonFilePath = Constant.CURRENT_CODE_PATH
//				+ File.separator + "app/neo/static/lib/neo-1.1/i18n/locale_en.json";
//		Map<String, String> map = TranslationUtil.readJSON(jsonFilePath);
//		for(Entry<String, String> entry : map.entrySet()) {
//			System.out.print(entry.getKey()+"=");
//			System.out.println(entry.getValue());
//		}
//		String jsonFilePath_new = "C:/SVN/locale_en.json";
//		TranslationUtil.generateJsonFile(map, jsonFilePath_new);
	}
}

package storage.tools_i18n.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

import storage.tools_i18n.model.FolderModel;
import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.model.SheetModel;

public class TranslationUtil {

	private static Logger log = Logger.getLogger(TranslationUtil.class
			.getName());

	/**
	 * download specific version files
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws TransportException
	 * @throws InvalidRemoteException
	 */
	public static void downloadPreviousCodes(String commitId) {
		log.log(Level.INFO,
				StringUtil.DELIMETER
						+ "Start donwloading previous applied translations version[commitId="
						+ commitId + "]");
		checkoutProject(Configuration.GIT_URL, commitId);
	}

	/**
	 * download latest necessary files, including metaData.json
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws TransportException
	 * @throws InvalidRemoteException
	 */
	public static MetaData downloadLatestCodes(String repoURL, String branchName) {
		String metadataFilePath = null;
		File metadataFile = null;
		log.log(Level.INFO, StringUtil.DELIMETER + "Donwloading...");
		MetaData metadata = new MetaData();
		metadata.setWorkspaceCommitId(checkoutProject(repoURL, branchName));
		log.log(Level.INFO,
				StringUtil.DELIMETER + "Donwloaded HEAD version[commitId="
						+ metadata.getWorkspaceCommitId() + "] from " + repoURL
						+ "[branch=" + branchName + "]");
		List<String> files = scanJsonFolders(repoURL,
				Configuration.METADATA_FILE);
		if (files.size() > 0) {
			metadataFilePath = files.get(0) + File.separator
					+ Configuration.METADATA_FILE;
			metadataFile = new File(metadataFilePath);
		}
		if (metadataFilePath != null && metadataFile.exists()) {
			Map<String, String> map = readJSON(metadataFilePath);
			metadata.setApplyId(map.get(MetaData.META_APPLY_ID));
			metadata.setExportId(map.get(MetaData.META_EXPORT_ID));
			metadata.setCreateDate(map.get(MetaData.META_CREATE_DATE));
			metadata.setCreatedBy(map.get(MetaData.META_CREATE_BY));
		}

		return metadata;
	}

	/**
	 * Read Json content from Json file and translate it to the Map
	 * 
	 * @param jsonFilePath
	 * @return
	 */
	public static Map<String, String> readJSON(String jsonFilePath) {
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
		StringBuffer sb = new StringBuffer(StringUtil.DELIMETER
				+ "Parsing file: ");
		sb.append(jsonFilePath);
		if (result.isEmpty()) {
			sb.append("\t" + jsonFilePath + " is empty");
		}
		log.log(Level.INFO, sb.toString());
		return result;
	}

	private static void parseJsonObjectToMap(JSONObject obj, String keyStr,
			Map<String, String> map) {
		Iterator iterator = obj.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			Object value = obj.get(key);
			if (!StringUtil.isEmpty(keyStr)) {
				key = keyStr + "." + key;
			}
			if (value instanceof JSONObject) {
				parseJsonObjectToMap((JSONObject) value, key, map);
			} else if (!value.equals(JSONNull.getInstance())) {
				map.put(key, value.toString());
			}
		}
	}

	/**
	 * 
	 * @param excelFilePath
	 *            excel file path including file name
	 * @param sheetName
	 * @return map Map<language, Map<keys, values>>
	 */
	public static Map<String, List<Message>> readExcelFromTranslateTeam(
			String excelFilePath) {
		Map<String, List<Message>> modulesTranslated = new HashMap<String, List<Message>>();

		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new FileInputStream(excelFilePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		int totalSheet = wb.getNumberOfSheets();
		for (int idx = 0; idx < totalSheet; idx++) {
			Sheet sheet = wb.getSheetAt(idx);
			String sheetName = sheet.getSheetName();
			if (Configuration.SHEET_METADATA_NAME.equals(sheetName)) {
				continue;
			}
			log.log(Level.INFO, "Parsing modules " + sheetName + "...");

			List<Message> allTranslatedMessages = new ArrayList<Message>();
			Map<String, Integer> languageColumeMap = new HashMap<String, Integer>();

			Row row = sheet.getRow(Configuration.LANGUAGE_ROW_NUM);
			int lastColumn = row.getLastCellNum();
			for (int cn = 0; cn < lastColumn; cn++) {
				for (Country country : Country.values()) {
					if (country.getName().equals(getCellValue(row.getCell(cn)))) {
						languageColumeMap.put(country.getCode(), cn);
						break;
					}
				}
			}
			if (languageColumeMap.size() != Country.values().size()) {
				log.log(Level.SEVERE,
						"Country Name defined in Country.java not match names in the spreadsheet at row#"
								+ (Configuration.LANGUAGE_ROW_NUM + 1));
			}
			sheet.setColumnWidth(Configuration.KEY_COLUMN_NUM, 0x24);
			for (int i = Configuration.LANGUAGE_ROW_NUM + 1, len = sheet
					.getLastRowNum(); i < len; i++) {
				row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				Cell cell = row.getCell(Configuration.KEY_COLUMN_NUM);
				String key = getCellValue(cell);
				if (StringUtil.isEmpty(key)
						|| Configuration.MODIFIED_EXCEL_TITLE.equals(key)
						|| Configuration.NEW_EXCEL_TITLE.equals(key)
						|| Configuration.NO_CHANGE_EXCEL_TITLE.equals(key)
						|| Configuration.DELETED_EXCEL_TITLE.equals(key)) {
					break;
				}
				Message message = new Message();
				message.setKey(key);
				message.setEnVal(getCellValue(row.getCell(languageColumeMap
						.get(Country.ENGLISH.getCode()))));
				Map<String, String> otherTranslatedValues = new HashMap<String, String>();
				for (Country country : Country.locals()) {
					otherTranslatedValues.put(country.getCode(),
							getCellValue(row.getCell(languageColumeMap
									.get(country.getCode()))));
				}
				message.setLocals(otherTranslatedValues);
				allTranslatedMessages.add(message);
			}
			sheet.setColumnWidth(Configuration.KEY_COLUMN_NUM, 0);
			log.log(Level.INFO, allTranslatedMessages.size()
					+ " translated messages founded in module " + sheetName);
			modulesTranslated.put(sheetName, allTranslatedMessages);
		}
		return modulesTranslated;
	}

	private static String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}

		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getRichStringCellValue().getString();
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			} else {
				return cell.getNumericCellValue() + "";
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
	 * there are multiple josn_xx.json(json_en.json), read all of the keys in
	 * the files and return them as a Map
	 * 
	 * @param folders
	 * @param country
	 * @return Map all the keys of the same country code
	 */
	public static Map<String, String> readAllKeys(String folder, Country country) {
		Map<String, String> keys = new HashMap<String, String>();
		keys.putAll(readJSON(folder + File.separator + country.getCode()));
		return keys;
	}

	/**
	 * Scan the folders that contains locale_en.json file
	 * 
	 * @param rootFolder
	 * @return
	 */
	public static List<String> scanJsonFolders(String rootFolder,
			String fileName) {
		log.log(Level.INFO, StringUtil.DELIMETER + "Searching...");
		List<String> folderPaths = new ArrayList<String>();
		traverseFileInDirectory(rootFolder, fileName, folderPaths);

		String lastKey = "";
		for (int i = 0; i < folderPaths.size(); i++) {
			Pattern pattern = Pattern.compile("\\\\v\\d+\\\\");
			Matcher matcher = pattern.matcher(folderPaths.get(i));

			if (matcher.find()) {
				int index = matcher.start();
				String key = folderPaths.get(i).substring(0, index);
				if (lastKey.equals(key)) {
					folderPaths.remove(i - 1);
					i--;
				} else {
					lastKey = key;
				}
			}
		}
		StringBuffer sb = new StringBuffer("\nSearching file ");
		sb.append(fileName).append(" in folder ").append(rootFolder)
				.append("\n");
		for (String path : folderPaths) {
			sb.append("\t" + path + File.separator + fileName + "\n");
		}
		sb.append(StringUtil.DELIMETER + "End searching folders. "
				+ folderPaths.size() + " folders found.\n");
		log.log(Level.INFO, sb.toString());
		return folderPaths;
	}

	public static void traverseFileInDirectory(String filePath,
			String fileName, List<String> folderPaths) {
		File pFile = new File(filePath);
		File[] files = pFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				traverseFileInDirectory(file.toString(), fileName, folderPaths);
			}
			if (fileName.equals(file.getName())) {
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
	public static void export(String outputFilePath,
			List<SheetModel> models, MetaData excelMetaData) {
		log.log(Level.INFO, StringUtil.DELIMETER + "Generating spreadsheet "
				+ outputFilePath + "......");
		Workbook workbook = new XSSFWorkbook();

		for (SheetModel model : models) {
			Sheet sheet = workbook.createSheet(model.getSheetName());
			sheet.setDefaultColumnWidth(0x24);
			int rowNum = 0;

			rowNum = ExcelUtil.creatColumnHeaders(sheet, rowNum, workbook);

			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getModifiedMessages(),
					Configuration.MODIFIED_EXCEL_TITLE,
					IndexedColors.GREEN.index);
			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getNewMessages(), Configuration.NEW_EXCEL_TITLE,
					IndexedColors.ORANGE.index);
			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getDeletedMessages(),
					Configuration.DELETED_EXCEL_TITLE, IndexedColors.RED.index);
			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getNoChangeMessages(),
					Configuration.NO_CHANGE_EXCEL_TITLE,
					IndexedColors.WHITE.index);

			sheet.createFreezePane(3, 1);
			sheet.setColumnWidth(0, 0);
			sheet.setColumnWidth(1, 5000);
		}
		ExcelUtil.generateMetaDataSheet(workbook, excelMetaData);
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(outputFilePath);
			workbook.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translate the Map to Json style content and put it to the Json file
	 * 
	 * @param pairs
	 * @param jsonFilePath
	 */
	public static void generateJsonFile(Map<String, String> pairs,
			String jsonFilePath) {
		log.log(Level.INFO, StringUtil.DELIMETER + "Generating " + jsonFilePath);
		JSONObject json = new JSONObject();
		for (Entry<String, String> entry : pairs.entrySet()) {
			String key = entry.getKey();
			String[] splittedKey = key.split("\\.");
			JSONObject nestedObject = json;

			for (int i = 0; i < splittedKey.length - 1; i++) {
				if (!nestedObject.has(splittedKey[i])) {
					nestedObject.accumulate(splittedKey[i], new JSONObject());
				}
				nestedObject = nestedObject.getJSONObject(splittedKey[i]);
			}
			nestedObject.accumulate(splittedKey[splittedKey.length - 1],
					entry.getValue());
		}
		IoUtil.write(json.toString(4), new File(jsonFilePath));
	}

	/**
	 * Clone a Git repo to a specified path and checkout to a specified branch
	 * or commit ID
	 * 
	 * @param repoURL
	 * @param localPath
	 * @param commitId
	 * @return
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public static String checkoutProject(String repoURL, String commitId) {
		if (StringUtil.isEmpty(commitId)) {
			throw new RuntimeException("commitId should not empty!");
		}
		File repo = new File(repoURL);
		if (!repo.exists() || repo.listFiles().length <= 0) {
			log.log(Level.SEVERE, "Can not find the Git repo: " + repoURL);
			throw new RuntimeException("Can not find the Git repo: " + repoURL);
		}

		Git git = null;
		try {
			git = Git.open(repo);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Open repo failed! Error: " + e);
			throw new RuntimeException("Open repo failed! Error: " + e);
		}

		// Checkout, commitId can be branch name or commit ID
		if (!StringUtil.isEmpty(commitId)) {
			CheckoutCommand cc = git.checkout();
			cc.setName(commitId);
			try {
				cc.call();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Checkout failed! Error: " + e);
				throw new RuntimeException("Checkout failed! Error: " + e);
			}
		}

		// If the commitId is empty(means the current branch is master) or a
		// branch name, get the latest commit ID
		try {
			Iterable<RevCommit> logs = git.log().call();
			RevCommit rc = logs.iterator().next();
			commitId = rc.getName();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Get commit ID failed! Error: " + e);
			throw new RuntimeException("Get commit ID failed! Error: " + e);
		}
		return commitId;
	}

	/**
	 * 
	 * @param meta
	 * @return Map Key is the folderPath
	 */
	public static List<FolderModel> loadDataForCompare(boolean export,
			MetaData meta) {
		// scan current version folder structure
		List<String> jsonFolders = TranslationUtil.scanJsonFolders(
				Configuration.GIT_URL, Country.ENGLISH.getCode());

		List<FolderModel> analysisTranslationData = new ArrayList();
		Set<String> currentModules = new HashSet<String>();
		// read current version English data
		for (String jsonFolder : jsonFolders) {
			currentModules.add(jsonFolder);
			Map<String, String> englishPair = TranslationUtil.readAllKeys(
					jsonFolder, Country.ENGLISH);
			FolderModel analysisDataModel = new FolderModel();
			analysisDataModel.setEnglishPair(englishPair);
			// avoid NullPointerException
			analysisDataModel.setOldEnPair(new HashMap<String, String>());
			analysisDataModel.setFolder(jsonFolder);
			analysisDataModel
					.setOtherLanguagesPreviousTranslatedPair(new HashMap<String, Map<String, String>>());
			analysisTranslationData.add(analysisDataModel);
		}
		if (currentModules.size() != jsonFolders.size()) {
			throw new RuntimeException(
					"Module name duplicated after simplified, Please modify the configuration \"IGNORE_KEY_WRODS\" in the properties file and run this tool again."
							+ "If it not works, please delete the keywords and run this tool again, thanks.");
		}
		// if applied translation before, download the last applied version
		readOldEnPairs(export, meta, analysisTranslationData);

		for (FolderModel model : analysisTranslationData) {
			Map<String, Map<String, String>> otherLanguagesTranslatedPair = new HashMap<String, Map<String, String>>();
			for (Country country : Country.locals()) {
				otherLanguagesTranslatedPair
						.put(country.getCode(), TranslationUtil.readAllKeys(
								model.getFolder(), country));
			}
			model.setOtherLanguagesPreviousTranslatedPair(otherLanguagesTranslatedPair);
		}
		return analysisTranslationData;
	}

	private static void readOldEnPairs(boolean export, MetaData meta,
			List<FolderModel> analysisTranslationData) {
		String commitId = export ? meta.getApplyId() : meta.getExportId();
		if (StringUtil.isEmpty(commitId)) {
			return;
		}
		TranslationUtil.downloadPreviousCodes(commitId);
		// scan previous version folder structure
		for (FolderModel model : analysisTranslationData) {
			Map<String, String> oldEnPair = TranslationUtil.readAllKeys(
					model.getFolder(), Country.ENGLISH);
			model.setOldEnPair(oldEnPair);
		}
		TranslationUtil.downloadLatestCodes(Configuration.GIT_URL,
				Configuration.DEFAULT_BRANCH);
	}

	public static MetaData readExcelMetaData(String translatedSpreadsheet) {
		log.log(Level.INFO, "Parsing MetaData from " + translatedSpreadsheet);
		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new FileInputStream(
					translatedSpreadsheet));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Sheet sheet = wb.getSheet(Configuration.SHEET_METADATA_NAME);
		if (sheet == null) {
			throw new RuntimeException(
					"Please specify the version in the sheet "
							+ Configuration.SHEET_METADATA_NAME + " in "
							+ Configuration.TRANSLATED_XLS);
		}
		Row row = null;
		int col = 0;
		MetaData metaData = new MetaData();
		for (int i = 0, len = sheet.getLastRowNum(); i < len; i++) {
			row = sheet.getRow(i);
			String keyName = getCellValue(row.getCell(col++));
			String value = getCellValue(row.getCell(col++));
			if (MetaData.META_APPLY_ID.equals(keyName)) {
				metaData.setApplyId(value);
			} else if (MetaData.META_EXPORT_ID.equals(keyName)) {
				metaData.setExportId(value);
			} else if (MetaData.META_CREATE_DATE.equals(keyName)) {
				metaData.setCreateDate(value);
			} else if (MetaData.META_CREATE_BY.equals(keyName)) {
				metaData.setCreatedBy(value);
			}
		}
		return metaData;
	}
}

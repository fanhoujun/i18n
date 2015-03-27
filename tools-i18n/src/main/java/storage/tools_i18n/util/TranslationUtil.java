package storage.tools_i18n.util;

import java.io.File;
import java.io.FileInputStream;
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

import storage.tools_i18n.model.AnalysisDataModel;
import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.model.NeedTranslationModel;

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
		log.log(Level.INFO, "Start donwloading files into folder [] from "
				+ repoURL + "[commitId=" + branchName + "]");
		String metadataFilePath = null;
		File metadataFile = null;

		MetaData metadata = new MetaData();
		metadata.setWorkspaceCommitId(checkoutProject(repoURL, branchName));

		List<String> files = scanJsonFolders(repoURL,
				Configuration.METADATA_FILE);
		if (files.size() > 0) {
			metadataFilePath = files.get(0) + File.separator
					+ Configuration.METADATA_FILE;
			metadataFile = new File(metadataFilePath);
		}
		if (metadataFilePath != null && metadataFile.exists()) {
			Map<String, String> map = readJSON(metadataFilePath);
			metadata.setCommitId(map.get(MetaData.META_COMMIT_ID));
			metadata.setCreateDate(map.get(MetaData.META_CREATE_DATE));
			metadata.setCreatedBy(map.get(MetaData.META_CREATE_BY));
		}

		log.log(Level.INFO, StringUtil.DELIMETER);
		return metadata;
	}

	/**
	 * Read Json content from Json file and translate it to the Map
	 * 
	 * @param jsonFilePath
	 * @return
	 */
	public static Map<String, String> readJSON(String jsonFilePath) {
		log.log(Level.INFO, "Start Parsing file: " + jsonFilePath);
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
		log.log(Level.INFO, StringUtil.DELIMETER);
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
			} else {
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
	public static List<Message> readExcelFromTranslateTeam(
			String excelFilePath, String sheetName) {
		List<Message> allTranslatedMessages = new ArrayList<Message>();

		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new FileInputStream(excelFilePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Sheet sheet = wb.getSheet(sheetName);
		Map<String, Integer> languageColumeMap = new HashMap<String, Integer>();

		int languagesRow = -1, keyColume = -1;
		for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i++) {
			Row r = sheet.getRow(i);
			int lastColumn = r.getLastCellNum();
			for (int cn = 0; cn < lastColumn; cn++) {
				if ("KEY".equals(getCellValue(r.getCell(cn)))) {
					languagesRow = i;
					keyColume = cn;
					break;
				}
			}
		}
		Row row = sheet.getRow(languagesRow);
		int lastColumn = row.getLastCellNum();
		for (int cn = 0; cn < lastColumn; cn++) {
			for (Country country : Country.values()) {
				if (country.getName().equals(getCellValue(row.getCell(cn)))) {
					languageColumeMap.put(country.getCode(), cn);
					break;
				}
			}
		}
		if (languageColumeMap.size() != Country.locals().size() + 1) {
			log.log(Level.SEVERE,
					"Country Name defined in Country.java not match names in the spreadsheet at row#"
							+ (languagesRow + 1));
		}
		for (int i = languagesRow + 1, len = sheet.getLastRowNum(); i < len; i++) {
			Row r = sheet.getRow(i);
			String key = getCellValue(r.getCell(keyColume));
			if (key == null || "".equals(key.trim())) {
				continue;
			}
			Message message = new Message();
			message.setKey(key);
			message.setEnVal(getCellValue(r.getCell(languageColumeMap
					.get(Country.ENGLISH.getCode()))));
			Map<String, String> otherTranslatedValues = new HashMap<String, String>();
			for (Country country : Country.locals()) {
				otherTranslatedValues.put(country.getCode(), getCellValue(r
						.getCell(languageColumeMap.get(country.getCode()))));
			}
			message.setLanguagesVal(otherTranslatedValues);
			allTranslatedMessages.add(message);
		}
		return allTranslatedMessages;
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
		log.log(Level.INFO, "Start parsing all locale_" + country.getCode()
				+ " files......");
		keys.putAll(readJSON(folder + File.separator + country.getCode()));
		log.log(Level.INFO, "End parsing. " + keys.size()
				+ " keys found In total.");
		if (keys.isEmpty()) {
			log.log(Level.WARNING, country.getCode() + " is empty");
		}
		log.log(Level.INFO, StringUtil.DELIMETER);
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
		List<String> folderPaths = new ArrayList<String>();
		log.log(Level.INFO, "Start searching folders in " + rootFolder
				+ " which store locale json files......");
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

		for (String path : folderPaths) {
			log.log(Level.INFO, path + "\n");
		}
		log.log(Level.INFO, "End searching folders. " + folderPaths.size()
				+ " folders found: \n");
		log.log(Level.INFO, StringUtil.DELIMETER);
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
	public static void generateNeedTranslateExcel(String outputFilePath,
			List<NeedTranslationModel> models, MetaData excelMetaData) {
		log.log(Level.INFO, "Generating spreadsheet " + outputFilePath
				+ "......");
		Workbook workbook = new XSSFWorkbook();
		ExcelUtil.generateMetaDataSheet(workbook, excelMetaData);
		for (NeedTranslationModel model : models) {
			Sheet sheet = workbook.createSheet(model.getSheetName());
			sheet.setDefaultColumnWidth(0x24);
			int rowNum = 0;

			rowNum = ExcelUtil.creatColumnHeaders(sheet, rowNum, workbook);

			List<Message> msgs = new ArrayList();
			for (Message msg : model.getNoChangeMessages()) {
				String uk = msg.getLanguagesVal().get("en_gb.json");
				if (msg.getEnVal().equals(uk)) {
					msgs.add(msg);
				} else {
					msg.setModifiedEnVal(msg.getEnVal());
					msg.setEnVal(uk);
					model.getModifiedMessages().add(msg);
				}
			}
			model.setNoChangeMessages(msgs);

			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getModifiedMessages(),
					"Sanity check needed for modified messages",
					IndexedColors.GREEN.index);
			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getNewMessages(), "Messages to be translated",
					IndexedColors.ORANGE.index);
			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getDeletedMessages(),
					"FYI no action needed, deleted messages",
					IndexedColors.RED.index);
			rowNum = ExcelUtil.createPart(sheet, rowNum,
					model.getNoChangeMessages(),
					"FYI no action needed, no change message",
					IndexedColors.WHITE.index);

			sheet.createFreezePane(3, 1);
			sheet.setColumnWidth(0, 0);
			sheet.setColumnWidth(1, 5000);
		}

		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(outputFilePath);
			workbook.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		log.log(Level.INFO, StringUtil.DELIMETER);
		log.log(Level.INFO, "Generated spreadsheet " + outputFilePath + ".");
	}

	/**
	 * Translate the Map to Json style content and put it to the Json file
	 * 
	 * @param pairs
	 * @param jsonFilePath
	 */
	public static void generateJsonFile(Map<String, String> pairs,
			String jsonFilePath) {
		log.log(Level.INFO, "Start to generate " + jsonFilePath);
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
		log.log(Level.INFO, StringUtil.DELIMETER);
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
		log.log(Level.INFO, "Open " + repoURL);
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
			log.log(Level.INFO, "checkout " + commitId);
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
		log.log(Level.INFO, StringUtil.DELIMETER);
		return commitId;
	}

	public static String getSheetName(String jsonFolder) {
		String path = jsonFolder.substring(Configuration.GIT_URL.length())
				.replaceAll("[\\\\/]+", "_");
		for (String s : new String[] { "i18n", "app", "static", "app", "web",
				"v\\d" }) {
			path = path.replaceAll("_" + s + "_", "_");
		}
		return path.replaceAll("_+", "");
	}

	public static AnalysisDataModel loadDataForCompare(MetaData meta,
			String jsonFolder) {
		AnalysisDataModel analysisDataModel = new AnalysisDataModel();

		Map<String, String> englishPair = TranslationUtil.readAllKeys(
				jsonFolder, Country.ENGLISH);

		// if applied translation before, download the last applied version
		String lastTranslatedCommitId = meta.getCommitId();
		Map<String, String> oldEnPair = new HashMap<String, String>();
		Map<String, Map<String, String>> otherLanguagesTranslatedPair = new HashMap<String, Map<String, String>>();
		for (Country otherCountries : Country.locals()) {
			otherLanguagesTranslatedPair.put(otherCountries.getCode(),
					TranslationUtil.readAllKeys(jsonFolder, otherCountries));
		}
		if (!StringUtil.isEmpty(lastTranslatedCommitId)) {
			TranslationUtil.downloadPreviousCodes(lastTranslatedCommitId);

			oldEnPair = TranslationUtil
					.readAllKeys(jsonFolder, Country.ENGLISH);
			TranslationUtil.checkoutProject(Configuration.GIT_URL,
					Configuration.DEFAULT_BRANCH);
		}
		analysisDataModel.setMetaData(meta);
		analysisDataModel.setEnglishPair(englishPair);
		analysisDataModel.setOldEnPair(oldEnPair);
		analysisDataModel
				.setOtherLanguagesPreviousTranslatedPair(otherLanguagesTranslatedPair);
		return analysisDataModel;
	}

	public static Map<String, String> checkFileConsistent(
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair) {
		log.log(Level.INFO,
				"\t Checking all the locale except English if the are consistent......");
		boolean setDefault = false;
		String nonEnglishLocale = "";
		Map<String, String> nonEnglishLocalePair = new HashMap<String, String>();
		for (String key : otherLanguagesPreviousTranslatedPair.keySet()) {
			Map<String, String> pairs = otherLanguagesPreviousTranslatedPair
					.get(key);
			// One more language(locale_ja) in Excel while josn file
			// locale_ja.json is not exists
			if (!setDefault && !pairs.isEmpty()) {
				nonEnglishLocalePair = pairs;
				nonEnglishLocale = key;
				setDefault = true;
				continue;
			}
			if (pairs.size() < nonEnglishLocalePair.size()) {
				log.log(Level.WARNING,
						"Keys between different locale "
								+ key
								+ " and "
								+ nonEnglishLocale
								+ " not equals. One of these files might be modified or supported countries greater than before. Please double check.");
				nonEnglishLocalePair = pairs;
				nonEnglishLocale = key;
			}
		}
		return nonEnglishLocalePair;
	}
}

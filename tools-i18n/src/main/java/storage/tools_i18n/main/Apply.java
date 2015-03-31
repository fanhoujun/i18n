package storage.tools_i18n.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.FolderModel;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.StringUtil;
import storage.tools_i18n.util.ResourceUtil;

public class Apply {
	private static Logger log = Logger.getLogger(Apply.class.getName());

	public static void main(String[] args) {
		Configuration.init(args);
		MetaData xlsMeta = readExcelMetaData(Configuration.TRANSLATED_XLS);
		if (StringUtil.isEmpty(xlsMeta.getExportId())) {
			log.warning("No Export ID was found in the file:"
					+ Configuration.TRANSLATED_XLS);
			return;
		}
		MetaData meta = ResourceUtil.downloadLatestCodes(Configuration.GIT_URL,
				Configuration.DEFAULT_BRANCH);

		if (!xlsMeta.getExportId().equals(meta.getExportId())) {
			log.warning("Export ID:" + xlsMeta.getExportId() + " in the file:"
					+ Configuration.TRANSLATED_XLS
					+ " does not match with Export ID:" + meta.getExportId());
			return;
		}

		List<FolderModel> folderModels = ResourceUtil.loadFolders();
		ResourceUtil.readOldEnPairs(meta.getExportId(), folderModels);
		Map<String, List<Message>> translatedMessages = readTranslations();

		for (FolderModel model : folderModels) {

			String sheetName = model.getSheetName();
			log.info("apply module " + sheetName + "...");
			applyTranslate(model, translatedMessages.get(sheetName));
		}
	}

	/**
	 * 
	 * @param excelFilePath
	 *            excel file path including file name
	 * @param sheetName
	 * @return map Map<language, Map<keys, values>>
	 */
	private static Map<String, List<Message>> readTranslations() {
		Map<String, List<Message>> modulesTranslated = new LinkedHashMap<String, List<Message>>();

		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new FileInputStream(
					Configuration.TRANSLATED_XLS));
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			Map<String, Integer> languageColumeMap = new LinkedHashMap<String, Integer>();

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
				Map<String, String> otherTranslatedValues = new LinkedHashMap<String, String>();
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

	private static MetaData readExcelMetaData(String translatedSpreadsheet) {
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

	private static void applyTranslate(FolderModel model,
			List<Message> translations) {
		String folder = model.getFolder();
		log.info("apply folder " + folder + "...");
		Map<String, String> readyEnPairs = new LinkedHashMap();

		for (Map.Entry<String, String> me : model.getEnglishPair().entrySet()) {
			String key = me.getKey();
			String oldEnVal = model.getOldEnPair().get(key);
			Message message = getMessage(translations, key);
			if (StringUtil.isEmpty(oldEnVal) || me.getValue().equals(oldEnVal)) {
				if (message == null) {
					readyEnPairs.put(key, me.getValue());
				} else {
					readyEnPairs.put(key, message.getEnVal());
				}
			} else {
				log.warning("Engilish value was changed:");
				log.warning("\tKey=" + key);
				log.warning("\tCurrent value=" + me.getValue());
				log.warning("\tOld value=" + model.getOldEnPair());
				// TODO
			}
		}

		if (!readyEnPairs.equals(model.getEnglishPair())) {
			ResourceUtil.generateJsonFile(readyEnPairs, folder + File.separator
					+ Country.ENGLISH.getCode());
		}

		for (Country country : Country.locals()) {
			Map<String, String> readyPairs = new LinkedHashMap();
			for (String key : readyEnPairs.keySet()) {
				String local = getLocal(translations, country, key);
				if (StringUtil.isEmpty(local)) {
					local = model.getLocal(key, country.getCode());
				}
				if (!StringUtil.isEmpty(local)) {
					readyPairs.put(key, local);
				}
			}
			if (!readyPairs.isEmpty()) {
				ResourceUtil.generateJsonFile(readyPairs, folder
						+ File.separator + country.getCode());
			}
		}
	}

	private static String getLocal(List<Message> translations, Country country,
			String key) {
		for (Message msg : translations) {
			if (key.equals(msg.getKey())) {
				return msg.getLocals().get(country.getCode());
			}
		}
		return null;
	}

	private static Message getMessage(List<Message> translations, String key) {
		for (Message msg : translations) {
			if (key.equals(msg.getKey())) {
				return msg;
			}
		}
		return null;
	}
}

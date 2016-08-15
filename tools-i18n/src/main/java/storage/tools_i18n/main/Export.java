package storage.tools_i18n.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.FolderModel;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.model.SheetModel;
import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.ExcelUtil;
import storage.tools_i18n.util.ResourceUtil;
import storage.tools_i18n.util.StringUtil;

public class Export {
	private static Logger log = Logger.getLogger(Export.class.getName());

	public static void main(String[] args) {
		Configuration.init(args);
		MetaData meta = ResourceUtil.downloadLatestCodes(Configuration.GIT_URL,
				Configuration.DEFAULT_BRANCH);
		List<FolderModel> folderModels = ResourceUtil.loadFolders();
		ResourceUtil.readOldEnPairs(meta.getApplyId(), folderModels);

		List<SheetModel> sheetModels = new ArrayList<SheetModel>();
		for (FolderModel folderModel : folderModels) {
			log.info(StringUtil.DELIMETER + "Parsing Module "
					+ folderModel.getFolder());
			sheetModels.add(toSheetModel(folderModel));
		}

		meta.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm",
				Locale.ENGLISH).format(new Date()));
		meta.setCreatedBy(Configuration.METADATA_CREATE_BY);
		meta.setExportId(meta.getWorkspaceCommitId());

		// generate metadata.json
		ResourceUtil.generateJsonFile(meta.converToMap(), Configuration.GIT_URL
				+ File.separator + Configuration.METADATA_FILE);
		export(Configuration.GIT_URL + File.separator
				+ Configuration.EXPORT_EXCEL_NAME, sheetModels, meta);

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
	public static void export(String outputFilePath, List<SheetModel> models,
			MetaData excelMetaData) {
		log.info(StringUtil.DELIMETER + "Generating spreadsheet "
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
		log.info("Successfully: Export Need Translated Messages in spreadsheet "
				+ outputFilePath);
	}

	private static SheetModel toSheetModel(FolderModel model) {

		log.info("\t Calculating not changed, modified, deleted, added messages......");
		List<Message> modifiedMessages = new ArrayList<Message>();
		List<Message> newMessages = new ArrayList<Message>();
		List<Message> noChangeMessages = new ArrayList<Message>();

		for (Map.Entry<String, String> me : model.getEnglishPair().entrySet()) {
			String key = me.getKey();
			String val = me.getValue();
			Message message = new Message(key, val);
			message.setOldEnVal(model.getOldEnPair().get(key));
			message.setLocals(model.getLocals(key));
			if (message.getLocals().size() < Country.locals().size()) {
				newMessages.add(message);
			} else if (message.isChanged()) {
				modifiedMessages.add(message);
			} else {
				noChangeMessages.add(message);
			}
		}

		List<Message> deletedMessages = new ArrayList<Message>();
		for (Map.Entry<String, String> me : model.getOldEnPair().entrySet()) {
			String key = me.getKey();
			if (model.getEnglishPair().containsKey(key)) {
				continue;
			}
			String val = me.getValue();
			Message message = new Message(key, val);
			message.setOldEnVal(model.getOldEnPair().get(key));
			message.setLocals(model.getLocals(key));
			deletedMessages.add(message);
		}
		log.info("\t" + noChangeMessages.size() + " messages not changed\n\t"
				+ modifiedMessages.size() + "messages modified\n\t"
				+ deletedMessages.size() + " messages deleted\n\t"
				+ newMessages.size() + " messages added\n");
		return new SheetModel(modifiedMessages, newMessages, deletedMessages,
				noChangeMessages, model.getSheetName());
	}

}

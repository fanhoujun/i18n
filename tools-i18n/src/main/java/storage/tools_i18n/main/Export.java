package storage.tools_i18n.main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.FolderModel;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.model.SheetModel;
import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.StringUtil;
import storage.tools_i18n.util.TranslationUtil;

public class Export {
	private static Logger log = Logger.getLogger(Export.class.getName());

	public static void main(String[] args) {
		Configuration.init(args);
		MetaData meta = TranslationUtil.downloadLatestCodes(
				Configuration.GIT_URL, Configuration.DEFAULT_BRANCH);
		List<FolderModel> analysisTranslationData = TranslationUtil
				.loadDataForCompare(true, meta);

		List<SheetModel> sheetModels = new ArrayList<SheetModel>();
		for (FolderModel folderModel : analysisTranslationData) {
			String folder = folderModel.getFolder();
			log.info(StringUtil.DELIMETER + "Parsing Module " + folder);
			sheetModels.add(toSheetModel(folderModel));
		}

		meta.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm",
				Locale.ENGLISH).format(new Date()));
		meta.setCreatedBy(Configuration.METADATA_CREATE_BY);
		meta.setExportId(meta.getWorkspaceCommitId());

		// generate metadata.json
		TranslationUtil.generateJsonFile(meta.converToMap(),
				Configuration.GIT_URL + File.separator
						+ Configuration.METADATA_FILE);
		String outputFile = Configuration.GIT_URL + File.separator
				+ Configuration.EXPORT_EXCEL_NAME;
		
		// generate spreadsheet
		TranslationUtil.export(outputFile, sheetModels, meta);
		log.info("Successfully: Export Need Translated Messages in spreadsheet "
				+ outputFile);
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

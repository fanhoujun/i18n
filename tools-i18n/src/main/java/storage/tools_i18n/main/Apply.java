package storage.tools_i18n.main;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.FolderModel;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.StringUtil;
import storage.tools_i18n.util.TranslationUtil;

public class Apply {
	private static Logger log = Logger.getLogger(Apply.class.getName());

	public static void main(String[] args) {
		Configuration.init(args);
		MetaData xlsMeta = TranslationUtil
				.readExcelMetaData(Configuration.TRANSLATED_XLS);
		if (StringUtil.isEmpty(xlsMeta.getExportId())) {
			log.warning("No Export ID was found in the file:"
					+ Configuration.TRANSLATED_XLS);
			return;
		}
		MetaData meta = TranslationUtil.downloadLatestCodes(
				Configuration.GIT_URL, Configuration.DEFAULT_BRANCH);

		if (!xlsMeta.getExportId().equals(meta.getExportId())) {
			log.warning("Export ID:" + xlsMeta.getExportId() + " in the file:"
					+ Configuration.TRANSLATED_XLS
					+ " does not match with Export ID:" + meta.getExportId());
			return;
		}

		List<FolderModel> folderModels = TranslationUtil.loadDataForCompare(
				false, meta);
		Map<String, List<Message>> translatedMessages = TranslationUtil
				.readExcelFromTranslateTeam(Configuration.TRANSLATED_XLS);

		for (FolderModel model : folderModels) {

			String sheetName = model.getSheetName();
			log.info("apply module " + sheetName + "...");
			applyTranslate(model, translatedMessages.get(sheetName));
		}
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
			TranslationUtil.generateJsonFile(readyEnPairs, folder
					+ File.separator + Country.ENGLISH.getCode());
		}

		for (Country country : Country.locals()) {
			Map<String, String> readyLocalPairs = new LinkedHashMap();
			for (String key : readyEnPairs.keySet()) {
				String local = getLocal(translations, country, key);
				if (StringUtil.isEmpty(local)) {
					local = model.getLocal(key, country.getCode());
				}
				if (!StringUtil.isEmpty(local)) {
					readyLocalPairs.put(key, local);
				}
			}
			TranslationUtil.generateJsonFile(readyLocalPairs, folder
					+ File.separator + country.getCode());
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

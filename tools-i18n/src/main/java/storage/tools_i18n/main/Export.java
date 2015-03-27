package storage.tools_i18n.main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import storage.tools_i18n.model.AnalysisDataModel;
import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.model.NeedTranslationModel;
import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.StringUtil;
import storage.tools_i18n.util.TranslationUtil;

public class Export {
	private static Logger log = Logger.getLogger(Export.class.getName());

	public static void main(String[] args) {
		// download the latest codes
		MetaData meta = TranslationUtil.downloadLatestCodes(
				Configuration.GIT_URL, Configuration.DEFAULT_BRANCH);

		List<String> jsonFolders = TranslationUtil.scanJsonFolders(
				Configuration.GIT_URL, Country.ENGLISH.getCode());

		List<NeedTranslationModel> needTranslationModels = new ArrayList();
		for (String jsonFolder : jsonFolders) {
			AnalysisDataModel analysisDataModel = TranslationUtil
					.loadDataForCompare(meta, jsonFolder);

			// MetaData metaData = analysisDataModel.getMetaData();
			Map<String, String> englishPair = analysisDataModel
					.getEnglishPair();
			Map<String, String> oldEnPair = analysisDataModel.getOldEnPair();
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair = analysisDataModel
					.getOtherLanguagesPreviousTranslatedPair();

			NeedTranslationModel needTranslationModel = prepareNeedTranslationData(
					oldEnPair, englishPair,
					otherLanguagesPreviousTranslatedPair);
			needTranslationModel.setSheetName(TranslationUtil
					.getSheetName(jsonFolder));
			needTranslationModels.add(needTranslationModel);
		}
		meta.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm",
				Locale.ENGLISH).format(new Date()));
		meta.setCreatedBy(Configuration.METADATA_CREATE_BY);
		TranslationUtil.generateNeedTranslateExcel(Configuration.GIT_URL
				+ File.separator + Configuration.EXPORT_EXCEL_NAME,
				needTranslationModels, meta);
		// generate metadata.json
		TranslationUtil.generateJsonFile(meta.converToMap(),
				Configuration.GIT_URL + File.separator
						+ Configuration.METADATA_FILE);

	}

	public static NeedTranslationModel prepareNeedTranslationData(
			Map<String, String> oldEnPair,
			Map<String, String> englishPair,
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair) {
		log.log(Level.INFO, "Start preparing need translation data......");

		Map<String, String> nonEnglishLocalePair = TranslationUtil
				.checkFileConsistent(otherLanguagesPreviousTranslatedPair);
		log.log(Level.INFO,
				"\t Calculating not changed, modified, deleted, added messages......");
		List<Message> modifiedMessages = new ArrayList<Message>(), newMessages = new ArrayList<Message>(), deletedMessages = new ArrayList<Message>(), noChangeMessages = new ArrayList<Message>();

		for (String key : oldEnPair.keySet()) {
			String previousEnValue = oldEnPair.get(key);
			Map<String, String> languagesVal = new HashMap<String, String>();

			boolean containsInCurrentVersion = englishPair.containsKey(key); // key
																				// contains
																				// in
																				// current
																				// version
			boolean translatedBefore = nonEnglishLocalePair.containsKey(key); // key
																				// translated
																				// before
			if (translatedBefore) {
				for (Country country : Country.values()) {
					Map<String, String> localTranslated = otherLanguagesPreviousTranslatedPair
							.get(country.getCode());
					String translatedValue = "";
					if (localTranslated != null && translatedValue != null) {
						translatedValue = localTranslated.get(key);
					} else {
						log.log(Level.WARNING,
								"Translation of Key "
										+ key
										+ " not founded due to the previous reason: Keys between different locale files not equals");
					}
					languagesVal.put(country.getCode(), translatedValue);
				}
			}

			String currentEnValue = englishPair.get(key);
			if (containsInCurrentVersion) {
				if (translatedBefore) {
					if (previousEnValue.equals(currentEnValue)) {
						noChangeMessages.add(new Message(key, previousEnValue,
								languagesVal));// no changed messages
					} else {
						modifiedMessages.add(new Message(key, previousEnValue,
								languagesVal, currentEnValue)); // modified
					}
				} else {
					newMessages.add(new Message(key, currentEnValue));
				}
			} else if (translatedBefore) {
				deletedMessages.add(new Message(key, previousEnValue,
						languagesVal));
			} else {
				log.log(Level.WARNING,
						"Translation Key: "
								+ key
								+ "="
								+ previousEnValue
								+ " added after applying translation data last time and then deleted now. It would not displayed in the generated spreadsheet.");
			}
		}

		for (String key : englishPair.keySet()) {
			if (!oldEnPair.containsKey(key)) {
				newMessages.add(new Message(key, englishPair.get(key)));
			}
		}
		log.log(Level.INFO, "\t" + noChangeMessages.size()
				+ " messages not changed\n\t" + modifiedMessages.size()
				+ "messages modified\n\t" + deletedMessages.size()
				+ " messages deleted\n\t" + newMessages.size()
				+ " messages added\n");
		log.log(Level.INFO, StringUtil.DELIMETER);
		return new NeedTranslationModel(modifiedMessages, newMessages,
				deletedMessages, noChangeMessages);
	}

}

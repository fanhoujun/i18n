package storage.tools_i18n.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import storage.tools_i18n.model.AnalysisDataModel;
import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.StringUtil;
import storage.tools_i18n.util.TranslationUtil;

public class Apply {
	private static Logger log = Logger.getLogger(Apply.class.getName());

	public static void main(String[] args) {
		MetaData spreadsheetMetaData = TranslationUtil.readExcelMetaData(Configuration.TRANSLATED_SPREADSHEET, Configuration.SHEET_METADATA_NAME);
		MetaData workspaceMetaData = TranslationUtil.downloadLatestCodes(Configuration.GIT_URL, Configuration.DEFAULT_BRANCH);
		log.log(Level.INFO, "MetaData in the current workspace: "+workspaceMetaData);
		boolean notApplyBefore =workspaceMetaData==null || StringUtil.isEmpty(workspaceMetaData.getApplyId()) || workspaceMetaData.getApplyId().equals("null");
		boolean exportIdNotMatch = spreadsheetMetaData==null 
				|| StringUtil.isEmpty(spreadsheetMetaData.getExportId()) 
				|| !spreadsheetMetaData.getExportId().equals(workspaceMetaData.getExportId()); 
		if(!notApplyBefore && exportIdNotMatch){
			throw new RuntimeException("Metadata not match between "+Configuration.TRANSLATED_SPREADSHEET+" and "+Configuration.METADATA_FILE+" spreadsheet, please check.");
		}
		
		Map<String, AnalysisDataModel> analysisTranslationData = TranslationUtil.loadDataForCompare();
		Map<String, List<Message>> translatedMessages = TranslationUtil.readExcelFromTranslateTeam(Configuration.TRANSLATED_SPREADSHEET);
		
		for(String folderPath : analysisTranslationData.keySet()){
			String moduleName = TranslationUtil.getSheetName(folderPath);
			log.log(Level.INFO, "apply module "+moduleName+"...");
			applyTranslate(folderPath, analysisTranslationData.get(folderPath), translatedMessages.get(moduleName));
		}
	}

	private static void applyTranslate(String folderPath, AnalysisDataModel analysisDataModel, List<Message> translatedMessages) {
		log.log(Level.INFO, "apply folder "+folderPath+"...");
		Map<String, String> englishPair = analysisDataModel.getEnglishPair();
		Map<String, String> oldEnPair = analysisDataModel.getOldEnPair();
		Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair = analysisDataModel
				.getOtherLanguagesPreviousTranslatedPair();
		Map<String, String> nonEnglishLocalePair = TranslationUtil
				.checkFileConsistent(otherLanguagesPreviousTranslatedPair);
		Map<String, String> survivedKeys = calculateApplyTranslationData(
				translatedMessages, englishPair, nonEnglishLocalePair,
				oldEnPair);
		// data--survivedKeys
		// apply the latest translated value
		for (Message translation : translatedMessages) {
			String key = translation.getKey();
			if (survivedKeys.containsKey(key)) {
				Map<String, String> translatedValues = translation
						.getLanguagesVal();
				for (String languageCode : translatedValues.keySet()) {
					Map<String, String> otherLanguageTraslations = otherLanguagesPreviousTranslatedPair
							.get(languageCode);
					if (otherLanguageTraslations == null) {
						otherLanguageTraslations = new HashMap<String, String>();
						otherLanguagesPreviousTranslatedPair.put(languageCode,
								otherLanguageTraslations);
					}
					String translatedValue = translatedValues.get(languageCode);
					if (!StringUtil.isEmpty(translatedValue)) {
						otherLanguageTraslations.put(key, translatedValue);
					}
				}
			}
		}

		for (String key : nonEnglishLocalePair.keySet()) {
			if (!englishPair.containsKey(key)) {
				log.log(Level.WARNING,
						"Message "
								+ key
								+ " will removed in this module since it not exists in locale_en.json.");
				for (Country ohterCountry : Country.locals()) {
					otherLanguagesPreviousTranslatedPair.get(
							ohterCountry.getCode()).remove(key);
				}
			}
		}
		// update values in the Map modified by Translation team
		for (String key : survivedKeys.keySet()) {
			englishPair.put(key, survivedKeys.get(key));
		}
		updateAllJsonFile(folderPath, englishPair,
				otherLanguagesPreviousTranslatedPair);
	}

	public static void updateAllJsonFile(
			String folder,
			Map<String, String> englishPair,
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair) {
		log.log(Level.INFO, "Start updating all the json files......");

		// keys separated in multiple json files
		log.log(Level.INFO, "|___Processing json files in foler " + folder
				+ "......");
		Map<String, String> partEnglishPair = TranslationUtil.readJSON(folder
				+ File.separator + Country.ENGLISH.getCode());
		for (Country country : Country.locals()) {
			String countryCode = country.getCode();
			Map<String, String> finalTranslations = new HashMap<String, String>();
			Map<String, String> translationValues = otherLanguagesPreviousTranslatedPair
					.get(countryCode);
			boolean modifiedByTranslationTeam = false;
			for (String key : partEnglishPair.keySet()) {
				if (!englishPair.get(key).equals(partEnglishPair.get(key))) {
					modifiedByTranslationTeam = true;
				}
				partEnglishPair.put(key, englishPair.get(key));
				if (translationValues.containsKey(key)) {
					finalTranslations.put(key, translationValues.get(key));
				}
			}
			TranslationUtil.generateJsonFile(finalTranslations, folder
					+ File.separator + countryCode);
			if (modifiedByTranslationTeam) {
				TranslationUtil.generateJsonFile(partEnglishPair, folder
						+ File.separator + Country.ENGLISH.getCode());
			}
		}

	}

	/**
	 * calculate all the keys should exists in other language json file after
	 * apply translation some key's value modified(store as the Map value) by
	 * the translation team which should update the locale_en.json as well
	 * 
	 * @param translatedMessages
	 * @param englishPair
	 * @param otherLocalePair
	 * @return
	 */
	public static Map<String, String> calculateApplyTranslationData(
			List<Message> translatedMessages, Map<String, String> englishPair,
			Map<String, String> otherLocalePair, Map<String, String> oldEngPair) {
		if(translatedMessages==null){
			translatedMessages= new ArrayList<Message>();
		}
		Map<String, String> survivedKeys = new HashMap<String, String>();
		Map<String, String> keysModifiedByTranslationTeam = new HashMap<String, String>();
		for (Message translation : translatedMessages) {
			String key = translation.getKey();
			if (englishPair.containsKey(key)) {
				String currentEnValue = englishPair.get(key);
				String translatedEnValue = translation.getEnVal();
				if (currentEnValue.equals(translatedEnValue)) {
					// key value match in translated file and current English
					// file
					survivedKeys.put(key, currentEnValue);
				} else {
					if (currentEnValue.equals(oldEngPair.get(key))) {
						// value modified by translation team
						survivedKeys.put(key, translatedEnValue);
						keysModifiedByTranslationTeam.put(key,
								translatedEnValue);
					}
				}
			}
		}
		// get keys already translated and exists in the current EN JSON file
		for (String key : otherLocalePair.keySet()) {
			if (englishPair.containsKey(key)) {
				if (!survivedKeys.containsKey(key)) {
					// the key's value may modified by translation team, it add
					// into survivedKeys with latest value in above steps
					// already
					survivedKeys.put(key, englishPair.get(key));
				}
			}
		}
		StringBuffer sb = new StringBuffer("Start calculate keys which can be updated in translation data:\n");
		if(!keysModifiedByTranslationTeam.isEmpty()){
			sb.append("\tBelow values updated by translation team:\n");
			for (String key : keysModifiedByTranslationTeam.keySet()) {
				sb.append("\t\t" + key + "=" + englishPair.get(key)
						+ "\n\t\t\tupdated to-->"
						+ keysModifiedByTranslationTeam.get(key));
			}
			log.log(Level.WARNING, sb.toString());
		}
		log.log(Level.INFO, survivedKeys.size()
				+ " keys can be updated in translation data("
				+ translatedMessages.size() + " items in total)......");
		log.log(Level.INFO, StringUtil.DELIMETER);
		return survivedKeys;
	}
}

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
		Map<String, AnalysisDataModel> analysisTranslationData = TranslationUtil.loadDataForCompare();
		
		List<NeedTranslationModel> needTranslationModels = new ArrayList<NeedTranslationModel>();
		for(String folderPath : analysisTranslationData.keySet()){
			log.log(Level.INFO, StringUtil.DELIMETER+"Parsing Module "+folderPath);
			AnalysisDataModel analysisDataModel = analysisTranslationData.get(folderPath); 
					
			NeedTranslationModel needTranslationModel = prepareNeedTranslationData(
					analysisDataModel.getOldEnPair(), 
					analysisDataModel.getEnglishPair(),
					analysisDataModel.getOtherLanguagesPreviousTranslatedPair());
			
			needTranslationModel.setSheetName(TranslationUtil.getSheetName(folderPath));
			needTranslationModels.add(needTranslationModel);
		}
		// switch to current version on the configured branch
		log.log(Level.INFO, StringUtil.DELIMETER+"switch to current version on the configured branch");
		MetaData metaData = TranslationUtil.downloadLatestCodes(Configuration.GIT_URL, Configuration.DEFAULT_BRANCH);
		
		metaData.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm",Locale.ENGLISH).format(new Date()));
		metaData.setCreatedBy(Configuration.METADATA_CREATE_BY);
		metaData.setExportId(metaData.getWorkspaceCommitId());
		
		// generate metadata.json
		TranslationUtil.generateJsonFile(metaData.converToMap(),
				Configuration.GIT_URL + File.separator + Configuration.METADATA_FILE);
		String outputFile = Configuration.GIT_URL+ File.separator + Configuration.EXPORT_EXCEL_NAME;
		// generate spreadsheet
		TranslationUtil.generateNeedTranslateExcel(outputFile, needTranslationModels, metaData);
		log.log(Level.INFO, "Successfully: Export Need Translated Messages in spreadsheet "+outputFile);
	}

	public static NeedTranslationModel prepareNeedTranslationData(
			Map<String, String> oldEnPair,
			Map<String, String> englishPair,
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair) {

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
		return new NeedTranslationModel(modifiedMessages, newMessages,
				deletedMessages, noChangeMessages);
	}

}

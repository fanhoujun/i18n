package storage.tools_i18n.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import storage.tools_i18n.constant.ConfigurationConstant;
import storage.tools_i18n.constant.Constant;
import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.model.NeedTranslationModel;

public class TranslationTool {
	
	private static Logger log = Logger.getLogger(TranslationTool.class.getName());
	private static List<Country> supportedOtherCountries= Country.otherCountries();
	
	public static void exportNeedTranslatedExcel() {
		//download the latest codes
		MetaData metaData = TranslationUtil.downloadLatestCodes(ConfigurationConstant.GIT_URL, ConfigurationConstant.DEFAULT_BRANCH);
		List<String> currentJsonFolders = TranslationUtil.scanJsonFolders(Constant.Directory_Current_Version, Constant.LOCALE_EN);
		Map<String, String> englishPair = TranslationUtil.readAllKeys(currentJsonFolders, Country.ENGLISH);
		
		//if applied translation before, download the last applied version 
		String lastTranslatedCommitId= metaData.getLastTranslatedCommitId();
		Map<String, String> oldEnPair = new HashMap<String, String>();
		Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair= new HashMap<String, Map<String, String>>();
		
		if(lastTranslatedCommitId!=null && !lastTranslatedCommitId.trim().equals("")){
			String downloadPath = TranslationUtil.downloadPreviousCodes(ConfigurationConstant.GIT_URL, lastTranslatedCommitId);
			// previous version JSON file folder structure may not match current structure
			List<String> previousJsonFileFolders = TranslationUtil.scanJsonFolders(downloadPath, Constant.LOCALE_EN);
			oldEnPair = TranslationUtil.readAllKeys(previousJsonFileFolders, Country.ENGLISH);
			for(Country otherCountries : supportedOtherCountries){
				otherLanguagesPreviousTranslatedPair.put(otherCountries.getCounrtyCode(), TranslationUtil.readAllKeys(previousJsonFileFolders, otherCountries));
			}
		}
		
		NeedTranslationModel needTranslationModel = prepareNeedTranslationData(oldEnPair, englishPair, otherLanguagesPreviousTranslatedPair);
		
		metaData.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH).format(new Date()));
		metaData.setCreatedBy("bhu@hp.com");
		
		needTranslationModel.setMetaData(metaData);
		TranslationUtil.generateNeedTranslateExcel(ConfigurationConstant.EXPORT_EXCEL_NAME, ConfigurationConstant.SHEET_STORAGE_NAME,
				needTranslationModel.getModifiedMessages(), 
				needTranslationModel.getNewMessages(),
				needTranslationModel.getDeletedMessages(),
				needTranslationModel.getNoChangeMessages(),
				needTranslationModel.getMetaData());
	}
	
	public static void applyTranslatedMessage(String excelFilePath){
		List<Message> translatedMessages = TranslationUtil.readExcelFromTranslateTeam(excelFilePath);
		
		MetaData metaData = TranslationUtil.downloadLatestCodes(ConfigurationConstant.GIT_URL, ConfigurationConstant.DEFAULT_BRANCH);
		List<String> currentJsonFolders = TranslationUtil.scanJsonFolders(Constant.Directory_Current_Version, Constant.LOCALE_EN);
		Map<String, String> englishPair = TranslationUtil.readAllKeys(currentJsonFolders, Country.ENGLISH);
		
		String lastTranslatedCommitId= metaData.getLastTranslatedCommitId();
		Map<String, String> oldEnPair = new HashMap<String, String>();
		Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair= new HashMap<String, Map<String, String>>();
		
		if(lastTranslatedCommitId!=null && !lastTranslatedCommitId.trim().equals("")){
			String downloadPath = TranslationUtil.downloadPreviousCodes(ConfigurationConstant.GIT_URL, metaData.getLastTranslatedCommitId());
			List<String> previousJsonFileFolders = TranslationUtil.scanJsonFolders(downloadPath, Constant.LOCALE_EN);
			oldEnPair = TranslationUtil.readAllKeys(previousJsonFileFolders, Country.ENGLISH);
			
			// non English Locale file should not modified by developers, so we read the previous commit version
			for(Country otherCountries : supportedOtherCountries){
				otherLanguagesPreviousTranslatedPair.put(otherCountries.getCounrtyCode(), TranslationUtil.readAllKeys(previousJsonFileFolders, otherCountries));
			}
		}
		
		Map<String, String> nonEnglishLocalePair = checkFileConsistent(otherLanguagesPreviousTranslatedPair);
		Map<String, String> survivedKeys = calculateApplyTranslationData(translatedMessages, englishPair, nonEnglishLocalePair, oldEnPair);
		// update all locale_xx.json file based on the calculated data--survivedKeys
		// apply the latest translated value
		for(Message translation : translatedMessages){
			String key = translation.getKey();
			if(survivedKeys.containsKey(key)){
				Map<String, String> translatedValues = translation.getLanguagesVal();
				for(String languageCode : translatedValues.keySet()){
					Map<String, String> otherLanguageTraslations = otherLanguagesPreviousTranslatedPair.get(languageCode);
					if(otherLanguageTraslations==null){
						otherLanguageTraslations = new HashMap<String, String>();
						otherLanguagesPreviousTranslatedPair.put(languageCode, otherLanguageTraslations);
					}
					otherLanguageTraslations.put(key, translatedValues.get(languageCode));
				}
			}
		}
		// synchronize the keys in EN file and locale_xx.json: remove unused keys in locale_xx.json
		for(String key : nonEnglishLocalePair.keySet()){
			if(!englishPair.containsKey(key)){
				log.log(Level.WARNING, "Message "+key+" will removed in locale_xx.json(locale_zh.json) since it not exists in locale_en.json.");
				for(Country ohterCountry : supportedOtherCountries){
					otherLanguagesPreviousTranslatedPair.get(ohterCountry.getCounrtyCode()).remove(key);
				}
			}
		}
		//update values in the Map modified by Translation team
		for(String key : survivedKeys.keySet()){
			englishPair.put(key, survivedKeys.get(key));
		}
		updateAllJSONFiles(currentJsonFolders, englishPair, otherLanguagesPreviousTranslatedPair);
		
	}
	public static void updateAllJSONFiles(List<String> folderPaths, Map<String, String> englishPair, 
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair){
		log.log(Level.INFO, "Start updating all the json files......");
		
		// keys separated in multiple json files
		for(String folder : folderPaths){
			log.log(Level.INFO, "|___Processing json files in foler "+folder+"......");
			Map<String, String> partEnglishPair = TranslationUtil.readJSON(folder+"\\locale_en.json");
			for(Country country : supportedOtherCountries){
				String countryCode = country.getCounrtyCode();
				Map<String, String> finalTranslations = new HashMap<String, String>();
				Map<String, String> translationValues = otherLanguagesPreviousTranslatedPair.get(countryCode);
				log.log(Level.INFO, "|_______processing locale_"+countryCode+".json......");
				for(String key : partEnglishPair.keySet()){
					//update locale_en.json
					partEnglishPair.put(key, englishPair.get(key));
					if(translationValues.containsKey(key)){
						finalTranslations.put(key, translationValues.get(key));
					}
				}
				TranslationUtil.generateJsonFile(finalTranslations, folder+"\\locale_"+countryCode+".json");
			}
		}
		
		log.log(Level.INFO, Constant.DELIMETER);
	}
	/**
	 * calculate all the keys should exists in other language json file after apply translation
	 * 		some key's value modified(store as the Map value) by the translation team which should update the locale_en.json as well
	 * @param translatedMessages
	 * @param englishPair
	 * @param otherLocalePair
	 * @return
	 */
	private static Map<String, String> calculateApplyTranslationData(List<Message> translatedMessages, 
			Map<String, String> englishPair, Map<String, String> otherLocalePair, Map<String, String> oldEngPair){
		log.log(Level.INFO, "Start calculate keys which can be updated in translation data......");
		Map<String, String> survivedKeys = new HashMap<String, String>();
		List<String> keysModifiedByTranslationTeam = new ArrayList<String>();
		for(Message translation: translatedMessages){
			String key=translation.getKey();
			if(englishPair.containsKey(key)){
				String currentEnValue=englishPair.get(key);
				String translatedEnValue=translation.getEnVal();
				if(currentEnValue.equals(translatedEnValue)){
					// key value match in translated file and current English file
					survivedKeys.put(key, currentEnValue);
				}else{
					if(currentEnValue.equals(oldEngPair.get(key))){
						// value modified by translation team
						survivedKeys.put(key, translatedEnValue);
						keysModifiedByTranslationTeam.add(key);
					}
				}
			}
		}
		// get keys already translated and exists in the current EN JSON file
		for(String key : otherLocalePair.keySet()){
			if(englishPair.containsKey(key)){
				survivedKeys.put(key, englishPair.get(key));
			}
		}
		log.log(Level.WARNING, "Below values updated by translation team: ");
		for(String key : keysModifiedByTranslationTeam){
			log.log(Level.WARNING, "\t"+key+"="+englishPair+"\n\t\tupdated to-->");
		}
		log.log(Level.INFO, survivedKeys.size()+" keys can be updated in translation data("+translatedMessages.size()+" items in total)......");
		log.log(Level.INFO, Constant.DELIMETER);
		return survivedKeys;
	}
	
	public static NeedTranslationModel prepareNeedTranslationData( Map<String, String> oldEnPair, 
			Map<String, String> englishPair, Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair){
		log.log(Level.INFO, "Start preparing need translation data......");
		
		Map<String, String> nonEnglishLocalePair = checkFileConsistent(otherLanguagesPreviousTranslatedPair);
		log.log(Level.INFO, "\t Calculating not changed, modified, deleted, added messages......");
		List<Message> modifiedMessages = new ArrayList<Message>(), newMessages = new ArrayList<Message>() ,deletedMessages = new ArrayList<Message>(), noChangeMessages = new ArrayList<Message>();
		
		for(String key : oldEnPair.keySet()){
			String previousEnValue = oldEnPair.get(key);
			Map<String, String> languagesVal= new HashMap<String, String>();
			
			boolean containsInCurrentVersion = englishPair.containsKey(key);  // key contains in current version
			boolean translatedBefore = nonEnglishLocalePair.containsKey(key); // key translated before 
			if(translatedBefore){
				for(Country country : Country.values()){
					Map<String, String> localTranslated = otherLanguagesPreviousTranslatedPair.get(country.getCounrtyCode());
					String translatedValue="";
					if(localTranslated!=null && translatedValue!=null){
						translatedValue = localTranslated.get(key);
					}else{
						log.log(Level.WARNING, "Translation of Key "+key+" not founded due to the previous reason: Keys between different locale files not equals");
					}
					languagesVal.put(country.getCounrtyCode(), translatedValue);
				}
			}
			
			String currentEnValue = englishPair.get(key);
			if(containsInCurrentVersion){
				if(translatedBefore){
					if(previousEnValue.equals(currentEnValue)){ 
						noChangeMessages.add(new Message(key, previousEnValue, languagesVal));// no changed messages
					}else{
						modifiedMessages.add(new Message(key, previousEnValue, languagesVal, currentEnValue)); // modified
					}
				}else{ 
					newMessages.add(new Message(key, currentEnValue));
				}
			}else if(translatedBefore){
				deletedMessages.add(new Message(key, previousEnValue, languagesVal));
			}else{
				log.log(Level.WARNING, "Translation Key: "+key+"="+previousEnValue+" added after applying translation data last time and then deleted now. It would not displayed in the generated spreadsheet.");
			}
		}
		
		for(String key : englishPair.keySet()){
			if(!oldEnPair.containsKey(key)){
				newMessages.add(new Message(key, englishPair.get(key)));
			}
		}
		log.log(Level.INFO, "\t"+noChangeMessages.size()+" messages not changed\n\t"+modifiedMessages.size()+ "messages modified\n\t"+ 
				deletedMessages.size()+" messages deleted\n\t"+newMessages.size()+" messages added\n");
		log.log(Level.INFO, Constant.DELIMETER);
		return new NeedTranslationModel(modifiedMessages,newMessages,deletedMessages,noChangeMessages);
	}

	public static Map<String, String> checkFileConsistent(Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair) {
		log.log(Level.INFO, "\t Checking all the locale except English if the are consistent......");
		boolean setDefault=false; String nonEnglishLocale="";
		Map<String, String> nonEnglishLocalePair = new HashMap<String, String>();
		for(String key : otherLanguagesPreviousTranslatedPair.keySet()){
			Map<String, String> pairs = otherLanguagesPreviousTranslatedPair.get(key);
			// One more language(locale_ja) in Excel while josn file locale_ja.json is not exists 
			if(!setDefault && !pairs.isEmpty()){ 
				nonEnglishLocalePair=pairs;
				nonEnglishLocale=key;
				setDefault=true;
				continue;
			}
			if(pairs.size()<nonEnglishLocalePair.size()){
				log.log(Level.WARNING, "Keys between different locale "+ key + " and "+ nonEnglishLocale+" not equals. One of these files might be modified or supported countries greater than before. Please double check.");
				nonEnglishLocalePair=pairs;
				nonEnglishLocale=key;
			}
		}
		return nonEnglishLocalePair;
	}
}

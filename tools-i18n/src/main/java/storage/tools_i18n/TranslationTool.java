package storage.tools_i18n;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TranslationTool {
	private static Country selectedCountry= Country.GERMAN;
	public static void main(String[] args) {
		
	}
	
	public static void exportNeedTranslatedExcel(String filePath){
		MetaData metaData = TranslationUtil.downloadLatestCodes(Constant.GIT_URL);
		TranslationUtil.downloadPreviousCodes(Constant.GIT_URL, metaData.getLastTranslatedCommitId());
		
		Map<String, String> oldEnPair = TranslationUtil.readJSON("previous/locale_en.json");
		Map<String, String> englishPair = TranslationUtil.readJSON("current/locale_en.json");
		
		Map<String, Map<String, String>> otherLanguagesTranslatedPair= new HashMap<String, Map<String, String>>();
		for(Country otherCountries : Country.otherCountries()){
			otherLanguagesTranslatedPair.put(otherCountries.getCounrtyCode(), TranslationUtil.readJSON("previous/locale_"+otherCountries.getCounrtyCode()+".json"));
		}
		
		NeedTranslationModel needTranslationModel = prepareNeedTranslationData(oldEnPair, englishPair, otherLanguagesTranslatedPair);
		
		metaData.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH).format(new Date()));
		metaData.setCreatedBy("bhu@hp.com");
		metaData.setCurrentCommitId("a63fda3afdafkve");
		metaData.setLastTranslatedCommitId("reivmxwoeew33afdsa");
		
		needTranslationModel.setMetaData(metaData);
		TranslationUtil.generateNeedTranslateExcel(Constant.EXPORT_EXCEL_NAME, 
				needTranslationModel.getModifiedMessages(), 
				needTranslationModel.getNewMessages(),
				needTranslationModel.getDeletedMessages(),
				needTranslationModel.getNewMessages(),
				needTranslationModel.getMetaData());
	}
	
	public static void applyTranslatedMessage(String excelFilePath){
		MetaData metaData = TranslationUtil.downloadLatestCodes(Constant.GIT_URL);
		TranslationUtil.downloadPreviousCodes(Constant.GIT_URL, metaData.getLastTranslatedCommitId());
		
		List<Message> translatedMessages = TranslationUtil.readExcelFromTranslateTeam(excelFilePath);
		Map<String, String> oldEnPair = TranslationUtil.readJSON("previous/locale_en.json");
		Map<String, String> englishPair = TranslationUtil.readJSON("current/locale_en.json");
		
		Map<String, Map<String, String>> allLanguagesPair= new HashMap<String, Map<String, String>>();
		allLanguagesPair.put(Country.ENGLISH.getCounrtyCode(), englishPair);
		// non English Locale file should not modified by developers, so we read the previous commit version
		for(Country otherCountries : Country.otherCountries()){
			allLanguagesPair.put(otherCountries.getCounrtyCode(), TranslationUtil.readJSON("previous/locale_"+otherCountries.getCounrtyCode()+".json"));
		}
		
		//typical other language to compare
		Map<String, String> nonEnglishLocalePair = allLanguagesPair.get(selectedCountry.getCounrtyCode());
		
		Set<String> survivedKeys = calculateApplyTranslationData(translatedMessages, englishPair, nonEnglishLocalePair, oldEnPair);
		// update all locale_xx.json file based on the calculated data--survivedKeys
		// apply the latest translated value
		for(Message translation : translatedMessages){
			String key = translation.getKey();
			if(survivedKeys.contains(key)){
				Map<String, String> translatedValues = translation.getLanguagesVal();
				for(String languageCode : translatedValues.keySet()){
					allLanguagesPair.get(languageCode).put(key, translatedValues.get(key));
				}
			}
		}
		// synchronize the keys in EN file and locale_xx.json: remove unused keys in locale_xx.json
		for(String key : nonEnglishLocalePair.keySet()){
			if(!englishPair.containsKey(key)){
				for(Country ohterCountry : Country.otherCountries()){
					allLanguagesPair.get(ohterCountry.getCounrtyCode()).remove(key);
				}
			}
		}
		// write files
		for(Country country : Country.values()){
			TranslationUtil.generateJsonFile(allLanguagesPair.get(country.getCounrtyCode()));
		}
		
	}
	/**
	 * calculate all the keys should exists in other language json file after apply translation
	 * 		some keys(ApplyTranslationKeys.keysUpdatedByTranslationTeam) modified by the translation team which should update the locale_en.json as well
	 * @param translatedMessages
	 * @param englishPair
	 * @param otherLocalePair
	 * @return
	 */
	private static Set<String> calculateApplyTranslationData(List<Message> translatedMessages, 
			Map<String, String> englishPair, Map<String, String> otherLocalePair, Map<String, String> oldEngPair){
		
		Set<String> survivedKeys = new HashSet<String>();
		
		for(Message translation: translatedMessages){
			String key=translation.getKey();
			if(englishPair.containsKey(key)){
				String currentEnValue=englishPair.get(translation.getKey());
				if(currentEnValue.equals(translation.getEnVal())){
					// key value match in translated file and current English file
					survivedKeys.add(key);
				}else{
					if(currentEnValue.equals(oldEngPair.get(key))){
						// value modified by translation team
						survivedKeys.add(key);
					}
				}
			}
		}
		// get keys already translated and exists in the current EN JSON file
		for(String key : otherLocalePair.keySet()){
			if(englishPair.containsKey(key)){
				survivedKeys.add(key);
			}
		}
		
		return survivedKeys;
	}
	
	private static NeedTranslationModel prepareNeedTranslationData( Map<String, String> oldEnPair, 
			Map<String, String> englishPair, Map<String, Map<String, String>> otherLanguagesTranslatedPair){
		//typical other language to compare
		Map<String, String> nonEnglishLocalePair = otherLanguagesTranslatedPair.get(selectedCountry.getCounrtyCode());
				
		List<Message> modifiedMessages = new ArrayList<Message>();
		List<Message> newMessages = new ArrayList<Message>();
		List<Message> deletedMessages = new ArrayList<Message>(); 
		List<Message> noChangeMessages = new ArrayList<Message>();
		
		for(String key : oldEnPair.keySet()){
			String previousEnValue = oldEnPair.get(key);
			Map<String, String> languagesVal= new HashMap<String, String>();
			for(Country country : Country.values()){
				languagesVal.put(country.getCounrtyCode(), 
						otherLanguagesTranslatedPair.get(country.getCounrtyCode()).get(key));
			}
			
			boolean containsInCurrentVersion = englishPair.containsKey(key);  // key contains in current version
			boolean translatedBefore = nonEnglishLocalePair.containsKey(key); // key translated before 
			String currentEnValue = englishPair.get(key);
			
			if(containsInCurrentVersion){
				if(translatedBefore){
					if(previousEnValue.equals(currentEnValue)){ 
						noChangeMessages.add(new Message(key, currentEnValue, languagesVal));// no changed messages
					}else{
						modifiedMessages.add(new Message(key, previousEnValue, languagesVal, currentEnValue)); // modified
					}
				}else{ 
					newMessages.add(new Message(key, currentEnValue));
				}
			}else if(translatedBefore){
				deletedMessages.add(new Message(key, currentEnValue, languagesVal));
			}
		}
		
		for(String key : englishPair.keySet()){
			if(!oldEnPair.containsKey(key)){
				newMessages.add(new Message(key, englishPair.get(key)));
			}
		}
		return new NeedTranslationModel(modifiedMessages,newMessages,deletedMessages,noChangeMessages);
	}
}

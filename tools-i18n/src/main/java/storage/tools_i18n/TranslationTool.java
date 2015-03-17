package storage.tools_i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranslationTool {
	public static void main(String[] args) {
	}
	
	public static void exportNeedTranslatedExcel(String filePath){
		MetaData metaData = TranslationUtil.downloadLatestCodes(Constant.GIT_URL);
		TranslationUtil.downloadPreviousCodes(metaData.getLastTranslatedCommitId());
		
		List<Message> modifiedMessages=new ArrayList<Message>(), 
				newMessages=new ArrayList<Message>(), 
				deletedMessages=new ArrayList<Message>(), 
				noChangeMessages=new ArrayList<Message>();
		
		prepareNeedTranslationData(modifiedMessages, newMessages, deletedMessages, noChangeMessages);
		
		metaData.setCreateDate("17th March 2015");
		metaData.setCreatedBy("bhu@hp.com");
		metaData.setCurrentCommitId("a63fda3afdafkve");
		metaData.setLastTranslatedCommitId("reivmxwoeew33afdsa");
		
		TranslationUtil.generateNeedTranslateExcel(Constant.EXPORT_EXCEL_NAME, 
				modifiedMessages, newMessages, deletedMessages, noChangeMessages, 
				metaData);
	}
	
	public static void applyTranslatedMessage(String excelFilePath){
		MetaData metaData = TranslationUtil.downloadLatestCodes(Constant.GIT_URL);
		List<Message> translatedMessages = TranslationUtil.readExcelFromTranslateTeam(excelFilePath);
		
		Map<String, String> englishPair = TranslationUtil.readJSON("current/locale_en.json");
		
		List<Map<String, String>> allLanguagesPair= new ArrayList<Map<String, String>>();
			allLanguagesPair.add(TranslationUtil.readJSON("current/locale_zh.json"));
		
		List<String> survivedKeys = calculateApplyTranslationData(translatedMessages, englishPair, allLanguagesPair.get(0));
		
		for(Map<String, String> map : allLanguagesPair){
			// update all locale_xx.json file based on the calculated data--survivedKeys
			TranslationUtil.generateJsonFile(map);
		}
	}
	private static List<String> calculateApplyTranslationData(List<Message> translatedMessages, 
			Map<String, String> englishPair, Map<String, String> otherLocalePair){
		return new ArrayList<String>();
	}
	
	private static String prepareNeedTranslationData(
			List<Message> modifiedMessages, 
			List<Message> newMessages, 
			List<Message> deletedMessages, 
			List<Message> noChangeMessages){
		Map<String, String> newEnPair = TranslationUtil.readJSON("current/locale_en.json");
		Map<String, String> oldEnPair = TranslationUtil.readJSON("previous/locale_en.json");
		Map<String, String> otherLanguagePair = TranslationUtil.readJSON("previous/locale_zh.json");
		
		StringBuilder sb=new StringBuilder("Output Warning Message or Debug Message");
		return sb.toString();
	}
}

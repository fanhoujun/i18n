package storage.tools_i18n.model;

import java.util.Map;

public class AnalysisDataModel {
	private MetaData metaData;
	private Map<String, String> englishPair;
	private Map<String, String> oldEnPair;
	private Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair;
	public MetaData getMetaData() {
		return metaData;
	}
	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}
	
	public Map<String, String> getEnglishPair() {
		return englishPair;
	}
	public void setEnglishPair(Map<String, String> englishPair) {
		this.englishPair = englishPair;
	}
	public Map<String, String> getOldEnPair() {
		return oldEnPair;
	}
	public void setOldEnPair(Map<String, String> oldEnPair) {
		this.oldEnPair = oldEnPair;
	}
	public Map<String, Map<String, String>> getOtherLanguagesPreviousTranslatedPair() {
		return otherLanguagesPreviousTranslatedPair;
	}
	public void setOtherLanguagesPreviousTranslatedPair(
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair) {
		this.otherLanguagesPreviousTranslatedPair = otherLanguagesPreviousTranslatedPair;
	}
	
}

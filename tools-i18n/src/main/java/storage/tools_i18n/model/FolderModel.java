package storage.tools_i18n.model;

import java.util.LinkedHashMap;
import java.util.Map;

import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.StringUtil;

public class FolderModel {
	private Map<String, String> englishPair;
	private Map<String, String> oldEnPair;
	/**
	 * (local_code,(key,value))
	 */
	private Map<String, Map<String, String>> allLocals;

	private String folder;

	public String getFolder() {
		return folder;
	}

	public String getSheetName() {
		String path = folder.substring(Configuration.GIT_URL.length())
				.replaceAll("[\\\\/]+", "_") + "_";
		if (Configuration.IGNORE_KEY_WRODS == null
				|| Configuration.IGNORE_KEY_WRODS.length == 0) {
			return path;
		}
		for (String ignoreKeyword : Configuration.IGNORE_KEY_WRODS) {
			if (ignoreKeyword != null) {
				path = path.replaceAll("_" + ignoreKeyword.trim() + "_", "_");
			}
		}
		return path.replaceAll("_+", "");
	}

	public void setFolder(String folder) {
		this.folder = folder;
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
		return allLocals;
	}

	public void setOtherLanguagesPreviousTranslatedPair(
			Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair) {
		this.allLocals = otherLanguagesPreviousTranslatedPair;
	}

	/**
	 * @param key
	 * @return (code,value)
	 */
	public Map<String, String> getLocals(String key) {
		Map<String, String> locals = new LinkedHashMap();
		for (Country country : Country.locals()) {
			Map<String, String> values = allLocals.get(country.getCode());
			if (values != null) {
				String localVal = values.get(key);
				if (!StringUtil.isEmpty(localVal)) {
					locals.put(country.getCode(), localVal);
				}
			}
		}
		return locals;
	}

	/**
	 * @param key
	 * @param countryCode
	 * @return value
	 */
	public String getLocal(String key, String countryCode) {
		String code = countryCode;
		Map<String, String> values = allLocals.get(code);
		if (values != null) {
			return values.get(key);
		}
		return null;
	}
}

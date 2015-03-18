package storage.tools_i18n;

import java.util.Map;

public class Message {
	/**
	 *  translation key
	 */
	private String key;
	/**
	 *  translated English value for the key
	 */
	private String enVal;
	/**
	 * all other languages translation,  key is the Country.XXX.getCountryCode(), value is the translation
	 */
	private Map<String, String> languagesVal;
	
	/**
	 *  current English value if modified by developer
	 */
	private String modifiedEnVal;

	public Message(String key, String enVal) {
		new Message(key, enVal, null);
	}
	
	public Message(String key, String enVal, Map<String, String> languagesVal) {
		new Message(key, enVal, languagesVal, null);
	}

	public Message(String key, String enVal, Map<String, String> languagesVal, String modifiedEnVal) {
		super();
		this.key = key;
		this.enVal = enVal;
		this.languagesVal = languagesVal;
		this.modifiedEnVal = modifiedEnVal;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getEnVal() {
		return enVal;
	}

	public void setEnVal(String enVal) {
		this.enVal = enVal;
	}

	public String getModifiedEnVal() {
		return modifiedEnVal;
	}

	public void setModifiedEnVal(String modifiedEnVal) {
		this.modifiedEnVal = modifiedEnVal;
	}

	public Map<String, String> getLanguagesVal() {
		return languagesVal;
	}

	public void setLanguagesVal(Map<String, String> languagesVal) {
		this.languagesVal = languagesVal;
	}

}

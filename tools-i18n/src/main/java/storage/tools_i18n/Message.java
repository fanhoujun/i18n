package storage.tools_i18n;

import java.util.Map;

public class Message {
	private String key;
	private String enVal;
	private Map<String, String> languagesVal;
	
	private String modifiedEnVal;

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

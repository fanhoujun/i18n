package storage.tools_i18n.model;

import java.util.Map;

import storage.tools_i18n.util.HashUtil;

public class Message {
	/**
	 * translation key
	 */
	private String key;

	private String enVal;
	/**
	 * all other languages translation, key is the Country.XXX.getCountryCode(),
	 * value is the translation
	 */
	private Map<String, String> locals;

	private String oldEnVal;

	public Message() {
	}

	public Message(String key, String enVal) {
		this(key, enVal, null);
	}

	public Message(String key, String enVal, Map<String, String> locals) {
		this(key, enVal, locals, null);
	}

	public Message(String key, String enVal, Map<String, String> locals,
			String oldEnval) {
		super();
		this.key = key;
		this.enVal = enVal;
		this.locals = locals;
		this.oldEnVal = oldEnval;
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

	public String getOldEnVal() {
		return oldEnVal;
	}

	public void setOldEnVal(String modifiedEnVal) {
		this.oldEnVal = modifiedEnVal;
	}

	public Map<String, String> getLocals() {
		return locals;
	}

	public void setLocals(Map<String, String> locals) {
		this.locals = locals;
	}

	public boolean isChanged() {
		return HashUtil.isEqual(this.getEnVal(), this.getOldEnVal());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("key=").append(key).append(" enVal=").append(enVal)
				.append(" oldEnVal=").append(oldEnVal).append("\nlocals=")
				.append(locals).append("\n");
		return sb.toString();
	}

}

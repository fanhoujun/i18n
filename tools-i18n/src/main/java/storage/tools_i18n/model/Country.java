package storage.tools_i18n.model;

import java.util.ArrayList;
import java.util.List;

public class Country {

	public Country(String name, String code) {
		this.code = code+".json";
		this.name = name;
	}

	private final String code;
	private final String name;

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	private static List<Country> locals = new ArrayList();

	public static Country ENGLISH;

	public static void addLocal(String ctryName, String ctryCode) {
		locals.add(new Country(ctryName, ctryCode));
	}

	public static List<Country> locals() {
		return locals;
	}

	public static List<Country> values() {
		List<Country> contries = new ArrayList();
		contries.add(Country.ENGLISH);
		contries.addAll(locals);
		return contries;
	}
}

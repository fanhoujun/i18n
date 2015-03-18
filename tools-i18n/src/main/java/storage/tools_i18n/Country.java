package storage.tools_i18n;

import java.util.ArrayList;
import java.util.List;

public enum Country {
	
	ENGLISH("UK ENGLISH", "en"), ENGLISH_US("US - ENGLISH", "en-us"), 
	//JAPANESE("JAPANESE", "ja"), ITALIAN("ITALIAN", "it"),
	FRENCH("FRANCE-FRENCH", "fr-fr"), GERMAN("GERMAN", "de"), SPANISH("SPANISH", "es");
	
	private Country(String ctryName, String countryCode){
		this.counrtyCode=countryCode;
		this.ctryName=ctryName;
	}
	private String counrtyCode;
	private String ctryName;
	public String getCounrtyCode() {
		return counrtyCode;
	}
	public void setCounrtyCode(String counrtyCode) {
		this.counrtyCode = counrtyCode;
	}
	public String getCtryName() {
		return ctryName;
	}
	public void setCtryName(String ctryName) {
		this.ctryName = ctryName;
	}
	public static List<Country> otherCountries(){
		List<Country> otherCountries = new ArrayList<Country>();
		for(Country country : Country.values()){
			if(!country.getCounrtyCode().equals(ENGLISH.getCounrtyCode())){
				otherCountries.add(country);
			}
		}
		return otherCountries;
	}
}

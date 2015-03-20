package storage.tools_i18n;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import storage.tools_i18n.constant.ConfigurationConstant;
import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.model.NeedTranslationModel;
import storage.tools_i18n.util.TranslationTool;
import storage.tools_i18n.util.TranslationUtil;


public class TranslationToolTest {
	@Test
	public void readExcelFromTranslateTeam(){
		
		List<Message> messages = TranslationUtil.readExcelFromTranslateTeam("MyExcel.xlsx");
		for(Message msg : messages){
			System.out.println(msg.getKey()+"="+msg.getEnVal());
		}
		Assert.assertEquals(98, messages.size());
	}
	@Test
	public void calculateApplyTranslationData(){
		List<Message> translatedMessages;
		Map<String, String> englishPair;Map<String, String> otherLocalePair;Map<String, String> oldEngPair;
		englishPair = new HashMap<String, String>(); oldEngPair = new HashMap<String, String>();
		englishPair.put("Menu.home", "Home");
		englishPair.put("Menu.ManageUser", "User Management System");
		englishPair.put("PlaceHolder.Search", "Search");
		englishPair.put("Menu.Navigator", "Navigator Bar");
		englishPair.put("Menu.Settings", "Settings");
		
		oldEngPair.put("Menu.home", "Home");
		oldEngPair.put("Menu.SiteMap", "WebSiteMap");
		oldEngPair.put("Menu.ManageUser", "User Management System");
		oldEngPair.put("PlaceHolder.Search", "Search");
		oldEngPair.put("Menu.Navigator", "Navigator");
		
		otherLocalePair= new HashMap<String, String>();
		otherLocalePair.put("Menu.home", "首页");
		otherLocalePair.put("Menu.ManageUser", "用户管理");
		otherLocalePair.put("Menu.Admin", "管理员");
		
		translatedMessages = new ArrayList<Message>();
		//Message msg =new Message();
		
		//translatedMessages.add(msg);
		oldEngPair.put("Menu.SiteMap", "WebSiteMap");
		oldEngPair.put("Menu.ManageUser", "User Management System");
		oldEngPair.put("PlaceHolder.Search", "Search");
		oldEngPair.put("Menu.Navigator", "Navigator");
	}
	@Test
	public void prepareNeedTranslationData(){
		Map<String, String> oldEnPair= new HashMap<String, String>(), englishPair = new HashMap<String, String>(); 
		Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair=new HashMap<String, Map<String, String>>();
		
		oldEnPair.put("Menu.home", "Home");
		oldEnPair.put("Menu.SiteMap", "SiteMap");
		oldEnPair.put("Menu.ManageUser", "Manage User");
		oldEnPair.put("Menu.UserCenter", "User Center");
		oldEnPair.put("Menu.Admin", "Administrator");
		oldEnPair.put("Menu.Navigator", "Navigator");
		
		englishPair.put("Menu.home", "Home");
		englishPair.put("Menu.SiteMap", "WebSiteMap");
		englishPair.put("Menu.ManageUser", "User Management System");
		englishPair.put("PlaceHolder.Search", "Search");
		englishPair.put("Menu.Navigator", "Navigator");
		
		Map<String, String> otherPair= new HashMap<String, String>();
		
		otherPair.put("Menu.home", "首页");
		otherPair.put("Menu.ManageUser", "用户管理");
		otherPair.put("Menu.Admin", "管理员");
		
		otherLanguagesPreviousTranslatedPair.put(Country.GERMAN.getCounrtyCode(), otherPair);
		
		NeedTranslationModel res =TranslationTool.prepareNeedTranslationData(oldEnPair, englishPair, otherLanguagesPreviousTranslatedPair); 
		System.out.println("getDeletedMessages:");
		
		Assert.assertEquals(res.getDeletedMessages().get(0).getKey(), "Menu.Admin");
		for(Message msg : res.getDeletedMessages()){
			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
		}
		System.out.println("getModifiedMessages:");
		Assert.assertEquals(res.getModifiedMessages().get(0).getKey(), "Menu.ManageUser");
		for(Message msg : res.getModifiedMessages()){
			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal()+"-->Modified to="+msg.getModifiedEnVal());
		}
		System.out.println("getNewMessages:");
		Assert.assertEquals(res.getNewMessages().size(), 3);
		for(Message msg : res.getNewMessages()){
			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
		}
		System.out.println("getNoChangeMessages:");
		Assert.assertEquals(res.getNoChangeMessages().get(0).getKey(), "Menu.home");
		for(Message msg : res.getNoChangeMessages()){
			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
		}
		
	}
	@Test
	public void checkFileConsistent(){
		Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair = new HashMap<String, Map<String, String>>();
		
		Map<String, String> map = new HashMap<String, String>();
		
		map = new HashMap<String, String>();
		otherLanguagesPreviousTranslatedPair.put(Country.FRENCH.getCounrtyCode(), map);
		
		map.put("Menu.Help", "Help");
		otherLanguagesPreviousTranslatedPair.put(Country.SPANISH.getCounrtyCode(), map);
		
		map = new HashMap<String, String>();
		map.put("Menu.Help", "Help");
		map.put("Menu.Nav", "Navigator");
		otherLanguagesPreviousTranslatedPair.put(Country.GERMAN.getCounrtyCode(), map);
		
		System.out.println();
		Assert.assertEquals(1, TranslationTool.checkFileConsistent(otherLanguagesPreviousTranslatedPair).size());
		
	}
	@Test
	public void generateNeedTranslationData(){
		List<Message> modifiedMessages = new ArrayList<Message>(), 
				newMessages = new ArrayList<Message>() ,
				deletedMessages = new ArrayList<Message>(), 
				noChangeMessages = new ArrayList<Message>();
		for(int i=0;i<10;i++){
			Message msg = new Message("M00"+i, "MV00MV"+i);
			msg.setModifiedEnVal("MV2"+i+"MV");
			
			Map<String, String> languagesVal = new HashMap<String, String>();
			for(Country country : Country.values()){
				languagesVal.put(country.getCounrtyCode(), "MV200"+country.getCounrtyCode());
			}
			msg.setLanguagesVal(languagesVal);
			modifiedMessages.add(msg);
		}
		
		for(int i=0;i<10;i++){
			Message msg = new Message("N00"+i, "New00MV"+i);
			newMessages.add(msg);
		}
		
		for(int i=0;i<10;i++){
			Message msg = new Message("D00"+i, "DV00MV"+i);
			
			Map<String, String> languagesVal = new HashMap<String, String>();
			for(Country country : Country.values()){
				languagesVal.put(country.getCounrtyCode(), "DE200"+country.getCounrtyCode());
			}
			msg.setLanguagesVal(languagesVal);
			deletedMessages.add(msg);
		}
		for(int i=0;i<10;i++){
			Message msg = new Message("NoC00"+i, "Noc00MV"+i);
			
			Map<String, String> languagesVal = new HashMap<String, String>();
			for(Country country : Country.values()){
				languagesVal.put(country.getCounrtyCode(), "Noc00"+country.getCounrtyCode());
			}
			msg.setLanguagesVal(languagesVal);
			noChangeMessages.add(msg);
		}
		
		MetaData metaData = new MetaData();
		metaData.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH).format(new Date()));
		metaData.setCreatedBy("bhu@hp.com");
		metaData.setCurrentCommitId("cxfdsafasdfasdfdas");
		metaData.setLastTranslatedCommitId("lastdafdasfasdkfsdakfa");
		TranslationUtil.generateNeedTranslateExcel(ConfigurationConstant.EXPORT_EXCEL_NAME, 
				ConfigurationConstant.SHEET_STORAGE_NAME, 
				modifiedMessages, newMessages, deletedMessages, noChangeMessages, metaData);
		
	}
}

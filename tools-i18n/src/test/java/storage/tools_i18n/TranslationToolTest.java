//package storage.tools_i18n;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import storage.tools_i18n.constant.ConfigurationConstant;
//import storage.tools_i18n.model.Country;
//import storage.tools_i18n.model.Message;
//import storage.tools_i18n.model.MetaData;
//import storage.tools_i18n.model.NeedTranslationModel;
//import storage.tools_i18n.util.TranslationTool;
//import storage.tools_i18n.util.TranslationUtil;
//
//
//public class TranslationToolTest {
//	@Ignore
//	public void readExcelFromTranslateTeam(){
//		System.out.println("==========readExcelFromTranslateTeam=========");
//		List<Message> messages = TranslationUtil.readExcelFromTranslateTeam("MyExcel.xlsx");
//		for(Message msg : messages){
//			System.out.println(msg.getKey()+"="+msg.getEnVal());
//		}
//		Assert.assertEquals(98, messages.size());
//	}
//	@Test
//	public void calculateApplyTranslationData(){
//		System.out.println("==========calculateApplyTranslationData=========");
//		Map<String, String> englishPair = new HashMap<String, String>(); 
//		englishPair.put("Menu.home", "Home");
//		englishPair.put("Menu.ManageUser", "User Management System");
//		englishPair.put("PlaceHolder.Search", "Search");
//		englishPair.put("Menu.Navigator", "Navigator Bar");
//		englishPair.put("Menu.Settings", "Settings");
//		
//		Map<String, String> oldEngPair = new HashMap<String, String>();
//		oldEngPair.put("Menu.home", "Home");
//		oldEngPair.put("Menu.SiteMap", "WebSiteMap");
//		oldEngPair.put("Menu.ManageUser", "User Management System");
//		oldEngPair.put("PlaceHolder.Search", "Search");
//		oldEngPair.put("Menu.Navigator", "Navigator");
//		
//		Map<String, String> otherLocalePair = new HashMap<String, String>();
//		otherLocalePair.put("Menu.home", "首页");
//		otherLocalePair.put("Menu.ManageUser", "用户管理");
//		otherLocalePair.put("Menu.Admin", "管理员");
//		
//		String modifiedByTranslationTeam="User Management: updated from translation team";
//		List<Message> translatedMessages = new ArrayList<Message>();
//		Message msg =new Message();
//		msg.setKey("Menu.ManageUser");
//		msg.setEnVal(modifiedByTranslationTeam);
//		Map<String, String> languagesVal = new HashMap<String, String>();
//		languagesVal.put(Country.FRENCH.getCounrtyCode(), "用户管理系统");
//		msg.setLanguagesVal(languagesVal);
//		translatedMessages.add(msg);
//		
//		msg =new Message();
//		msg.setKey("PlaceHolder.Search");
//		msg.setEnVal("Search");
//		languagesVal = new HashMap<String, String>();
//		languagesVal.put(Country.FRENCH.getCounrtyCode(), "搜索");
//		msg.setLanguagesVal(languagesVal);
//		translatedMessages.add(msg);
//		
//		msg =new Message();
//		msg.setKey("Menu.SiteMap");
//		msg.setEnVal("WebSiteMap");
//		languagesVal = new HashMap<String, String>();
//		languagesVal.put(Country.FRENCH.getCounrtyCode(), "网站地图");
//		msg.setLanguagesVal(languagesVal);
//		translatedMessages.add(msg);
//		
//		msg =new Message();
//		msg.setKey("Menu.Navigator");
//		msg.setEnVal("Navigator");
//		languagesVal = new HashMap<String, String>();
//		languagesVal.put(Country.FRENCH.getCounrtyCode(), "导航");
//		msg.setLanguagesVal(languagesVal);
//		translatedMessages.add(msg);
//		
//		Map<String, String> res = TranslationTool.calculateApplyTranslationData(translatedMessages, englishPair, otherLocalePair, oldEngPair);
//		for(String key : res.keySet()){
//			System.out.println(key+"="+res.get(key));
//		}
//		Assert.assertEquals(res.get("Menu.ManageUser"), modifiedByTranslationTeam);
//		Assert.assertEquals(res.size(), 3);
//	}
//	@Test
//	public void prepareNeedTranslationData(){
//		System.out.println("==========prepareNeedTranslationData=========");
//		Map<String, String> oldEnPair= new HashMap<String, String>(), englishPair = new HashMap<String, String>(); 
//		Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair=new HashMap<String, Map<String, String>>();
//		
//		oldEnPair.put("Menu.home", "Home");
//		oldEnPair.put("Menu.SiteMap", "SiteMap");
//		oldEnPair.put("Menu.ManageUser", "Manage User");
//		oldEnPair.put("Menu.UserCenter", "User Center");
//		oldEnPair.put("Menu.Admin", "Administrator");
//		oldEnPair.put("Menu.Navigator", "Navigator");
//		
//		englishPair.put("Menu.home", "Home");
//		englishPair.put("Menu.SiteMap", "WebSiteMap");
//		englishPair.put("Menu.ManageUser", "User Management System");
//		englishPair.put("PlaceHolder.Search", "Search");
//		englishPair.put("Menu.Navigator", "Navigator");
//		
//		Map<String, String> otherPair= new HashMap<String, String>();
//		
//		otherPair.put("Menu.home", "首页");
//		otherPair.put("Menu.ManageUser", "用户管理");
//		otherPair.put("Menu.Admin", "管理员");
//		
//		otherLanguagesPreviousTranslatedPair.put(Country.GERMAN.getCounrtyCode(), otherPair);
//		
//		NeedTranslationModel res =TranslationTool.prepareNeedTranslationData(oldEnPair, englishPair, otherLanguagesPreviousTranslatedPair); 
//		System.out.println("getDeletedMessages:");
//		
//		Assert.assertEquals(res.getDeletedMessages().get(0).getKey(), "Menu.Admin");
//		for(Message msg : res.getDeletedMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
//		}
//		System.out.println("getModifiedMessages:");
//		Assert.assertEquals(res.getModifiedMessages().get(0).getKey(), "Menu.ManageUser");
//		for(Message msg : res.getModifiedMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal()+"-->Modified to="+msg.getModifiedEnVal());
//		}
//		System.out.println("getNewMessages:");
//		Assert.assertEquals(res.getNewMessages().size(), 3);
//		for(Message msg : res.getNewMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
//		}
//		System.out.println("getNoChangeMessages:");
//		Assert.assertEquals(res.getNoChangeMessages().get(0).getKey(), "Menu.home");
//		for(Message msg : res.getNoChangeMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
//		}
//		
//	}
//	@Test
//	public void prepareNeedTranslationData_OtherLocaleEmpty(){
//		System.out.println("==========prepareNeedTranslationData_OtherLocaleEmpty=========");
//		Map<String, String> oldEnPair= new HashMap<String, String>(), englishPair = new HashMap<String, String>(); 
//		
//		oldEnPair.put("Menu.home", "Home");
//		oldEnPair.put("Menu.SiteMap", "SiteMap");
//		oldEnPair.put("Menu.ManageUser", "Manage User");
//		oldEnPair.put("Menu.UserCenter", "User Center");
//		oldEnPair.put("Menu.Admin", "Administrator");
//		oldEnPair.put("Menu.Navigator", "Navigator");
//		
//		englishPair.put("Menu.home", "Home");
//		englishPair.put("Menu.SiteMap", "WebSiteMap");
//		englishPair.put("Menu.ManageUser", "User Management System");
//		englishPair.put("PlaceHolder.Search", "Search");
//		englishPair.put("Menu.Navigator", "Navigator");
//		
//		
//		NeedTranslationModel res =TranslationTool.prepareNeedTranslationData(oldEnPair, englishPair, new HashMap<String, Map<String, String>>()); 
//		System.out.println("getDeletedMessages:");
//		
//		//Assert.assertEquals(res.getDeletedMessages().get(0).getKey(), "Menu.Admin");
//		for(Message msg : res.getDeletedMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
//		}
//		System.out.println("getModifiedMessages:");
//		//Assert.assertEquals(res.getModifiedMessages().get(0).getKey(), "Menu.ManageUser");
//		for(Message msg : res.getModifiedMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal()+"-->Modified to="+msg.getModifiedEnVal());
//		}
//		System.out.println("getNewMessages:");
//		//Assert.assertEquals(res.getNewMessages().size(), 3);
//		for(Message msg : res.getNewMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
//		}
//		System.out.println("getNoChangeMessages:");
//		//Assert.assertEquals(res.getNoChangeMessages().get(0).getKey(), "Menu.home");
//		for(Message msg : res.getNoChangeMessages()){
//			System.out.println("\t"+msg.getKey()+"="+msg.getEnVal());
//		}
//		
//	}
//	@Test
//	public void checkFileConsistent(){
//		System.out.println("==========checkFileConsistent=========");
//		
//		Map<String, Map<String, String>> otherLanguagesPreviousTranslatedPair = new HashMap<String, Map<String, String>>();
//		
//		Map<String, String> map = new HashMap<String, String>();
//		
//		map = new HashMap<String, String>();
//		otherLanguagesPreviousTranslatedPair.put(Country.FRENCH.getCounrtyCode(), map);
//		
//		map.put("Menu.Help", "Help");
//		otherLanguagesPreviousTranslatedPair.put(Country.SPANISH.getCounrtyCode(), map);
//		
//		map = new HashMap<String, String>();
//		map.put("Menu.Help", "Help");
//		map.put("Menu.Nav", "Navigator");
//		otherLanguagesPreviousTranslatedPair.put(Country.GERMAN.getCounrtyCode(), map);
//		
//		Map<String, String> res = TranslationTool.checkFileConsistent(otherLanguagesPreviousTranslatedPair);
//		System.out.println(res);
//		Assert.assertEquals(1, res.size());
//		
//	}
//	@Test
//	public void generateNeedTranslationData(){
//		System.out.println("==========generateNeedTranslationData=========");
//		
//		List<Message> modifiedMessages = new ArrayList<Message>(), 
//				newMessages = new ArrayList<Message>() ,
//				deletedMessages = new ArrayList<Message>(), 
//				noChangeMessages = new ArrayList<Message>();
//		for(int i=0;i<10;i++){
//			Message msg = new Message("M00"+i, "MV00MV"+i);
//			msg.setModifiedEnVal("MV2"+i+"MV");
//			
//			Map<String, String> languagesVal = new HashMap<String, String>();
//			for(Country country : Country.values()){
//				languagesVal.put(country.getCounrtyCode(), "MV200"+country.getCounrtyCode());
//			}
//			msg.setLanguagesVal(languagesVal);
//			modifiedMessages.add(msg);
//		}
//		
//		for(int i=0;i<10;i++){
//			Message msg = new Message("N00"+i, "New00MV"+i);
//			newMessages.add(msg);
//		}
//		
//		for(int i=0;i<10;i++){
//			Message msg = new Message("D00"+i, "DV00MV"+i);
//			
//			Map<String, String> languagesVal = new HashMap<String, String>();
//			for(Country country : Country.values()){
//				languagesVal.put(country.getCounrtyCode(), "DE200"+country.getCounrtyCode());
//			}
//			msg.setLanguagesVal(languagesVal);
//			deletedMessages.add(msg);
//		}
//		for(int i=0;i<10;i++){
//			Message msg = new Message("NoC00"+i, "Noc00MV"+i);
//			
//			Map<String, String> languagesVal = new HashMap<String, String>();
//			for(Country country : Country.values()){
//				languagesVal.put(country.getCounrtyCode(), "Noc00"+country.getCounrtyCode());
//			}
//			msg.setLanguagesVal(languagesVal);
//			noChangeMessages.add(msg);
//		}
//		
//		MetaData metaData = new MetaData();
//		metaData.setCreateDate(new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH).format(new Date()));
//		metaData.setCreatedBy("bhu@hp.com");
//		metaData.setCommitId("cxfdsafasdfasdfdas");
//		NeedTranslationModel model=new NeedTranslationModel(modifiedMessages, newMessages, deletedMessages, noChangeMessages);
//		model.setSheetName("testName");
//		List<NeedTranslationModel> needTranslationModels=new ArrayList();
//		
//		needTranslationModels.add(model);
//		TranslationUtil.generateNeedTranslateExcel(ConfigurationConstant.EXPORT_EXCEL_NAME, 
//				needTranslationModels, metaData);
//		
//	}
//}

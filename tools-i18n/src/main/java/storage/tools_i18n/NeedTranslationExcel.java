package storage.tools_i18n;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class NeedTranslationExcel {
	
	public static void generateMetaDataSheet(Workbook workbook, MetaData metadata){
		
		Sheet sheet = workbook.createSheet(Constant.SHEET_METADATA);
		Row row; int col=0, rowNum=0;
		
		row = sheet.createRow(rowNum++);col=0;
		row.createCell(col++).setCellValue(MetaData.META_LAST_TRANSLATED_COMMIT_ID);
		row.createCell(col++).setCellValue(metadata.getLastTranslatedCommitId());
		
		row = sheet.createRow(rowNum++);col=0;
		row.createCell(col++).setCellValue(MetaData.META_CURRENT_COMMIT_ID);
		row.createCell(col++).setCellValue(metadata.getCurrentCommitId());
		
		row = sheet.createRow(rowNum++);col=0;
		row.createCell(col++).setCellValue(MetaData.META_CREATE_BY);
		row.createCell(col++).setCellValue(metadata.getCreatedBy());
		
		row = sheet.createRow(rowNum++);col=0;
		row.createCell(col++).setCellValue(MetaData.META_CREATE_DATE);
		row.createCell(col++).setCellValue(metadata.getCreateDate());
	}
	
	public static int generateDeletedMessages(Sheet sheet, int rowNum, List<Message> deletedMessages){
		Row row; int col=0;
		List<Country> countries = Country.otherCountries();
		
		if(!deletedMessages.isEmpty()){
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col).setCellValue("Deleted Messages");
			
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col++).setCellValue("Key");
			row.createCell(col++).setCellValue("en_EN");
			for(int i=0;i<countries.size();i++){
				row.createCell(col++).setCellValue(countries.get(i).getCounrtyCode());
			}
			
			for(Message message : deletedMessages){
				row = sheet.createRow(rowNum++);col=0;
				row.createCell(col++).setCellValue(message.getKey());
				row.createCell(col++).setCellValue(message.getEnVal());
				
				for(int i=0;i<countries.size();i++){
					row.createCell(col++).setCellValue(message.getLanguagesVal().get(countries.get(i).getCounrtyCode()));
				}
			}
		}
		return rowNum;
	}
	
	public static int generateNoChangeMessages(Sheet sheet, int rowNum, List<Message> noChangeMessages){
		Row row; int col=0;
		List<Country> countries = Country.otherCountries();
		if(!noChangeMessages.isEmpty()){
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col).setCellValue("No Change Messages");
			
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col++).setCellValue("Key");
			row.createCell(col++).setCellValue("en_EN");
			for(int i=0;i<countries.size();i++){
				row.createCell(col++).setCellValue(countries.get(i).getCounrtyCode());
			}
			
			for(Message message : noChangeMessages){
				row = sheet.createRow(rowNum++);col=0;
				row.createCell(col++).setCellValue(message.getKey());
				row.createCell(col++).setCellValue(message.getEnVal());
				
				for(int i=0;i<countries.size();i++){
					row.createCell(col++).setCellValue(message.getLanguagesVal().get(countries.get(i).getCounrtyCode()));
				}
			}
			
		}
		return rowNum;
	}
	public static int generateNewMessages(Sheet sheet, int rowNum, List<Message> newMessages){
		Row row; int col=0;
		
		if(!newMessages.isEmpty()){
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col).setCellValue("New Messages");
			
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col++).setCellValue("Key");
			row.createCell(col++).setCellValue("en_EN");
			
			for(Message message : newMessages){
				row = sheet.createRow(rowNum++);col=0;
				row.createCell(col++).setCellValue(message.getKey());
				row.createCell(col++).setCellValue(message.getEnVal());
			}
			
		}
		return rowNum;
	}
	public static int generateModifiedMessages(Sheet sheet, int rowNum, List<Message> modifiedMessages){
		Row row;  int col=0;
		
		List<Country> countries = Country.otherCountries();
		if(!modifiedMessages.isEmpty()){
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col).setCellValue("Modified Messages (last column on right contains new english message that needs translation)");
			
			row = sheet.createRow(rowNum++);col=0;
			row.createCell(col++).setCellValue("Key");
			row.createCell(col++).setCellValue("old en_EN");
			for(int i=0;i<countries.size();i++){
				row.createCell(col++).setCellValue("old "+countries.get(i).getCounrtyCode());
			}
			row.createCell(col++).setCellValue("modified en_EN");
			
			for(Message message : modifiedMessages){
				row = sheet.createRow(rowNum++);col=0;
				row.createCell(col++).setCellValue(message.getKey());
				row.createCell(col++).setCellValue(message.getEnVal());
				
				for(int i=0;i<countries.size();i++){
					row.createCell(col++).setCellValue(message.getLanguagesVal().get(countries.get(i).getCounrtyCode()));
				}
				
				row.createCell(col++).setCellValue(message.getModifiedEnVal());
			}
		}
		return rowNum;
	}
	public static void createEmptyRow(Sheet sheet, int rowNum){
		sheet.createRow(rowNum);
	}
}

package storage.tools_i18n;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class GenerateNeedTranslationExcel {
	
	public static void generateMetaDataSheet(Workbook workbook, MetaData metadata){
		
		Sheet sheet = workbook.createSheet(Constant.SHEET_METADATA);
		Row row; int col=0, rowNum=0;
		sheet.setDefaultColumnWidth(0x20);
		
		CellStyle style = createMessageItemStyle(workbook);
		
		row = sheet.createRow(rowNum++);col=0;
		setCell(row.createCell(col++), MetaData.META_LAST_TRANSLATED_COMMIT_ID, style);
		setCell(row.createCell(col++), metadata.getLastTranslatedCommitId(), style);
		
		row = sheet.createRow(rowNum++);col=0;
		setCell(row.createCell(col++), MetaData.META_CURRENT_COMMIT_ID, style);
		setCell(row.createCell(col++), metadata.getCurrentCommitId(), style);
		
		row = sheet.createRow(rowNum++);col=0;
		setCell(row.createCell(col++), MetaData.META_CREATE_BY, style);
		setCell(row.createCell(col++), metadata.getCreatedBy(), style);
		
		row = sheet.createRow(rowNum++);col=0;
		setCell(row.createCell(col++), MetaData.META_CREATE_DATE, style);
		setCell(row.createCell(col++), metadata.getCreateDate(), style);
		
	}
	
	public static int generateDeletedMessages(Sheet sheet, int rowNum, List<Message> deletedMessages){
		Row row; int col=0; 
		List<Country> countries = Country.otherCountries();
		
		if(!deletedMessages.isEmpty()){
			CellStyle style =createSubSectionStyle(sheet.getWorkbook(), IndexedColors.RED.index);
			
			row = sheet.createRow(rowNum++);col=0;
			setCell(row.createCell(col++), "Deleted Messages", style);
			
			for(int i=0;i<Country.values().length;i++){
				setCell(row.createCell(col++), null, style);
			}
			
			style = createSubBarStyle(sheet.getWorkbook());
			row = sheet.createRow(rowNum++);col=0;
			setCell(row.createCell(col++), "Key", style);
			setCell(row.createCell(col++), "en_EN", style);
			
			for(int i=0;i<countries.size();i++){
				setCell(row.createCell(col++), countries.get(i).getCounrtyCode(), style);
			}
			
			style = createMessageItemStyle(sheet.getWorkbook());
			for(Message message : deletedMessages){
				row = sheet.createRow(rowNum++);col=0;
				setCell(row.createCell(col++), message.getKey(), style);
				
				setCell(row.createCell(col++), message.getEnVal(), style);
				
				for(int i=0;i<countries.size();i++){
					setCell(row.createCell(col++), message.getLanguagesVal().get(countries.get(i).getCounrtyCode()), style);
				}
			}
		}
		return rowNum;
	}
	
	private static void setCell(Cell cell, String value, CellStyle style){
		if(value!=null && !"".equals(value)){
			cell.setCellValue(value);
		}
		cell.setCellStyle(style);
	}
	public static int generateNoChangeMessages(Sheet sheet, int rowNum, List<Message> noChangeMessages){
		Row row; int col=0;
		List<Country> otherCountries = Country.otherCountries();
		if(!noChangeMessages.isEmpty()){
			CellStyle style =createSubSectionStyle(sheet.getWorkbook(), IndexedColors.WHITE.index);
			
			row = sheet.createRow(rowNum++);col=0;
			setCell(row.createCell(col++), "No Change Messages", style);
			
			for(int i=0;i<Country.values().length;i++){
				setCell(row.createCell(col++), null, style);
			}
			
			style = createSubBarStyle(sheet.getWorkbook());
			row = sheet.createRow(rowNum++);col=0;
			setCell(row.createCell(col++), "Key", style);
			
			setCell(row.createCell(col++), "en_EN", style);
			
			for(int i=0;i<otherCountries.size();i++){
				setCell(row.createCell(col++), otherCountries.get(i).getCounrtyCode(), style);
			}
			
			style = createMessageItemStyle(sheet.getWorkbook());
			for(Message message : noChangeMessages){
				row = sheet.createRow(rowNum++);col=0;
				setCell(row.createCell(col++), message.getKey(), style);
				setCell(row.createCell(col++), message.getEnVal(), style);
				
				for(int i=0;i<otherCountries.size();i++){
					setCell(row.createCell(col++), message.getLanguagesVal().get(otherCountries.get(i).getCounrtyCode()), style);
				}
			}
			
		}
		return rowNum;
	}
	public static int generateNewMessages(Sheet sheet, int rowNum, List<Message> newMessages){
		Row row; int col=0;
		
		if(!newMessages.isEmpty()){
			CellStyle style =createSubSectionStyle(sheet.getWorkbook(), IndexedColors.ORANGE.index);
			
			row = sheet.createRow(rowNum++);col=0;
			
			setCell(row.createCell(col++), "New Messages", style);
			setCell(row.createCell(col++), "", style);
			
			style = createSubBarStyle(sheet.getWorkbook());
			row = sheet.createRow(rowNum++);col=0;
			
			setCell(row.createCell(col++), "Key", style);
			setCell(row.createCell(col++), "en_EN", style);
			
			style = createMessageItemStyle(sheet.getWorkbook());
			for(Message message : newMessages){
				row = sheet.createRow(rowNum++);col=0;
				setCell(row.createCell(col++), message.getKey(), style);
				setCell(row.createCell(col++), message.getEnVal(), style);
			}
			
		}
		return rowNum;
	}
	public static CellStyle createSubBarStyle(Workbook wb){
		if(subBarStyle==null){
			subBarStyle = wb.createCellStyle();
			subBarStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			subBarStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    Font font = wb.createFont();
		    font.setColor(IndexedColors.BLACK.index);
		    font.setBold(true);
		    font.setFontHeightInPoints((short)9);
		    subBarStyle.setFont(font);
		}
		return subBarStyle;
	}
	public static CellStyle createMessageItemStyle(Workbook wb){
		if(messageItemStyle==null){
			messageItemStyle = wb.createCellStyle();
			messageItemStyle.setBorderRight((short)1);
			messageItemStyle.setBorderBottom((short)1);
			messageItemStyle.setWrapText(true);
			messageItemStyle.setBorderLeft((short)1);
			messageItemStyle.setBorderTop((short)1);
		}
		return messageItemStyle;
	}
	public static CellStyle createSubSectionStyle(Workbook wb, short fontColor){
		subSectionStyle = wb.createCellStyle();
		subSectionStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		subSectionStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    Font font = createSubSectionFont(wb, fontColor, (short)18);
	    subSectionStyle.setFont(font);
		return subSectionStyle;
	}
	private static Font createSubSectionFont(Workbook wb, short fontColor, short fontPoints){
		Font font = wb.createFont();
	    font.setColor(fontColor);
	    font.setBold(true);
	    font.setFontHeightInPoints(fontPoints);
	    return font;
	}
	private static CellStyle subBarStyle;
	private static CellStyle subSectionStyle;
	private static CellStyle messageItemStyle;
	public static int generateModifiedMessages(Sheet sheet, int rowNum, List<Message> modifiedMessages){
		Row row;  int col=0; 
		
		Workbook wb = sheet.getWorkbook();
		
		List<Country> otherCountries = Country.otherCountries();
		if(!modifiedMessages.isEmpty()){
			CellStyle style =createSubSectionStyle(sheet.getWorkbook(), IndexedColors.GREEN.index);
					
			row = sheet.createRow(rowNum++);col=0;
			String modifiedStr = "Modified Messages ";
			String desc = "(last column on right contains new english message that needs translation)";
			RichTextString rts =new XSSFRichTextString(modifiedStr+desc);
			for(int i=0;i<Country.values().length+2;i++){
				if(i==0){
					Cell cell = row.createCell(col++);
					
					Font font = createSubSectionFont(wb, IndexedColors.GREEN.index, (short)18);
					rts.applyFont(font);
					
					font = createSubSectionFont(wb, IndexedColors.WHITE.index, (short)11);
					
					rts.applyFont(modifiedStr.length(), rts.length(), font);
					cell.setCellValue(rts);
					cell.setCellStyle(style);
					
				}else{
					setCell(row.createCell(col++), null, style);
				}
			}
			
			style = createSubBarStyle(wb);
		    
			row = sheet.createRow(rowNum++);col=0;
			setCell(row.createCell(col++), "Key", style);
			setCell(row.createCell(col++), "old en_EN", style);
			
			for(int i=0;i<otherCountries.size();i++){
				setCell(row.createCell(col++), "old "+otherCountries.get(i).getCounrtyCode(), style);
			}
			setCell(row.createCell(col++), "modified en_EN", style);
			
			style = createMessageItemStyle(sheet.getWorkbook());
			for(Message message : modifiedMessages){
				row = sheet.createRow(rowNum++);col=0;
				setCell(row.createCell(col++), message.getKey(), style);
				setCell(row.createCell(col++), message.getEnVal(), style);
				
				for(int i=0;i<otherCountries.size();i++){
					setCell(row.createCell(col++), message.getLanguagesVal().get(otherCountries.get(i).getCounrtyCode()), style);
				}
				setCell(row.createCell(col++), message.getModifiedEnVal(), style);
			}
		}
		return rowNum;
	}
	public static void createEmptyRow(Sheet sheet, int rowNum){
		sheet.createRow(rowNum);
	}

}

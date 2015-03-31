package storage.tools_i18n.util;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.Message;
import storage.tools_i18n.model.MetaData;

public class ExcelUtil {

	public static void generateMetaDataSheet(Workbook workbook,
			MetaData metadata) {

		Sheet sheet = workbook.createSheet(Configuration.SHEET_METADATA_NAME);
		Row row;
		int col = 0, rowNum = 0;
		sheet.setDefaultColumnWidth(0x28);

		CellStyle style = createMessageItemStyle(workbook);

		row = sheet.createRow(rowNum++);
		col = 0;
		setCell(row.createCell(col++), MetaData.META_EXPORT_ID, style);
		setCell(row.createCell(col++), metadata.getWorkspaceCommitId(), style);

		row = sheet.createRow(rowNum++);
		col = 0;
		setCell(row.createCell(col++), MetaData.META_CREATE_BY, style);
		setCell(row.createCell(col++), metadata.getCreatedBy(), style);

		row = sheet.createRow(rowNum++);
		col = 0;
		setCell(row.createCell(col++), MetaData.META_CREATE_DATE, style);
		setCell(row.createCell(col++), metadata.getCreateDate(), style);

	}

	private static int createTitle(Sheet sheet, int rowNum, CellStyle style,
			String title) {

		Row row = sheet.createRow(rowNum++);
		int col = 0;
		setCell(row.createCell(col++), title, style);
		for (int i = 0; i < Country.locals().size() + 2; i++) {
			setCell(row.createCell(col++), "", style);
		}
		sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0,
				Country.locals().size() + 1));
		return rowNum;
	}

	private static void setCell(Cell cell, String value, CellStyle style) {
		if (!StringUtil.isEmpty(value)) {
			cell.setCellValue(value);
		}
		cell.setCellStyle(style);
	}

	private static CellStyle createSubBarStyle(Workbook wb) {
		if (subBarStyle == null) {
			subBarStyle = wb.createCellStyle();
			subBarStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
					.getIndex());
			subBarStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			Font font = wb.createFont();
			font.setColor(IndexedColors.BLACK.index);
			font.setBold(true);
			font.setFontHeightInPoints((short) 9);
			subBarStyle.setFont(font);
		}
		return subBarStyle;
	}

	private static CellStyle createOldItemStyle(Workbook wb) {
		if (oldItemStyle == null) {
			oldItemStyle = wb.createCellStyle();
			oldItemStyle.setBorderRight((short) 1);
			oldItemStyle.setBorderBottom((short) 1);
			oldItemStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
					.getIndex());
			oldItemStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			oldItemStyle.setWrapText(true);
			oldItemStyle.setBorderLeft((short) 1);
			oldItemStyle.setBorderTop((short) 1);
		}
		return oldItemStyle;
	}

	private static CellStyle createMessageItemStyle(Workbook wb) {
		if (messageItemStyle == null) {
			messageItemStyle = wb.createCellStyle();
			messageItemStyle.setBorderRight((short) 1);
			messageItemStyle.setBorderBottom((short) 1);
			messageItemStyle.setWrapText(true);
			messageItemStyle.setBorderLeft((short) 1);
			messageItemStyle.setBorderTop((short) 1);
		}
		return messageItemStyle;
	}

	private static CellStyle createSubSectionStyle(Workbook wb, short fontColor) {
		subSectionStyle = wb.createCellStyle();
		subSectionStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		subSectionStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		Font font = createSubSectionFont(wb, fontColor, (short) 18);
		subSectionStyle.setFont(font);
		return subSectionStyle;
	}

	private static Font createSubSectionFont(Workbook wb, short fontColor,
			short fontPoints) {
		Font font = wb.createFont();
		font.setColor(fontColor);
		font.setBold(true);
		font.setFontHeightInPoints(fontPoints);
		return font;
	}

	private static CellStyle subBarStyle;
	private static CellStyle subSectionStyle;
	private static CellStyle messageItemStyle;

	private static CellStyle oldItemStyle;

	public static int createPart(Sheet sheet, int rowNum,
			List<Message> messages, String title, short titleColor) {
		Workbook wb = sheet.getWorkbook();
		CellStyle style = createSubSectionStyle(wb, titleColor);
		rowNum = createTitle(sheet, rowNum, style, title);
		style = createMessageItemStyle(wb);
		CellStyle oldStyle = createOldItemStyle(wb);
		for (Message message : messages) {
			Row row = sheet.createRow(rowNum++);
			int col = 0;
			setCell(row.createCell(col++), message.getKey(), style);
			setCell(row.createCell(col++),
					message.isChanged() ? message.getOldEnVal() : "--", oldStyle);
			setCell(row.createCell(col++), message.getEnVal(), style);
			boolean haveLocalVal = message.getLocals() != null;
			for (Country country : Country.locals()) {
				setCell(row.createCell(col++), haveLocalVal ? message
						.getLocals().get(country.getCode()) : null, style);
			}
		}
		createEmptyRow(sheet, rowNum++);
		return rowNum;
	}

	public static int creatColumnHeaders(Sheet sheet, int rowNum, Workbook wb) {
		CellStyle style = createSubBarStyle(wb);
		Row row = sheet.createRow(rowNum++);
		int col = 0;
		final String EN_US = Country.ENGLISH.getName();
		setCell(row.createCell(col++), "Key", style);
		setCell(row.createCell(col++), "Old " + EN_US, style);
		setCell(row.createCell(col++), EN_US, style);

		for (Country country : Country.locals()) {
			setCell(row.createCell(col++), country.getName(), style);
		}
		return rowNum;
	}

	private static void createEmptyRow(Sheet sheet, int rowNum) {
		sheet.createRow(rowNum);
	}

}

package upc.edu.gessi.repo.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ExcelUtils {

    public static Workbook generateExcelSheet() {
        return new XSSFWorkbook();
    }
    public static CellStyle generateTitleCellStyle(final Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    public static XSSFFont generateTitleArial16Font(final Workbook workbook) {
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        return font;
    }

    public static void insertRowInSheet(Sheet sheet, final List<String> rowData, final Integer rowIndex) {
        Row row = sheet.createRow(rowIndex);
        for (String data : rowData) {
            Cell cell = row.createCell(rowData.indexOf(data));
            cell.setCellValue(data);
        }
    }

    public static void insertHeaderRowInSheet(Sheet sheet, CellStyle headerCellStyle, XSSFFont titleFont, final List<String> titles) {
        Row row = sheet.createRow(0);
        headerCellStyle.setFont(titleFont);
        for (String title : titles) {
            Cell cell = row.createCell(titles.indexOf(title));
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue(title);
        }
    }

    public static Sheet createWorkbookSheet(final Workbook workbook, final String sheetTitle) {
        Sheet sheet = workbook.createSheet(sheetTitle);
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 6000);
        return sheet;
    }

    public static byte[] createByteArrayFromWorkbook(final Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
}

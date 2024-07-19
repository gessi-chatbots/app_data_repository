package upc.edu.gessi.repo.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class ExcelUtils {

    public static Workbook generateExcelSheet() {
        return new XSSFWorkbook();
    }
    public static CellStyle generateTitleCellStyle(final Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
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
    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static void insertRowInSheet(Sheet sheet, final List<String> rowData, final Integer rowIndex) {
        Workbook workbook = sheet.getWorkbook();
        Row row = sheet.createRow(rowIndex);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DataFormat dataFormat = workbook.createDataFormat();

        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy"));

        for (int i = 0; i < rowData.size(); i++) {
            String data = rowData.get(i);
            Cell cell = row.createCell(i);

            if (isDate(data, dateFormat)) {
                try {
                    Date date = dateFormat.parse(data);
                    cell.setCellValue(date);
                    cell.setCellStyle(dateCellStyle);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (isNumeric(data)) {
                cell.setCellValue(Double.parseDouble(data));
            } else {
                cell.setCellValue(data);
            }
        }
    }


    private static boolean isDate(String data, SimpleDateFormat dateFormat) {
        try {
            dateFormat.parse(data);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static void insertHeaderRowInSheet(final Sheet sheet,
                                              final CellStyle headerCellStyle,
                                              final XSSFFont titleFont,
                                              final List<String> titles) {
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
        sheet.setColumnWidth(0, 10000);
        sheet.setColumnWidth(1, 10000);
        return sheet;
    }

    public static byte[] createByteArrayFromWorkbook(final Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    public static String cleanInputForExcel(String input) {
        if (input == null) return null;
        return input.replaceAll("[\"',\n\r\t]", "");
    }

    public static String extractLastIdentifierSegment(final String identifier) {
        if (identifier == null) return null;
        String[] parts = identifier.split("\\.");
        if (parts.length < 2) {
            return identifier;
        }
        String lastTwoSegments = parts[parts.length - 2] + "." + parts[parts.length - 1];
        return lastTwoSegments;
    }
}

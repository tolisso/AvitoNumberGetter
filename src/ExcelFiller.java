import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExcelFiller {


    public static void main (String[] args) throws IOException, InterruptedException {

        FileInputStream fis = new FileInputStream("table.xlsx");
        Workbook wb = new XSSFWorkbook(fis);
        fis.close();
        Sheet sheet = wb.getSheetAt(0);
        try {
            boolean changed = false;
            for (int i = 1; i < 2498; i++) {
                System.out.println(i);
                var row = sheet.getRow(i);
//        row.getCell(37).getStringCellValue(); - file
                if (row.getCell(46) == null || row.getCell(46).getStringCellValue().equals("ERROR")) {
                    changed = true;
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    String dateString = date.format(formatter);

                    String id = row.getCell(0).getCellType() == CellType.STRING ?
                            row.getCell(0).getStringCellValue() :
                            ((Integer) (int) row.getCell(0).getNumericCellValue()).toString();
                    if (id.charAt(0) == '\'') {
                        id = id.substring(1);
                    }

                    String file = "screens/tolisso-screens/" + id + "_" + dateString + ".png";
                    String link = row.getCell(1).getStringCellValue();
                    try {
                        Main.main("-screenshot", link, file, "png");
                        row.createCell(46).setCellValue("+");
                        row.createCell(47).setCellValue(file);
                    } catch (ClosedHousingException exc) {
                        System.err.println(exc);
                        row.createCell(46).setCellValue("closed");
                    } catch (WrongLinkException exc) {
                        System.err.println(exc);
                        row.createCell(46).setCellValue("wrong link");
                    } catch (Exception exc) {
                        System.err.println(exc);
                        row.createCell(46).setCellValue("ERROR");
                        row.getCell(1);
                    }

                }
                if (changed && (i % 20 == 0 || i == 2497)) {
                    changed = false;
                    try (FileOutputStream outputStream = new FileOutputStream("table.xlsx")) {
                        wb.write(outputStream);
                    }
                }
            }
        } finally {
            wb.close();
        }
    }
}

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExcelFiller {

    private String fileName;
    private String fileExtension;
    private String site;
    private int sheetNumber;
    private int idCol;
    private int siteCol;
    private int linkCol;
    private int resultCol;
    private int pathCol;
    private int from;
    private int to;

    public ExcelFiller(String fileName, String fileExtension, String site, int sheetNumber, int idCol, int siteCol, int linkCol, int resultCol, int pathCol, int from, int to) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.site = site;
        this.sheetNumber = sheetNumber;
        this.idCol = idCol;
        this.siteCol = siteCol;
        this.linkCol = linkCol;
        this.resultCol = resultCol;
        this.pathCol = pathCol;
        this.from = from;
        this.to = to;
    }

    public static void main (String[] args) throws IOException {
        ExcelFiller ef = new ExcelFiller("Рынок ЗУ 2020", "xlsx", "www.avito", 0, 0, 39,1, 46, 47, 1, 4054);
//        ExcelFiller ef = new ExcelFiller("Рынок ЗУ 2020", "xlsx", "www.avito", 0, 0, 39,1, 46, 47, 1, 1062);
        ef.fill();
    }

    public void fill() throws IOException {
        InputStream is = new FileInputStream(fileName + "." + fileExtension);
        try (Workbook wb = new XSSFWorkbook(is)) {
            is.close();
            Sheet sheet = wb.getSheetAt(sheetNumber);


            LocalDateTime date;
            int changedCellsNumber = 1;
            try {
                boolean changed = false;
                for (int i = from; i < to; i++) {
                    System.out.println(i);
                    var row = sheet.getRow(i);
                    if (row.getCell(siteCol).getStringCellValue().equals(site) &&
                            (
                                    row.getCell(resultCol) == null ||
                                    row.getCell(resultCol).getStringCellValue().equals("ERROR") ||
                                    row.getCell(resultCol).getStringCellValue().equals("closed")
                            )) {
                        changedCellsNumber++;
                        changed = true;
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        date = LocalDateTime.now();
                        String dateString = date.format(dateFormatter);

                        String id = row.getCell(idCol).getCellType() == CellType.STRING ?
                                row.getCell(idCol).getStringCellValue() :
                                ((Integer) (int) row.getCell(idCol).getNumericCellValue()).toString();
                        if (id.charAt(0) == '\'') {
                            id = id.substring(1);
                        }

                        String file = "screens/tolisso-screens/" + id + "-" + site + "-" + fileName.replace(' ', '_') +
                                "-" + dateString + ".jpg";
                        String link = row.getCell(linkCol).getStringCellValue();
                        try {
                            Main.main("-screenshot", link, file);
                            row.createCell(resultCol).setCellValue("+");
                            row.createCell(pathCol).setCellValue(file);
                        } catch (ClosedHousingException exc) {
                            System.err.println(exc);
                            row.createCell(resultCol).setCellValue("closed");
                        } catch (WrongLinkException exc) {
                            System.err.println(exc);
                            row.createCell(resultCol).setCellValue("wrong link");
                        } catch (Exception exc) {
                            System.err.println(exc);
                            row.createCell(resultCol).setCellValue("ERROR");
                        }

                    }
                    if (changed && (changedCellsNumber % 100 == 0)) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss");
                        date = LocalDateTime.now();
                        String dateTimeString = date.format(dateTimeFormatter);
                        try (FileOutputStream outputStream = new FileOutputStream(fileName + "_save_" + dateTimeString + "." + fileExtension)) {
                            wb.write(outputStream);
                        }
//                    wb.close();
//                    fis = new FileInputStream(fileName + "." + fileExtension);
//                    wb = new XSSFWorkbook(fis);
//                    fis.close();
//                    sheet = wb.getSheetAt(sheetNumber);
                    }
                    if (changed && (changedCellsNumber % 10 == 0 || i == to - 1)) {
                        changed = false;
                        try (FileOutputStream outputStream = new FileOutputStream(fileName + "." + fileExtension)) {
                            wb.write(outputStream);
                        }
                    }

                }
//            WhitePagesRemover rm = new WhitePagesRemover(wb, fileName + "." + fileExtension, sheetNumber, resultCol, pathCol, from, to);
//
//            rm.remove();
            } finally {
                try (FileOutputStream outputStream = new FileOutputStream(fileName + "." + fileExtension)) {
                    wb.write(outputStream);
                }
            }
        }
    }
}

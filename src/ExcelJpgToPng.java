import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExcelJpgToPng {
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

    public ExcelJpgToPng(String fileName, String fileExtension, String site, int sheetNumber, int idCol, int siteCol, int linkCol, int resultCol, int pathCol, int from, int to) {
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

    public static void main(String[] args) throws IOException, InvalidFormatException {
        ExcelJpgToPng ef = new ExcelJpgToPng("Рынок Квартиры 2020", "xlsx", "www.avito", 0, 0, 39,1, 46, 47, 1, 5054);
        ef.fill();
    }

    public void fill() throws IOException, InvalidFormatException {
        InputStream is = new FileInputStream(fileName + "." + fileExtension);
        Workbook wb = new XSSFWorkbook(is);
        is.close();
        Sheet sheet = wb.getSheetAt(sheetNumber);


        int changedCellsNumber = 1;
        try {
            for (int i = from; i < to; i++) {
                System.out.println(i);
                var row = sheet.getRow(i);
                if (row.getCell(pathCol) != null) {
                    String fullName = row.getCell(pathCol).getStringCellValue();
                    if (fullName.substring(fullName.length() - 4).equals(".png")) {
                        row.getCell(pathCol).setCellValue(fullName.substring(0, fullName.length() - 4) + ".jpg");
                        changedCellsNumber++;
                    }
                }
//                if (changedCellsNumber % 100 == 0 || i == to - 1) {
//                    try (FileOutputStream outputStream = new FileOutputStream(fileName + "." + fileExtension)) {
//                        wb.write(outputStream);
//                    }
//                }

            }
        } finally {
            try (FileOutputStream outputStream = new FileOutputStream(fileName + "." + fileExtension)) {
                wb.write(outputStream);
            } finally {
                wb.close();
            }
        }
    }
}

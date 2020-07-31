import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WhitePagesRemover {
    private Workbook wb;
    private int sheetNumber;
    private int resultCol;
    private int pathCol;
    private int from;
    private int to;

    public WhitePagesRemover(Workbook wb, int sheetNumber, int resultCol, int pathCol, int from, int to) {
        this.wb = wb;
        this.sheetNumber = sheetNumber;
        this.resultCol = resultCol;
        this.pathCol = pathCol;
        this.from = from;
        this.to = to;
    }

    public void remove() {
        Sheet sheet = wb.getSheetAt(sheetNumber);
        for (int i = from; i < to; i++) {
            Row row = sheet.getRow(i);
            Cell pathCell = row.getCell(pathCol);
            if (pathCell == null) {
                continue;
            }
            String path = pathCell.getStringCellValue();
            try {
                File imgFile = new File(path);
                Image img = ImageIO.read(imgFile);
                int width = img.getWidth(null);
                int height = img.getHeight(null);
            } catch (IOException exc) {

            }
        }
    }
}

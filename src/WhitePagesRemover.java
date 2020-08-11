import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.*;


public class WhitePagesRemover {
    private Workbook wb;
    private String filename;
    private int sheetNumber;
    private int resultCol;
    private int pathCol;
    private int from;
    private int to;

    public WhitePagesRemover(Workbook wb, String filename, int sheetNumber, int resultCol, int pathCol, int from, int to) {
        this.wb = wb;
        this.filename = filename;
        this.sheetNumber = sheetNumber;
        this.resultCol = resultCol;
        this.pathCol = pathCol;
        this.from = from;
        this.to = to;
    }

    public void remove() throws IOException {
        Sheet sheet = wb.getSheetAt(sheetNumber);
        int changed = 1;
        for (int i = from; i < to; i++) {
            System.out.println(i);
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
                int[] pixels = new int[width * height];
                PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
                pg.grabPixels();
                boolean isWhite = true;
                for (int pixel : pixels) {
                    Color color = new Color(pixel);
                    if (color.getAlpha() == 0 || color.getRGB() != Color.WHITE.getRGB()) {
                        isWhite = false;
                        break;
                    }
                }
                if (isWhite) {
                    changed++;
                    System.out.println(i + " removing");
                    row.removeCell(pathCell);
                    row.createCell(resultCol).setCellValue("ERROR");
                    imgFile.delete();
                }
            } catch (IOException | InterruptedException exc) {
                changed++;
                row.removeCell(pathCell);
                row.createCell(resultCol).setCellValue("ERROR");
                System.err.println("Problems with row " + i + ":\n" + exc);
            }
            if (changed % 10 == 0) {
                try (FileOutputStream outputStream = new FileOutputStream(filename)) {
                    wb.write(outputStream);
                }
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            wb.write(outputStream);
        }
    }
}

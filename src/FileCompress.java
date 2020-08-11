import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class FileCompress {
    public static void main(String[] args) throws IOException {
        var fileNames = new File("screens/tolisso-screens").list();
        Set<String> png = new TreeSet<>();
        Set<String> jpg = new TreeSet<>();
        for (String fileName : fileNames) {
            if (fileName.length() < 4) {
                continue;
            }
            String name = fileName.substring(0, fileName.length() - 4);
            String extension = fileName.substring(fileName.length() - 4);
            if (extension.equals(".png")) {
                png.add(name);
            }
            if (extension.equals(".jpg")) {
                jpg.add(name);
            }
        }
        int i = 0;
        for (var name : png) {
            i++;
            if (jpg.contains(name)) {
                continue;
            }
            System.out.println(i);
            pngToCompressedJpg("screens/tolisso-screens/" + name, ".png");
        }
    }

    public static void pngToCompressedJpg(String name, String extension) throws IOException {
        if (!extension.equals(".png")) {
            return;
        }
        File png = new File(name + extension);
        BufferedImage tmp = ImageIO.read(png);
        BufferedImage bufferedImage = new BufferedImage(tmp.getWidth(),
                tmp.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        bufferedImage.createGraphics().drawImage(tmp, 0, 0, Color.WHITE, null);

        try(OutputStream os = new FileOutputStream(name + ".jpg")) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                writer.setOutput(ios);
                try {
                    ImageWriteParam param = writer.getDefaultWriteParam();

                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.3f);  // Change the quality value you prefer
                    writer.write(null, new IIOImage(bufferedImage, null, null), param);
                } finally {
                    writer.dispose();
                }
            }
        }
    }
}

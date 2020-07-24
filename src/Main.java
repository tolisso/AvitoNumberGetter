import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Main {

    private static Integer numberOfPages = Integer.MAX_VALUE;
    private static String inputUrl = "https://www.avito.ru/yahroma/doma_dachi_kottedzhi/dom_90_m_na_uchastke_6_sot._1926399383";
    private static final String listNumberPrefix = "item-extended-phone";
    private static final String bigNumberPrefix = "<div class=\"item-phone-big-number";

    private static final int height = 1400;
    private static final int width = 2100;

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length == 1 && args[0].equals("-man")) {
            System.out.println("not implemented");
            return;
        } else if (args.length == 1) {
            inputUrl = args[0];
        } else if (args.length == 2) {
            inputUrl = args[0];
            try {
                numberOfPages = Integer.parseInt(args[1]);
            } catch (NumberFormatException exc) {
                System.err.println("Wrong number format exception (arg2)");
                return;
            }
            if (numberOfPages < 1) {
                System.err.println("Small number exception (arg2)");
                return;
            }
        }
        var headlessOptions = new ChromeOptions();
        headlessOptions.addArguments("--headless");
        headlessOptions.addArguments("window-size=" + height + "," + width);

	    ChromeDriver driver = new ChromeDriver(headlessOptions);
        ChromeDriver imageDriver = new ChromeDriver(headlessOptions);
        Actions action = new Actions(driver);
	    try {
            driver.get(inputUrl);

            var buttonsConst = driver.findElementsByXPath("//*[contains(text(), \'Показать телефон\')]");
            var buttons = new ArrayList<WebElement>();
            for (int i = 0; i < Math.min(buttonsConst.size(), numberOfPages); i++) {
                if (buttonsConst.get(i).isDisplayed()) {
                    buttons.add(buttonsConst.get(i));
                }
            }
            Collections.shuffle(buttons);

            Random random = new Random();
            TimeUnit.MILLISECONDS.sleep(1000 + random.nextInt() % 500);

            for (var button : buttons) {
                try {
                    TimeUnit.MILLISECONDS.sleep(random.nextInt() % 2000 + 8000);
                } catch (Exception exc) {

                }
                action.moveToElement(button).perform();
                TimeUnit.MILLISECONDS.sleep(1000 + random.nextInt() % 500);
                button.click();
            }
            // for product list (with phones!)
            List<String> urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), listNumberPrefix);
            // for product page
            if (urlsOfNumbersImages.isEmpty()) {
                urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), bigNumberPrefix);
            }

            TimeUnit.MILLISECONDS.sleep(1000 + random.nextInt() % 500);
            while (urlsOfNumbersImages.size() != buttons.size()) {
                TimeUnit.MILLISECONDS.sleep(1000 + random.nextInt() % 500);
                urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), listNumberPrefix);
                if (urlsOfNumbersImages.isEmpty()) {
                    urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), bigNumberPrefix);
                }
            }

            for (String strUrl : urlsOfNumbersImages) {
                Image image = null;
                while (driver.getWindowHandles().size() != 1) {
                    TimeUnit.MILLISECONDS.sleep(1000 + random.nextInt() % 500);
                }

                imageDriver.get(strUrl);
                TimeUnit.MILLISECONDS.sleep(5000 + random.nextInt() % 700);
                var number = imageDriver.findElementByXPath("//img");

                WrapsDriver wrapsDriver = (WrapsDriver) number;

                File screenshot = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
                int width = number.getSize().width;
                int height = number.getSize().height;
                Point location = number.getLocation();
                BufferedImage bufferedImage = ImageIO.read(screenshot);
                bufferedImage = bufferedImage.getSubimage(location.x, location.y, width, height);
                File outputfile = new File("../number.png");
                ImageIO.write(bufferedImage, "png", outputfile);

                ProcessBuilder builder = new ProcessBuilder("bash", "-c", "tesseract ./number.png stdout 2>/dev/null\n");
                builder.redirectErrorStream(true);
                Process process = builder.start();
                InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                System.out.println(reader.readLine());
                is.close();
                reader.close();
            }
        } finally {
	        driver.close();
	        imageDriver.close();
        }
    }

    private static List<String> getUrlsOfNumbersImages(String source, String imgClass) {
        ArrayList<String> ans = new ArrayList<>();
        outer : for (int i = 0; i < source.length() - imgClass.length(); i++) {
            for (int j = 0; j < imgClass.length(); j++) {
                if (source.charAt(i + j) != imgClass.charAt(j)) {
                    continue outer;
                }
            }
            int start = i + imgClass.length();
            final String urlPref = "src=\"";
            while (!checkSubstr(source, urlPref, start)) {
                start++;
            }
            start += urlPref.length();
            int end = start;
            while (source.charAt(end) != '\"') {
                end++;
            }
            ans.add(source.substring(start, end));
        }
        return ans;
    }

    private static boolean checkSubstr(String source, String substr, int from) {
        for (int i = 0; i < substr.length(); i++) {
            if (source.charAt(i + from) != substr.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}



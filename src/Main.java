import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Main {

    static final Integer numberOfPages = 3;

    public static void main(String[] args) throws IOException, InterruptedException {


	    ChromeDriver driver = new ChromeDriver();
        ChromeDriver imageDriver = new ChromeDriver();
        Actions action = new Actions(driver);
	    try {
            driver.get("https://www.avito.ru/rossiya/doma_dachi_kottedzhi/sdam-ASgBAgICAUSUA9IQ");

//        System.out.println(getNumberOfPages(driver));
            var buttonsConst = driver.findElementsByXPath("//*[contains(text(), \'Показать телефон\')]");
            var buttons = new ArrayList<WebElement>();
            for (int i = 0; i < Math.min(buttonsConst.size(), numberOfPages); i++) {
                buttons.add(buttonsConst.get(i));
            }
            Collections.shuffle(buttons);
            Random random = new Random();
            for (var button : buttons) {
                try {
                    TimeUnit.MILLISECONDS.sleep(random.nextInt() % 2000 + 8000);
                } catch (Exception exc) {

                }
                action.moveToElement(button);
                button.click();
            }

//            List<WebElement> numbers;

//            while(true) {
//                try {
//                    TimeUnit.SECONDS.sleep(2);
//                    numbers = driver.findElementsByXPath("//*[@class='item-extended-phone']");
//                    driver.findElementsByClassName("item-extended-phone");
//                    if (numbers.size() == buttons.size()) {
//                        break;
//                    }
//                } catch (Exception exc) {
//                    System.err.println("Wait");
//                }
//            }
//            System.out.println(countSubstr(driver.getPageSource(), "item-extended-phone"));

//            for (var number : numbers) {
//        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", number);
//                action.moveToElement(number);
//                TimeUnit.MILLISECONDS.sleep(1000 + random.nextInt() % 500);
//                JavascriptExecutor executor = (JavascriptExecutor) driver;
//                int xOffset = (int) (long) executor.executeScript("return window.pageXOffset;");
//                int yOffset = (int) (long) executor.executeScript("return window.pageYOffset;");
//
//                WrapsDriver wrapsDriver = (WrapsDriver) number;
//
//                File screenshot = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
//                int width = number.getSize().width;
//                int height = number.getSize().height;
//                Point location = number.getLocation();
//                BufferedImage bufferedImage = ImageIO.read(screenshot);
//                System.err.println(location.x - xOffset);
//                System.err.println(location.y - yOffset);
//                bufferedImage = bufferedImage.getSubimage(location.x - xOffset, location.y - yOffset, width, height);
//                File outputfile = new File("number.png");

            var urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), "item-extended-phone");
            System.out.println(urlsOfNumbersImages);

            for (String strUrl : urlsOfNumbersImages) {
                Image image = null;
                while (driver.getWindowHandles().size() != 1) {
                    TimeUnit.MILLISECONDS.sleep(1000 + random.nextInt() % 500);
                }

                imageDriver.get(strUrl);
                TimeUnit.MILLISECONDS.sleep(500 + random.nextInt() % 700);
                var number = imageDriver.findElementByXPath("//img");

//                int xOffset = (int) (long) executor.executeScript("return window.pageXOffset;");
//                int yOffset = (int) (long) executor.executeScript("return window.pageYOffset;");

                WrapsDriver wrapsDriver = (WrapsDriver) number;

                File screenshot = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
                int width = number.getSize().width;
                int height = number.getSize().height;
                Point location = number.getLocation();
                BufferedImage bufferedImage = ImageIO.read(screenshot);
                System.err.println(location.x);
                System.err.println(location.y);
                bufferedImage = bufferedImage.getSubimage(location.x, location.y, width, height);
                File outputfile = new File("number.png");
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
    private static void changeTab(WebDriver wd, int tab) {
        wd.switchTo().window(wd.getWindowHandles().toArray(new String[0])[tab]);
    }
}


//data:image/png;base64,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]
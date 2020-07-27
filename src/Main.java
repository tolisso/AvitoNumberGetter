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
import java.util.ArrayList.*;


public class Main {

    private static Integer numberOfHousings = 2; //Integer.MAX_VALUE;
    private static Integer numberOfPages = 1;
    private static String inputUrl;
    private static final String listNumberPrefix = "item-extended-phone";
    private static final String bigNumberPrefix = "<div class=\"item-phone-big-number";
    private static final List<String> firstArg = List.of("-link", "-page", "-pages");

    private static final int height = 1400;
    private static final int width = 2100;

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length < 2) {
            System.err.println("too few arguments");
        } else {
            if (!firstArg.contains(args[0])) {
                System.out.println("Wrong argument: " + args[0]);
                return;
            }
            inputUrl = args[1];
            if (args.length == 3) {
                try {
                    if (args[0].equals("-page")) {
                        numberOfHousings = Integer.parseInt(args[2]);
                    } else if (args[0].equals("-pages")) {
                        numberOfPages = Integer.parseInt(args[2]);
                    } else {
                        System.err.println("Wrong argument: " + args[0]);
                        return;
                    }
                } catch (NumberFormatException exc) {
                    System.err.println("Wrong number format exception (arg2)");
                    return;
                }
                if (numberOfHousings < 1 && numberOfPages < 1) {
                    System.err.println("Small number exception (arg2)");
                    return;
                }
            }
        }
        var headlessOptions = new ChromeOptions();
        headlessOptions.addArguments("--headless");
        headlessOptions.addArguments("window-size=" + height + "," + width);

//	    ChromeDriver driver = new ChromeDriver(headlessOptions);
        ChromeDriver driver = new ChromeDriver();
        ChromeDriver imageDriver = new ChromeDriver(headlessOptions);
        Actions action = new Actions(driver);
        try {
            for (int page = 1; page <= numberOfPages; page++) {
                if (numberOfPages != 1) {
                    driver.get(inputUrl + "?p=" + page);
                } else {
                    driver.get(inputUrl);
                }
                parsePage(driver, imageDriver, action);
            }
        } finally {
            driver.close();
            imageDriver.close();
        }
    }

    private static void parsePage(ChromeDriver driver, ChromeDriver imageDriver, Actions action) throws InterruptedException, IOException {
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

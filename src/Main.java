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

    private static final boolean DEBUG = false;
    private static final int DELAY = 3_500; // ms

    private static Integer numberOfHousings = Integer.MAX_VALUE; //Integer.MAX_VALUE;
    private static Integer numberOfPages = 1; // - default value: DO NOT CHANGE
    private static String inputUrl;
    private static String outputFileName;
    private static String outputExtension;

    private static final String listNumberPrefix = "item-extended-phone";
    private static final String bigNumberXpath = "//div[@class='item-phone-big-number js-item-phone-big-number']";
    private static final String exitFromBigNumberXpath = "//*[@class=\"b-popup item-popup\"]/span";
    private static final List<String> firstPhonesArg = List.of("-link", "-page", "-pages");

    private static final int height = 3500;
    private static final int width = 1400;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            System.err.println("too few arguments");
        } else if (args[0].equals("-phones")) {
            if (args.length < 3) {
                System.err.println("too few arguments");
            } else {
                if (!firstPhonesArg.contains(args[1])) {
                    System.err.println("Wrong argument: " + args[1]);
                    return;
                }
                inputUrl = args[2];
                if (args.length == 4) {
                    try {
                        if (args[1].equals("-page")) {
                            numberOfHousings = Integer.parseInt(args[3]);
                        } else if (args[1].equals("-pages")) {
                            numberOfPages = Integer.parseInt(args[3]);
                        } else {
                            System.err.println("Wrong argument: " + args[1]);
                            return;
                        }
                    } catch (NumberFormatException exc) {
                        System.err.println("Wrong number format exception (arg3)");
                        return;
                    }
                    if (numberOfHousings < 1 && numberOfPages < 1) {
                        System.err.println("Small number exception (arg3)");
                        return;
                    }
                }
            }
        } else if (args[0].equals("-screenshot")) {
            if (args.length != 4) {
                System.err.println("Wrong number of arguments with -screenshot");
                return;
            }
            inputUrl = args[1];
            outputFileName = args[2];
            outputExtension = args[3];
        } else {
            System.err.println("Wrong number format exception (arg1)");
            return;
        }

        System.setProperty("webdriver.chrome.silentOutput", "true");

        var headlessOptions = new ChromeOptions();
        headlessOptions.addArguments("--headless");
        headlessOptions.addArguments("window-size=" + width + "," + height);
        ChromeDriver driver;
        ChromeDriver imageDriver = null;
        if (DEBUG) {
            driver = new ChromeDriver();
        } else {
            driver = new ChromeDriver(headlessOptions);
        }

        if (args[0].equals("-phones")) {
            if (DEBUG) {
                imageDriver = new ChromeDriver();
            } else {
                imageDriver = new ChromeDriver(headlessOptions);
            }
        }
        Actions action = new Actions(driver);
        try {
            for (int page = 1; page <= numberOfPages; page++) {
                if (numberOfPages != 1) {
                    driver.get(inputUrl + "?p=" + page);
                } else {
                    driver.get(inputUrl);
                }
                if (args[0].equals("-phones")) {
                    parseNumbers(driver, imageDriver, action);
                } else if (args[0].equals("-screenshot")) {
                    takeScreenshot(driver, action);
                }
            }
        } finally {
            if (!DEBUG) {
                driver.close();
                if (args[0].equals("-phones")) {
                    imageDriver.close();
                }
            }
        }
    }

    private static void takeScreenshot(ChromeDriver driver, Actions action) throws IOException, InterruptedException {
        var buttons = pressPhoneButtons(driver, action, "//*[contains(text(), \'Показать телефон\')]");
        // wait until phones was loaded

        for (int i = 0; i < 4; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(getDelay());
                driver.findElementByXPath(exitFromBigNumberXpath).click();
                TimeUnit.MILLISECONDS.sleep(getDelay());
                break;
            } catch (Exception exc) {

            }
        }

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage bufferedImage = ImageIO.read(screenshot);
        File outputFile = new File(outputFileName);
        ImageIO.write(bufferedImage, outputExtension, outputFile);
    }

    private static List<String> parseButtonsXpath(String html, String pattern) throws IOException {
        File file = File.createTempFile("scammed", ".html");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(html);
            writer.close();
            return getPhoneButtonsXpathFromLocalSite(file, pattern);
        } finally {
            file.deleteOnExit();
        }
    }

    private static List<String> getPhoneButtonsXpathFromLocalSite(File file, String pattern) throws IOException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver localDriver;
        if (DEBUG) {
            localDriver = new ChromeDriver();
        } else {
            localDriver = new ChromeDriver(options);
        }
        try {
            localDriver.get("file://" + file.getAbsolutePath());
            JavascriptExecutor js = (JavascriptExecutor) localDriver;

            List<String> xpathList = new ArrayList<>();
            for (int i = 0;; i++) {
                var jsStream = Main.class.getResourceAsStream("/script.js");
                var sc = new Scanner(jsStream).useDelimiter("\\A");

                String jsScript = sc.next();
                sc.close();
                jsStream.close();

                Object xpath = js.executeScript("var text = \"" + pattern + "\"; " + jsScript);

                if (xpath == null) {
                    break;
                }
                if (DEBUG) {
                    System.err.println(xpath);
                }
                xpathList.add((String)xpath);
            }
            return xpathList;
        } finally {
            if (!DEBUG) {
                localDriver.close();
            }
        }
    }
    private static List<WebElement> pressPhoneButtons(ChromeDriver driver, Actions action, String pattern) throws InterruptedException, IOException {
        var xpathList = parseButtonsXpath(driver.getPageSource(), pattern);

        var buttonsConst = new ArrayList<WebElement>();
        for (var xpath : xpathList) {
            buttonsConst.add(driver.findElementByXPath(xpath));
        }
        var buttons = new ArrayList<WebElement>();
        for (int i = 0; i < buttonsConst.size(); i++) {
            buttons.add(buttonsConst.get(i));
        }
        while (buttons.size() > numberOfHousings) {
            buttons.remove(buttons.size() - 1);
        }
        Collections.shuffle(buttons);

        TimeUnit.MILLISECONDS.sleep(getDelay());

        for (var button : buttons) {
            try {
                TimeUnit.MILLISECONDS.sleep(getDelay());
            } catch (Exception exc) {

            }
            try {
                action.moveToElement(button).perform();
                TimeUnit.MILLISECONDS.sleep(getDelay());
                button.click();
            } catch (Exception exc) {
                System.out.println("button not clicked");
            }
            // popup may be invoked
            var popUpsExits = driver.findElementsByXPath("//div[@class=\"close js-item-popup-close\"]");
            for (var exit : popUpsExits) {
                try {
                    TimeUnit.MILLISECONDS.sleep(getDelay() / 3);
                    exit.click();
                } catch (Exception exc) {

                }
            }
        }
        return buttons;
    }
    private static void parseNumbers(ChromeDriver driver, ChromeDriver imageDriver, Actions action) throws InterruptedException, IOException {
        var buttons = pressPhoneButtons(driver, action, "//button[contains(text(), \'Показать телефон\')]");

        var urlsOfNumbersImages = getAllUrlsOfNumbersImages(driver, buttons.size());


        for (String strUrl : urlsOfNumbersImages) {
            while (driver.getWindowHandles().size() != 1) {
                TimeUnit.MILLISECONDS.sleep(getDelay());
            }

            imageDriver.get(strUrl);
            TimeUnit.MILLISECONDS.sleep(getDelay());
            var number = imageDriver.findElementByXPath("//img");

            WrapsDriver wrapsDriver = (WrapsDriver) number;

            File screenshot = ((TakesScreenshot) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
            int width = number.getSize().width;
            int height = number.getSize().height;
            Point location = number.getLocation();
            BufferedImage bufferedImage = ImageIO.read(screenshot);
            bufferedImage = bufferedImage.getSubimage(location.x, location.y, width, height);

            File outputfile = new File("number.png");
            try {
                ImageIO.write(bufferedImage, "png", outputfile);
                ProcessBuilder colorBinarization = new ProcessBuilder("bash", "-c", "convert number.png -threshold 0 number.png\n");
                colorBinarization.start();
                ProcessBuilder builder = new ProcessBuilder("bash", "-c", "tesseract ./number.png stdout 2>/dev/null\n");
                builder.redirectErrorStream(true);
                Process process = builder.start();
                InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                System.out.println(reader.readLine());
                is.close();
                reader.close();
            } finally {
                outputfile.deleteOnExit();
            }
        }
    }

    private static List<String> getAllUrlsOfNumbersImages(ChromeDriver driver, int size) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(getDelay());
        // for product list (with phones!)
        List<String> urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), listNumberPrefix);
        // for product page
        if (urlsOfNumbersImages.isEmpty()) {
            urlsOfNumbersImages = getBigPhoneUrls(driver);
        }
        System.out.println(urlsOfNumbersImages.size() + " " + size);
        return urlsOfNumbersImages;
    }

    private static List<String> getBigPhoneUrls(ChromeDriver driver) {
        var elements = driver.findElementsByXPath(bigNumberXpath);
        System.out.println(elements);
        for (var element : elements) {
            System.out.println(element.isDisplayed());
            if (element.isDisplayed()) {
                return getUrlsOfNumbersImages(element.getAttribute("innerHTML"), "");
            }
        }
        return new ArrayList<>();
    }

    private static List<String> getUrlsOfNumbersImages(String source, String imgClass) {
        ArrayList<String> ans = new ArrayList<>();
        outer : for (int i = 0; i < source.length() - imgClass.length(); i++) {
            for (int j = 0; j < imgClass.length(); j++) {
                if (source.charAt(i + j) != imgClass.charAt(j)) {
                    continue outer;
                }
            }
            int openParenthesis = 0;
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

    private static int getDelay() {
        Random rand = new Random();
        return DELAY + Math.abs(rand.nextInt() % DELAY);
    }
}

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
    private static final int DELAY = 7_000; // ms

    private static Integer numberOfHousings = Integer.MAX_VALUE; //Integer.MAX_VALUE;
    private static Integer numberOfPages = 1; // - default value: DO NOT CHANGE
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
                System.err.println("Wrong argument: " + args[0]);
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

//        File file = new File("scammed1.html");
//        workWithLocalVersion(file);

        System.setProperty("webdriver.chrome.silentOutput", "true");

        var headlessOptions = new ChromeOptions();
        headlessOptions.addArguments("--headless");
        headlessOptions.addArguments("window-size=" + height + "," + width);
        ChromeDriver driver;
        ChromeDriver imageDriver;
        if (DEBUG) {
            driver = new ChromeDriver();
            imageDriver = new ChromeDriver();
        } else {
            driver = new ChromeDriver(headlessOptions);
            imageDriver = new ChromeDriver(headlessOptions);
        }
        Actions action = new Actions(driver);
        try {
            for (int page = 1; page <= numberOfPages; page++) {
                if (numberOfPages != 1) {
                    driver.get(inputUrl + "?p=" + page);
                } else {
                    driver.get(inputUrl);
                }
                var xpathList = parseButtonsXpath(driver.getPageSource());
//                parseHtml(driver.getPageSource());
                parsePage(driver, imageDriver, action, xpathList);
            }
        } finally {
            if (!DEBUG) {
                driver.close();
                imageDriver.close();
            }
        }
    }

    private static List<String> parseButtonsXpath(String html) throws IOException {
//        File file = File.createTempFile("scam", ".html");
        File file = File.createTempFile("scammed", ".html");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(html);
            writer.close();
            return workWithLocalVersion(file);
        } finally {
            file.deleteOnExit();
        }
//        try {
//            System.out.println(html.substring(0, 100000));
//            Match $html = (Match) JOOX.builder().parse(html);
//            System.out.println($html.xpath("//*[contains(text(), \'Показать телефон\')]").each());
//        } catch (Exception exc) {
//            System.err.println(exc);
//        }
    }

    private static List<String> workWithLocalVersion(File file) throws IOException {
//        System.setProperty("webdriver.firefox.driver", "/home/tolisso/Programming/drivers/geckodriver");
//
//        FirefoxOptions options = new FirefoxOptions();
//        options.addPreference("javascript.enabled", false);
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

                Object xpath = js.executeScript(jsScript);

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

    private static void parsePage(ChromeDriver driver, ChromeDriver imageDriver, Actions action, List<String> xpathList) throws InterruptedException, IOException {
        var buttonsConst = new ArrayList<WebElement>();
        for (var xpath : xpathList) {
            buttonsConst.add(driver.findElementByXPath(xpath));
        }
        var buttons = new ArrayList<WebElement>();
        for (int i = 0; i < buttonsConst.size(); i++) {
            if (buttonsConst.get(i).isEnabled()) {
                buttons.add(buttonsConst.get(i));
            }
        }
        while (buttons.size() > numberOfHousings) {
            buttons.remove(buttons.size() - 1);
        }
        Collections.shuffle(buttons);

        Random random = new Random();
        TimeUnit.MILLISECONDS.sleep(getDelay());

        for (var button : buttons) {
            try {
                TimeUnit.MILLISECONDS.sleep(getDelay());
            } catch (Exception exc) {

            }
            action.moveToElement(button).perform();
            TimeUnit.MILLISECONDS.sleep(getDelay());
            button.click();
        }
        // for product list (with phones!)
        List<String> urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), listNumberPrefix);
        // for product page
        if (urlsOfNumbersImages.isEmpty()) {
            urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), bigNumberPrefix);
        }

        TimeUnit.MILLISECONDS.sleep(getDelay());
        while (urlsOfNumbersImages.size() != buttons.size()) {
            TimeUnit.MILLISECONDS.sleep(getDelay());
            urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), listNumberPrefix);
            if (urlsOfNumbersImages.isEmpty()) {
                urlsOfNumbersImages = getUrlsOfNumbersImages(driver.getPageSource(), bigNumberPrefix);
            }
        }

        for (String strUrl : urlsOfNumbersImages) {
            Image image = null;
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

    private static int getDelay() {
        Random rand = new Random();
        return DELAY + Math.abs(rand.nextInt() % DELAY);
    }
}

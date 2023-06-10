package retool;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class LaunchBrowser {

    public String url = "https://retool.com/api-generator/";
    public static WebDriver driver;

    @BeforeClass
    public void setup() {
        System.setProperty(
                "webdriver.chrome.driver", "./src/Driver/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.get(url);
        driver.manage().window().maximize();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }
}

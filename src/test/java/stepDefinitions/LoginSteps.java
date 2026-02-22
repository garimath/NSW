package stepDefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class LoginSteps {

    Properties properties = new Properties();
    WebDriver driver;

    // Load config once
    public LoginSteps() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            if(input == null) {
                throw new RuntimeException("config.properties not found! Check resources folder and classpath.");
            }
            properties.load(input);
            System.out.println("Properties loaded: " + properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before("@login")
    public void setUp() throws IOException {
        // Load configuration
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
        props.load(fis);

        String browser = props.getProperty("browser").toLowerCase();

        if (browser.equals("chrome")) {
            ChromeOptions options = new ChromeOptions();

            if (System.getenv("CI") != null) {
                options.addArguments("--headless=new");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--disable-gpu");
            } else {
                options.addArguments("--start-maximized");
            }

            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver(options);

        } else if (browser.equals("edge")) {
            EdgeOptions options = new EdgeOptions();
            if (System.getenv("CI") != null) {
                options.addArguments("headless=new", "--no-sandbox", "--disable-gpu");
            } else {
                options.addArguments("--start-maximized");
            }
            WebDriverManager.edgedriver().setup(); // <-- automatically downloads correct Edge driver
            driver = new EdgeDriver(options);
        
        } else {
            throw new IllegalArgumentException("Browser not supported: " + browser);
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Given("user opens the NSW Stamp Duty page")
    public void openPage() {
        //loadConfig();   // load config file
        String url = properties.getProperty("baseUrl");
        driver.get(url);
        System.out.println("website launched "+url);
    }

    @Then("the page should load successfully")
    public void verifyPageLoaded() {
        String title = driver.getTitle();
        Assert.assertTrue("Page did not load correctly",
                title.contains("Check motor vehicle stamp duty"));
        System.out.println("Check motor vehicle stamp duty page loaded successfully");
    }

    @When("user clicks on Check online button")
    public void clickCheckOnline() {
        driver.findElement(By.linkText("Check online")).click();
        System.out.println("Check online button clicked.");
    }

    @Then("Revenue NSW calculators page should appear")
    public void verifyCalculatorPage() {
        WebElement heading = driver.findElement(By.tagName("h1"));
        Assert.assertTrue(heading.getText().contains("Revenue NSW calculators"));
        System.out.println("Revenue NSW calculators page loaded successfully");
    }

    @When("user selects Yes option")
    public void selectYesOption() {
        String option = properties.getProperty("vehicleOption");
        if(option.equalsIgnoreCase("Yes")) {
            driver.findElement(By.xpath("//label[contains(text(),'Yes')]")).click();
        } else {
            driver.findElement(By.xpath("//label[contains(text(),'No')]")).click();
        }
        System.out.println("Registration for passenger vehicle selected as "+option);
    }

    @When("user enters purchase price")
    public void enterPurchasePrice() {
        String amount = properties.getProperty("purchaseAmount");
        WebElement priceField = driver.findElement(By.id("purchasePrice"));
        priceField.sendKeys(amount);
        System.out.println("Purchase Price entered as "+amount);
    }

    @When("user clicks on Calculate button")
    public void clickCalculate() {
        driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
        System.out.println("Calculate button is clicked");
    }

    @Then("calculation result should be displayed correctly")
    public void verifyResults() {

        //wait for the next window to appear
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));
        System.out.println("Motor vehicle registration window is visible: " + modal.isDisplayed());
       /* try {
            Thread.sleep(5000);  // wait for 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //compare the vehicle answer is similar to what was entered in previous page
        String passengerVehicleAnswer = driver.findElement(By.xpath("//td[text()='Is this registration for a passenger vehicle?']/following-sibling::td")).getText().trim();
        Assert.assertEquals(properties.getProperty("vehicleOption"), passengerVehicleAnswer);
        System.out.println("Passenger vehicle shown as "+passengerVehicleAnswer);

        //compare the purchase value is similar to what was entered in previous page
        String purchaseValue = driver.findElement(By.xpath("//td[text()='Purchase price or value']/following-sibling::td")).getText().replace("$", "").replace(",", "").replace(".00", "");
        Assert.assertEquals(properties.getProperty("purchaseAmount"), purchaseValue);
        System.out.println("Purchase amount shown as "+purchaseValue);

        //validate that duty payable is visible
        String dutyPayable = driver.findElements(By.cssSelector("td.focus")).get(7).getText();

        if(dutyPayable != null){
            System.out.println("Duty payable text is visible "+dutyPayable);
        } else{
            System.out.println("Duty payable text is not visible");
            assert(false);
        }
        driver.quit();
    }
}

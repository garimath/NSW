package stepDefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    public void setUp() {
        // Configure ChromeOptions for headless mode (required in CI)
        ChromeOptions options = new ChromeOptions();
        // If running in CI → enable headless
        if (System.getenv("CI") != null)
        { options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }
        else { options.addArguments("--start-maximized"); }
        // Initialize WebDriver with options
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        // Implicit wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

    }

    // Generic click helper (safe for headless and overlays)
    public void clickElement(By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        Actions actions = new Actions(driver);
        actions.moveToElement(element).click().perform();
    }

    // Pass By locator, not WebElement
    public void typeText(By locator, String text) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
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
            //driver.findElement(By.xpath("//label[contains(text(),'Yes')]")).click();
            clickElement(By.xpath("//label[contains(text(),'Yes')]"));
        } else {
            //driver.findElement(By.xpath("//label[contains(text(),'No')]")).click();
            clickElement(By.xpath("//label[contains(text(),'No')]"));
        }
        System.out.println("Registration for passenger vehicle selected as "+option);
    }

    @When("user enters purchase price")
    public void enterPurchasePrice() {
        String amount = properties.getProperty("purchaseAmount");
        //WebElement priceField = driver.findElement(By.id("purchasePrice"));
        //priceField.sendKeys(amount);
        typeText(By.id("purchasePrice"), amount);
        System.out.println("Purchase Price entered as "+amount);
    }

    @When("user clicks on Calculate button")
    public void clickCalculate() {
        driver.findElement(By.cssSelector("button.btn.btn-primary")).click();
        System.out.println("Calculate button is clicked");
    }

    @Then("calculation result should be displayed correctly")
    public void verifyResults() {
        if(System.getenv("CI") != null) {
            System.out.println("Running in CI → skipping modal");
            try {
                Thread.sleep(5000); // wait 5 seconds for modal to appear
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));
            wait.until(ExpectedConditions.visibilityOf(modal));
            System.out.println("Motor vehicle registration window is visible: " + modal.isDisplayed());
        }

        /*//compare the vehicle answer is similar to what was entered in previous page
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
        }*/

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

// Use contains() for more robust XPath in CI headless mode
        WebElement passengerVehicleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(),'Is this registration for a passenger vehicle?')]/following-sibling::td")
        ));
        String passengerVehicleAnswer = passengerVehicleElement.getText().trim();
        Assert.assertEquals(properties.getProperty("vehicleOption"), passengerVehicleAnswer);
        System.out.println("Passenger vehicle shown as " + passengerVehicleAnswer);

// --- Compare purchase value ---
        WebElement purchaseValueElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[contains(text(),'Purchase price or value')]/following-sibling::td")
        ));
        String purchaseValue = purchaseValueElement.getText().replace("$", "")
                .replace(",", "")
                .replace(".00", "")
                .trim();
        Assert.assertEquals(properties.getProperty("purchaseAmount"), purchaseValue);
        System.out.println("Purchase amount shown as " + purchaseValue);

// --- Validate duty payable ---
        WebElement dutyPayableElement = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("td.focus"))).get(7);
        String dutyPayable = dutyPayableElement.getText().trim();

        if (!dutyPayable.isEmpty()) {
            System.out.println("Duty payable text is visible: " + dutyPayable);
        } else {
            System.out.println("Duty payable text is not visible");
            Assert.fail("Duty payable text is missing");
        }
        driver.quit();
    }
}

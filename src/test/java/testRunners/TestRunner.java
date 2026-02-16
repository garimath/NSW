package testRunners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepDefinitions"},
        tags = "@api or @login",
        plugin = {"pretty","html:target/cucumber-report.html","json:target/cucumber.json"}
)

public class TestRunner {
}

package WebScrapers;

import org.openqa.selenium.Dimension; // Selenium Dimension
import org.openqa.selenium.WebDriver;

import java.awt.Toolkit;


public class MaximizeWindowIfNot {

    public static void maximizeWindowIfNot(WebDriver driver) {
        // Get the screen size
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Get the current browser size
        Dimension browserSize = driver.manage().window().getSize();
        int browserWidth = browserSize.width;
        int browserHeight = browserSize.height;

        // Compare browser size with screen size
        if (browserWidth < screenWidth || browserHeight < screenHeight) {
            driver.manage().window().maximize();
        }
    }
  
}

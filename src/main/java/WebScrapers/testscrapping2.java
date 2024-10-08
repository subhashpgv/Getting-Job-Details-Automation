package WebScrapers;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import Utilities.ExtentManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class testscrapping2 {
    private static final String REPORT_PATH = "path/to/your/report.html"; // Update this path to your desired report location

    public static void main(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        // Initialize the report
        ExtentManager.initReport(REPORT_PATH);

        // Start the test
        ExtentManager.startTest("Job Scraping Test", "Test to scrape job listings from weworkremotely.com");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=1920x1080");
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        driver.get("https://weworkremotely.com/remote-jobs/search?search_uuid=&term=&sort=any_time&categories%5B%5D=2&categories%5B%5D=17&categories%5B%5D=18&region%5B%5D=1&region%5B%5D=5&region%5B%5D=6&region%5B%5D=7&company_size%5B%5D=1+-+10&company_size%5B%5D=11+-+50");
        driver.manage().window().maximize();
        sleepRandom();
        System.out.println("ADDING JOBS FROM \"weworkremotely.com\"");

        String source = "weworkremotely.com";
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<String[]> jobDetailsList = new ArrayList<>();
        Connection connection = null;
        int totalJobCount=0;
        int totalJobsAppended = 0;
        int totalJobFinds = 0;

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div//ul//li/a//span[@class='title']")));

            List<WebElement> totalJobs = driver.findElements(By.xpath("//div//ul//li/a//span[@class='title']"));
            totalJobCount = totalJobs.size();

            int[] sections = {2, 17, 18};
           
            List<String> tabs = null;

            for (int sectionId : sections) {
                String companyName = null;
                String jobTitle = null;
                String jobLocation = null;
                String jobURL = null;
                String employeeCount = "1-50";
                String companyWebsite = null;
                String dateCreated = null;

                String viewAll = null;
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.xpath("//section[@id='category-" + sectionId + "']//li[@class='view-all']/a")));
                } catch (Exception e) {
                    switch (sectionId) {
                        case 2:
                            System.out.println("No jobs found for Full-stack programming");
                            break;
                        case 17:
                            System.out.println("No jobs found for Front-end programming");
                            break;
                        case 18:
                            System.out.println("No jobs found for back-end programming");
                            break;
                    }
                }

                WebElement viewAllElement = getElementIfExists(driver, "//section[@id='category-" + sectionId + "']//li[@class='view-all']/a");
                if (viewAllElement != null) {
                    viewAll = viewAllElement.getAttribute("href");
                }

                String script = "window.open(arguments[0], '_blank');";
                js.executeScript(script, viewAll);
                sleepRandom();

                tabs = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(1));

                List<WebElement> resultCountElement = driver.findElements(By.xpath("//section[@id='category-" + sectionId + "']//li/a//span[@class='title']"));

                for (int i = 1; i <= resultCountElement.size(); i++) {
                    System.out.println("Adding Jobs for " + source + " please wait until it shows completed.....");

                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.xpath("(//section[@id='category-" + sectionId + "']//li/a//span[@class='title'])[" + i + "]")));

                    WebElement companyNames = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li/a//span[@class='company'][1])[" + i + "]");
                    if (companyNames != null) {
                        companyName = companyNames.getText();
                    }

                    WebElement jobTitles = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li/a//span[@class='title'])[" + i + "]");
                    if (jobTitles != null) {
                        jobTitle = jobTitles.getText();
                    }

                    WebElement jobLocations = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li/a//span[@class='region company'])[" + i + "]");
                    if (jobLocations != null) {
                        jobLocation = jobLocations.getText();
                    }

                    WebElement jobURLs = getElementIfExists(driver, "(//section[@id='category-" + sectionId + "']//li/a//span[@class='region company'])[" + i + "]/parent::a");
                    if (jobURLs != null) {
                        jobURL = jobURLs.getAttribute("href");
                    }

                    js.executeScript(script, jobURL);
                    sleepRandom();

                    tabs = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(tabs.get(2));

                    WebElement companyWebsites = getElementIfExists(driver, "//div[@class='company-card border-box']//a[normalize-space()='Website']");
                    if (companyWebsites != null) {
                        companyWebsite = companyWebsites.getAttribute("href");
                    }

                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    dateCreated = now.format(formatter);

                    jobDetailsList.add(new String[]{jobTitle, jobLocation, jobURL, companyName, employeeCount,
                            companyWebsite, source, dateCreated});

                    totalJobFinds++;

                    driver.close();
                    driver.switchTo().window(tabs.get(1));
                }
                driver.close();
                driver.switchTo().window(tabs.get(0));
            }

        } catch (Exception e) {
            System.out.println("Code did not execute completely.-- " + source);
            e.printStackTrace();
            String screenshotPath = takeScreenshot(driver, "error");
            ExtentManager.addScreenshot(screenshotPath);
        } finally {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
                connection = DriverManager.getConnection(connectionURL);

                // SQL queries
                String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";
                ResultSet resultSet = null;
                // Check and insert jobs into the database
                String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                for (String[] jobDetails : jobDetailsList) {
                    String jobURL = jobDetails[2];

                    // Check if job URL already exists
                    PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
                    checkStatement.setString(1, jobURL);
                    resultSet = checkStatement.executeQuery();
                    if (resultSet.next() && resultSet.getInt(1) == 0) {
                        // Insert new job listing
                        PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
                        for (int j = 0; j < jobDetails.length; j++) {
                            insertStatement.setString(j + 1, jobDetails[j]);
                        }
                        insertStatement.executeUpdate();
                        insertStatement.close();
                        totalJobsAppended++;
                    }
                    resultSet.close();
                    checkStatement.close();
                }

                if (totalJobCount == totalJobFinds) {
                    System.out.println("Searched all companies for new jobs.-- " + source);
                }

                if (totalJobsAppended > 0) {
                    System.out.println(totalJobsAppended + " jobs added to DB successfully. -" + source);
                } else {
                    System.out.println("No new jobs found.-- " + source);
                }
            } catch (Exception e) {
                System.out.println("Error in Jobs adding to data base - " + source);
                e.printStackTrace();
            } finally {
                if (driver != null) {
                    driver.quit();
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Flush the report
            ExtentManager.flushReport();
        }
    }

    private static WebElement getElementIfExists(WebDriver driver, String xpath) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        } catch (Exception e) {
            return null;
        }
    }

    private static void sleepRandom() {
        try {
            int delay = new Random().nextInt(2000) + 1000; // Delay between 1 and 2 seconds
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String takeScreenshot(WebDriver driver, String fileName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            File destination = new File("C:/Users/user01/Desktop/Automation Scrapping Code Error Screenshots/"
                    + fileName + "_" + timestamp + ".png");
            FileUtils.copyFile(source, destination);
            System.out.println("Screenshot taken in " + source + " :" + destination.getPath());
            return destination.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}


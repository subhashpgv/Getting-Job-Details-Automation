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
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import Utilities.ExtentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

public class testscrap2 {

    static String sources = "weworkremotely.com";
    static int totalJobCount = 0;  // Add this variable to track the total number of jobs
    static int totalJobsAppended = 0;
    

    public static void main(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        // Initialize ExtentReports
    	 String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
         String reportPath = "C:/Users/user01/Desktop/Extended Reports/" +sources+ "_"+ timestamp + ".html";
        ExtentManager.initReport(reportPath);
        ExtentManager.startTest("Job Scraping Test - weworkremotely", "Automated job scraping from weworkremotely.com");
        System.out.println("ADDING JOBS FROM \"weworkremotely.com\"");

        EdgeOptions options = new EdgeOptions();
        // Set headless mode
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--proxy-server='direct://'");
        options.addArguments("--proxy-bypass-list=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");


        WebDriver driver = new EdgeDriver(options);

        List<String[]> jobDetailsList = new ArrayList<>();
        
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get("https://weworkremotely.com/remote-jobs/search?search_uuid=&term=&sort=any_time&categories%5B%5D=2&categories%5B%5D=17&categories%5B%5D=18&region%5B%5D=1&region%5B%5D=5&region%5B%5D=6&region%5B%5D=7&company_size%5B%5D=1+-+10&company_size%5B%5D=11+-+50");
            driver.manage().window().maximize();
            sleepRandom();
            ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, "Navigated to weworkremotely.com");
           

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

           
            Connection connection = null;

            int[] sections = {2, 17, 18};
            int totalJobFinds = 0;  // Add this variable to track the number of jobs found

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
                    WebElement t = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//section[@id='category-" + sectionId + "']//li[@class='view-all']/a")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", t);
                } catch (Exception e) {
                	 String screenshotPath = takeScreenshot(driver, "error");
                	e.printStackTrace();
                	
                    switch (sectionId) {
                        case 2:
                            ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, "No jobs found for Full-stack programming");
                            break;
                        case 17:
                            ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, "No jobs found for Front-end programming");
                            break;
                        case 18:
                            ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, "No jobs found for back-end programming");
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
                	
                   	System.out.println("looking Jobs from "+sources +" please wait untill completed" );

                    ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, "Adding Jobs for " + sources + " please wait until it shows completed.....");

                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.xpath("(//section[@id='category-" + sectionId + "']//li/a//span[@class='title'])[" + i + "]")));

                    // Handle each element and check if it exists
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

                    WebElement CompanyWebsites = getElementIfExists(driver, "//div[@class='company-card border-box']//a[normalize-space()='Website']");
                    if (CompanyWebsites != null) {
                        companyWebsite = CompanyWebsites.getAttribute("href");
                    }

                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    dateCreated = now.format(formatter);

                    jobDetailsList.add(new String[]{jobTitle, jobLocation, jobURL, companyName, employeeCount,
                            companyWebsite, sources, dateCreated});

                    totalJobFinds++;

                    driver.close();
                    driver.switchTo().window(tabs.get(1));
                }
                driver.close();
                driver.switchTo().window(tabs.get(0));
            }

            // After scraping all sections, report the results
            if (totalJobCount == totalJobFinds) {
                ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, "Searched all companies for new jobs.-- " + sources);
                System.out.println("Searched all companies for new jobs.-- " + sources);
            }

            if (totalJobsAppended > 0) {
                ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, totalJobsAppended + " jobs added to DB successfully. -" + sources);
                System.out.println( totalJobsAppended + " jobs added to DB successfully. -" + sources);
            } else {
                ExtentManager.getTest().log(com.aventstack.extentreports.Status.INFO, "No new jobs found.-- " + sources);
                System.out.println("No new jobs found.-- " + sources);
            }

        } catch (Exception e) {
        	ExtentManager.getTest().fail("Error occurred: " + e.getMessage());
            ExtentManager.getTest().log(com.aventstack.extentreports.Status.FAIL, "Code did not execute completely. -- " + sources);
            System.out.println("Code did not execute completely. -- " + sources);
            ExtentManager.getTest().fail("Error occurred: " + e.getMessage());
            e.printStackTrace();
            String screenshotPath = takeScreenshot(driver, "error");
            ExtentManager.addScreenshot(screenshotPath);
        } finally {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection connection = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=JobDatabase;user=sa;password=password");
                String checkSQL = "SELECT COUNT(*) FROM Jobs WHERE JobURL = ?";
                String insertSQL = "INSERT INTO Jobs (JobTitle, JobLocation, JobURL, CompanyName, EmployeeCount, CompanyWebsite, Source, DateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
                PreparedStatement insertStatement = connection.prepareStatement(insertSQL);

                for (String[] jobDetails : jobDetailsList) {
                    checkStatement.setString(1, jobDetails[2]);
                    ResultSet resultSet = checkStatement.executeQuery();
                    if (resultSet.next() && resultSet.getInt(1) == 0) {
                        // Insert new job record
                        for (int i = 0; i < jobDetails.length; i++) {
                            insertStatement.setString(i + 1, jobDetails[i]);
                        }
                        insertStatement.executeUpdate();
                        totalJobsAppended++;
                    }
                }
                connection.close();
            } catch (SQLException | ClassNotFoundException e) {
                ExtentManager.getTest().log(com.aventstack.extentreports.Status.FAIL, "Error inserting job data into SQL database. " + e.getMessage());
                System.out.println("Error inserting job data into SQL database. " + e.getMessage());
                e.printStackTrace();
                ExtentManager.getTest().fail("Error occurred: " + e.getMessage());
            }
            driver.quit();
            ExtentManager.flushReport();
        }
    }

    private static void sleepRandom() throws InterruptedException {
        Random rand = new Random();
        int randomDelay = rand.nextInt(5000) + 1000; // 1 to 5 seconds
        Thread.sleep(randomDelay);
    }

    private static WebElement getElementIfExists(WebDriver driver, String xpath) {
        try {
            return driver.findElement(By.xpath(xpath));
        } catch (Exception e) {
            return null;
        }
    }

    private static String takeScreenshot(WebDriver driver, String screenshotName) throws IOException {
    	String screenshotPath = null;
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destinationFile = new File("C:/Users/user01/Desktop/Extended Reports/Automation Scrapping Code Error Screenshots/" + screenshotName + "_" +
                                         DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()) + ".png");
        FileUtils.copyFile(screenshot, destinationFile);
        System.out.println("Screenshot taken in " + sources + " :" + destinationFile.getPath());
		return screenshotPath;
    }
}

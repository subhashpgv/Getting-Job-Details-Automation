package WebScrapers;

import Utilities.ExtentManager;
import com.aventstack.extentreports.Status;
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class testscrap4 {
    static String sources = "workingnomads.com";
    
    public static void main(String[] args) {
        WebDriver driver = null;
        Connection connection = null;
        List<String[]> jobDetailsList = new ArrayList<>();
        int totalJobsAppended = 0;
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        String reportPath = "C:/Users/user01/Desktop/Extended Reports/" +sources+ "_"+ timestamp + ".html";

        // Initialize ExtentReports
        ExtentManager.initReport(reportPath);
        ExtentManager.startTest("Job Scraping Test - workingnomads.com", "Automated job scraping from workingnomads.com");

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--window-size=1920x1080");
            options.addArguments("--disable-gpu");
            driver = new ChromeDriver(options);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            driver.get("https://www.workingnomads.com/jobs?location=europe,australia,usa,uk&category=development");
            driver.manage().window().maximize();

            ExtentManager.getTest().log(Status.INFO, "Navigated to workingnomads.com");
            System.out.println("ADDING JOBS FROM \"workingnomads.com\"");
            sleepRandom();

            WebElement resultCountElement = driver.findElement(By.xpath("(//div[contains(@class,'total-number')])[1]"));
            String resultText = resultCountElement.getText();
            String parts = resultText.split(" ")[0];
            NumberFormat format = NumberFormat.getInstance(Locale.US);
            int totalJobCount = 0;
            try {
                Number number = format.parse(parts);
                totalJobCount = number.intValue();
                ExtentManager.getTest().log(Status.INFO, "Total Job count for workingnomads.com is " + totalJobCount);
                System.out.println("Total Job count for workingnomads.com is " + totalJobCount);
            } catch (Exception e) {
                ExtentManager.getTest().log(Status.FAIL, "Error parsing job count in workingnomads.com");
                System.out.println("Error parsing job count in workingnomads.com");
                String screenshotPath = takeScreenshot(driver, "error_"+sources);
                ExtentManager.addScreenshot(screenshotPath);
            }

            for (int i = 1; i <= totalJobCount; i++) {
                String companyName = "";
                String jobTitle = "";
                String jobLocation = "";
                String jobURL = "";
                String companyWebsite = "";
                String employeeCount = "";
                String dateCreated = "";

                ExtentManager.getTest().log(Status.INFO, "Looking at Job " + i + " from " + sources);
                System.out.println("Looking at Job " + i + " from " + sources);
                
                List<WebElement> totalJobsOnPage = getElementsIfExists(driver, "(//div[@class='job-cols']//h4[1]//a)");
                if (totalJobsOnPage.size() == i&&totalJobsOnPage.size()<i) {
                    driver.findElement(By.xpath("//div[@class='show-more']")).click();
                    sleepRandom();
                    int k=i+1;
                    try{
                    	 WebElement viewElement = getElementIfExists(driver, "(//div[@class='job-cols']//h4[1]//a)[" + k + "]");
                    	if(viewElement==null) {
                    		System.out.println("No more jobs displyed after clicking on 'see more' - " + sources);
							break;
                    	}
                    } catch (Exception e) {
    					ExtentManager.getTest().log(Status.FAIL, "inner break perforemed at 'See more'" + i );
    					System.out.println("inner break perforemed at 'See more'" + i );
    					String screenshotPath = takeScreenshot(driver, "error_"+sources);
    	                ExtentManager.addScreenshot(screenshotPath);
    					e.printStackTrace();
    					break;
    				}
                }

                WebElement jobTitleElement = getElementIfExists(driver, "(//div[@class='job-cols']//h4[1]//a)[" + i + "]");
                if (i % 2 == 0) {
                    int j = i - 1;
                    js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(By.xpath("(//div[@class='job-cols']//h4[1]/a)[" + j + "]")));
                }

                if (jobTitleElement != null) {
                    jobTitle = jobTitleElement.getText();
                }

                WebElement jobLinkElement = getElementIfExists(driver, "(//div[@class='job-cols']//h4[1]/a)[" + i + "]");
                if (jobLinkElement != null) {
                    jobURL = jobLinkElement.getAttribute("href");
                    sleepRandom();

                    WebElement jobLocationElement = getElementIfExists(driver, "(//div[contains(@class,'boxes')]//div[contains(@ng-show,'source.locations')])[" + i + "]");
                    if (jobLocationElement != null) {
                        jobLocation = jobLocationElement.getText();
                    }

                    WebElement companyNameElement = getElementIfExists(driver, "(//div[@class='job-cols'])[" + i + "]//div[contains(@class,'company')]/a");
                    if (companyNameElement != null) {
                        companyName = companyNameElement.getText();
                        String URL = companyNameElement.getAttribute("href");

                        String script = "window.open(arguments[0], '_blank');";
                        js.executeScript(script, URL);

                        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                        driver.switchTo().window(tabs.get(1));
                        sleepRandom();

                        WebElement companyUrlElement1 = getElementIfExists(driver, "//div[@class='company-links']/a");
                        WebElement companyUrlElement2 = getElementIfExists(driver, "//div[@class='job-company']//a");

                        companyWebsite = (companyUrlElement1 != null) ? companyUrlElement1.getAttribute("href") :
                                (companyUrlElement2 != null ? companyUrlElement2.getAttribute("href") : null);

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        dateCreated = now.format(formatter);

                        jobDetailsList.add(new String[]{jobTitle, jobLocation, jobURL, companyName, employeeCount,
                                companyWebsite, sources, dateCreated});

                        driver.close();
                        driver.switchTo().window(tabs.get(0));
                    }
                }
            }
        } catch (Exception e) {
        	
            ExtentManager.getTest().log(Status.FAIL, "Error occurred during scraping."+sources);
            System.out.println("Error occurred during scraping."+sources);
            ExtentManager.getTest().fail("Error occurred: " + e.getMessage());
            String screenshotPath = takeScreenshot(driver, "error_"+sources);
            ExtentManager.addScreenshot(screenshotPath);
            e.printStackTrace();
        } finally {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
                connection = DriverManager.getConnection(connectionURL);

                String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";
                String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                ResultSet resultSet = null;
                for (String[] jobDetails : jobDetailsList) {
                    String jobURL = jobDetails[2];

                    PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
                    checkStatement.setString(1, jobURL);
                    resultSet = checkStatement.executeQuery();
                    if (resultSet.next() && resultSet.getInt(1) == 0) {
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

                if (totalJobsAppended > 0) {
                    ExtentManager.getTest().log(Status.INFO, totalJobsAppended + " jobs added to DB successfully.-- " + sources);
                    System.out.println(totalJobsAppended + " jobs added to DB successfully.-- " + sources);
                } else {
                    ExtentManager.getTest().log(Status.INFO, "No new jobs found.-- " + sources);
                    System.out.println("No new jobs found.-- " + sources);
                }
            } catch (Exception e) {
                ExtentManager.getTest().log(Status.FAIL, "Error in adding jobs to database. -- " + sources);
                System.out.println("Error in adding jobs to database. -- " + sources);
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
            // Flush the report at the end of the test
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
    
    private static List<WebElement> getElementsIfExists(WebDriver driver, String xpath) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xpath)));
        } catch (Exception e) {
            return Collections.emptyList(); // Return an empty list if elements are not found or any exception occurs
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
    	
    	String screenshotPath =null; 
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            screenshotPath="C:/Users/user01/Desktop/Extended Reports/Automation Scrapping Code Error Screenshots/"
                    + fileName + "_" + timestamp + ".png";
            File destination = new File(screenshotPath);
            FileUtils.copyFile(source, destination);
            ExtentManager.getTest().addScreenCaptureFromPath(destination.getPath());
            System.out.println("Screenshot taken: " + destination.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
		return screenshotPath;
    }
}

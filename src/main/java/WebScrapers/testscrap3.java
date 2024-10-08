package WebScrapers;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.aventstack.extentreports.Status;

import Utilities.ExtentManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class testscrap3 {
    static String source = "jobgether.com";
    private static final String[] LOCATIONS = { "UK", "Europe", "Australia", "USA" };

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ExecutorService executor = null;
        try {
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String reportPath = "C:/Users/user01/Desktop/Extended Reports/" + source + "_" + timestamp + ".html";
            ExtentManager.initReport(reportPath);
            System.out.println("ADDING JOBS FROM  \"jobgether.com\"");

            // Create a thread pool with a fixed number of threads, one for each location
            executor = Executors.newFixedThreadPool(LOCATIONS.length);

            // List to hold Future objects representing the completion of submitted tasks
            List<Future<?>> futures = new ArrayList<>();

            // Submit a new task for each location to the thread pool
            for (String location : LOCATIONS) {
                futures.add(executor.submit(new JobScraperTask3(location)));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    // get() will block until the task is complete
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("All tasks completed._"+source);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ExtentManager.flushReport();
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown(); // Ensure executor is shut down gracefully
            }
        }
    }
}
class JobScraperTask3 implements Runnable {

	private String location = null;

	public JobScraperTask3(String location) {
		this.location = location;
	}

	@Override
	public void run() {
		WebDriver driver = null;
		Connection connection = null;
		List<String[]> jobDetailsList = new ArrayList<>();
		String source = "jobgether.com";
		int totalJobsAppended = 0;
		try {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless");
			options.addArguments("--window-size=1920x1080");
			options.addArguments("--disable-gpu");
			driver = new ChromeDriver(options);

			ExtentManager.startTest("Job Scraping Test -" + source + ": " + location,
					"Automated job scraping from " + source + ": " + location);
			if (location == "USA") {
				driver.get(
						"https://jobgether.com/remote-jobs/united-states?industries=computer-software-saas&industries=information-technology-services&sort=date");
			} else if (location == "UK") {
				driver.get(
						"https://jobgether.com/remote-jobs/united-kingdom?industries=computer-software-saas&industries=information-technology-services&sort=date");
			} else if (location == "Australia") {
				driver.get(
						"https://jobgether.com/remote-jobs/australia?industries=computer-software-saas&industries=information-technology-services&sort=date");
			} else if (location == "Europe") {
				driver.get(
						"https://jobgether.com/remote-jobs/europe?industries=computer-software-saas&industries=information-technology-services&sort=date");
			}

			driver.manage().window().maximize();

			ExtentManager.getTest().log(Status.INFO, "Navigated to " + source + " : " + location);

			Thread.sleep(8000);
			handlePopUp(driver);

			WebElement resultCountElement = getElementIfExists(driver,
					"//div[contains(text(),'jobs found')]/child::span/parent::div");
			String resultText = resultCountElement.getText();
			String[] parts = resultText.split(" ");
			int totalJobCount = Integer.parseInt(parts[0].trim());
			System.out.println(location + "- total Job Count :" + totalJobCount);
			int PageNaviagtionCount = 2;

			for (int i = 1; i <= totalJobCount; i++) {

				System.out.println(
						"Adding Jobs for \"" + source + "\" please wait until it shows completed....." + location);
				List<WebElement> TotalJobsOnPage = getElementsIfExists(driver, "//div[@id='offer-body']/div/div/h3");
				

				WebElement jobTitleElement = getElementIfExists(driver,
						"(//div[@id='offer-body'])[" + i + "]/div/div/h3");

				if (i % 2 == 0 && i <= TotalJobsOnPage.size()) {
					int j = i - 1;
					WebElement ScrollElement = getElementIfExists(driver,
							"(//div[@id='offer-body'])[" + j + "]/div/div/h3");
					if (jobTitleElement != null) {
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
								ScrollElement);
					}
				}

				if (jobTitleElement == null) {
					System.out.println("No jobs showing (or) Jobs list might be at the end -- " + location);
					break;
				}

				String jobTitle = jobTitleElement.getText();

				WebElement jobLinkElement = getElementIfExists(driver,
						"(//div[@id='offer-body']/parent::div/preceding-sibling::a)[" + i + "]");

				if (jobLinkElement != null) {
					jobLinkElement.click();
					sleepRandom();
				}

				List<String> tabs = new ArrayList<>(driver.getWindowHandles());

				try {
					driver.switchTo().window(tabs.get(1));

					String jobURL = driver.getCurrentUrl();

					if (jobURL == null) {
						continue;
					}

					WebElement companySizeElement = getElementIfExists(driver,
							"//div[contains(@class,'flex justify-center')]/following-sibling::div//span");
					String companySize = companySizeElement != null ? companySizeElement.getText() : "";

					List<String> validSizes = Arrays.asList("11 - 50", "2 - 10", "51 - 200");

					if (companySizeElement == null) {
						continue;
					}

					WebElement jobLocationElement = getElementIfExists(driver,
							"//div[@id='offer_general_data']//span[contains(.,'Work from:')]/following-sibling::div");
					String jobLocation = jobLocationElement != null ? jobLocationElement.getText() : "";

					WebElement companyNameElement = getElementIfExists(driver,
							"//div[contains(@class,'flex justify-center')]/following-sibling::span[1]");
					String companyName = companyNameElement != null ? companyNameElement.getText() : "";

					WebElement companyUrlElement = getElementIfExists(driver,
							"//div[contains(@class,'flex justify-center')]/following-sibling::a");
					String companyWebsite = companyUrlElement != null ? companyUrlElement.getAttribute("href") : "";

					String dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

					if (validSizes.contains(companySize)) {
						jobDetailsList.add(new String[] { jobTitle, jobLocation, jobURL, companyName, companySize,
								companyWebsite, source, dateCreated });
					}
					
				

				} catch (Exception e) {

					ExtentManager.getTest().log(Status.FAIL,
							"Code Not executed completely for " + location + "--" + source);
					System.out.println("Code Not executed completely for " + location + "--" + source);
					String screenshotPath = takeScreenshot(driver, "error_" + source, location);
					ExtentManager.addScreenshot(screenshotPath);

					e.printStackTrace();

				} finally {
					driver.close();
					driver.switchTo().window(tabs.get(0));
				}
				
				try {
					
					

					if (TotalJobsOnPage.size() == i && i <= totalJobCount) {
						
						int k = i + 1;
						WebElement ScrollElement = getElementIfExists(driver,
								"(//div[@id='offer-body'])[" + i + "]/div/div/h3");
						if (ScrollElement != null) {
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
									ScrollElement);

							WebElement naviagteButton = getElementIfExists(driver,
									"//nav/a[normalize-space()='"+ PageNaviagtionCount +"']");
		
							if (naviagteButton != null) {
								naviagteButton.click();
								sleepRandom();
								PageNaviagtionCount++;
								i = 0;
								((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
								
							} else {

								System.out.println("page naviagtion might at the end.(or) max reading limt reached -500 records "+source+location);
								break;

							}
						}
					}
				} catch (Exception e) {
					ExtentManager.getTest().log(Status.FAIL,
							"inner break perforemed at 'page naviagtion'" + i + location);
					System.out.println("inner break perforemed at 'page naviagtion'" + i + location);
					String screenshotPath = takeScreenshot(driver, "error_" + source, location);
					ExtentManager.addScreenshot(screenshotPath);
					e.printStackTrace();
					break;
				}

			}

		} catch (Exception e) {
			String screenshotPath = takeScreenshot(driver, "error_" + source, location);
			ExtentManager.addScreenshot(screenshotPath);
			e.printStackTrace();
			if (driver != null) {
				driver.quit();
			}
		} finally {
			// SQL connection setup
			try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
				connection = DriverManager.getConnection(connectionURL);

				// SQL queries
				String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";
				String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

				for (String[] jobDetails : jobDetailsList) {
					String jobURL = jobDetails[2];

					PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
					checkStatement.setString(1, jobURL);
					ResultSet resultSet = checkStatement.executeQuery();

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
					ExtentManager.getTest().log(Status.INFO,totalJobsAppended + " jobs added to DB successfully.--" + source + "--" + location);
					System.out.println(
							totalJobsAppended + " jobs added to DB successfully.--" + source + "--" + location);
				} else {
					ExtentManager.getTest().log(Status.INFO, "No new jobs found.--" + source +location);
					System.out.println("No new jobs found.--" + source +location);
				}
			} catch (Exception e) {
				ExtentManager.getTest().log(Status.FAIL, "Error in adding jobs to database. -- " + source);
				System.out.println("Error in adding jobs to database. -- " + source);
				e.printStackTrace();
			} finally {

				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (driver != null) {
					driver.quit();
				}
			}
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
			return Collections.emptyList();
		}
	}

	private static void sleepRandom() {
		try {
			int delay = new Random().nextInt(2000) + 1000;
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void handlePopUp(WebDriver driver) {
		try {
			WebElement closeButton = getElementIfExists(driver, "//button[@data-pc-section='closebutton']");
			if (closeButton != null) {
				closeButton.click();
				System.out.println("Pop-up closed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String takeScreenshot(WebDriver driver, String fileName, String location) {

		try {

			String screenshotPath = null;
			String source = "jobtogrther.com";

			TakesScreenshot ts = (TakesScreenshot) driver;
			File sources = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			screenshotPath = "C:/Users/user01/Desktop/Extended Reports/Automation Scrapping Code Error Screenshots/"

					+ fileName + "_" + timestamp + ".png";
			File destination = new File(screenshotPath);
			FileUtils.copyFile(sources, destination);
			System.out.println("Screenshot taken in " + source + " :" + location + " :" + destination.getPath());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return location;
	}
}

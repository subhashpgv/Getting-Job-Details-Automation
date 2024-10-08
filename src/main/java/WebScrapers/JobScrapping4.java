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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;

import java.io.File;
import java.io.IOException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import java.util.Random;

public class JobScrapping4 {
	 static String sources = null;
	public static void main(String[] args) {
		WebDriver driver = null;
		Connection connection = null;

		List<String[]> jobDetailsList = new ArrayList<>();
		int totalJobsAppended = 0;
		
		try {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless");
			options.addArguments("--window-size=1920x1080");
			options.addArguments("--disable-gpu");
			driver = new ChromeDriver(options);

			JavascriptExecutor js = (JavascriptExecutor) driver;
			driver.get("https://www.workingnomads.com/jobs?location=europe,australia,usa,uk&category=development");
			driver.manage().window().maximize();

			System.out.println("ADDING JOBS FROM \"workingnomads.com\"");

			Thread.sleep(5000);

			WebElement resultCountElement = driver.findElement(By.xpath("(//div[contains(@class,'total-number')])[1]"));
			String resultText = resultCountElement.getText();
			String parts = resultText.split(" ")[0];
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			int totalJobCount = 0;
			try {
				Number number = format.parse(parts);
				totalJobCount = number.intValue();
				System.out.println("Total Job count for workingnomads.com is "+totalJobCount );

			} catch (Exception e) {
				e.printStackTrace();
				 takeScreenshot( driver,"error");
			}

			for (int i = 1; i <= totalJobCount; i++) {
				String companyName = "";
				String jobTitle = "";
				String jobLocation = "";
				String jobURL = "";
				String companyWebsite = "";
				sources = "workingnomads.com";
				String employeeCount = "";
				String dateCreated = "";

				System.out.println("looking Job " + i + " from " + sources + " please wait until it shows completed.....");
				
				List<WebElement> TotalJobsOnPage = getElementsIfExists(driver,"(//div[@class='job-cols']//h4[1]//a)");
				if (TotalJobsOnPage.size() == i) {

					driver.findElement(By.xpath("//div[@class='show-more']")).click();
				}

				WebElement jobTitleElement = getElementIfExists(driver,"(//div[@class='job-cols']//h4[1]//a)[" + i + "]");
				// Make webelement on focus
				if (i % 2 == 0) {
					int j = i - 1;
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
							driver.findElement(By.xpath("(//div[@class='job-cols']//h4[1]/a)[" + j + "]")));
				}

					if (jobTitleElement != null) {
						jobTitle = jobTitleElement.getText();

					}

					WebElement jobLinkElement = getElementIfExists(driver,
							"(//div[@class='job-cols']//h4[1]/a)[" + i + "]");

					if (jobLinkElement != null) {
						jobURL = jobLinkElement.getAttribute("href");
						sleepRandom();

						// Extract additional details
						WebElement jobLocationElement = getElementIfExists(driver,
								"(//div[contains(@class,'boxes')]//div[contains(@ng-show,'source.locations')])[" + i+ "]");
						if (jobLocationElement != null) {
							jobLocation = jobLocationElement.getText();
						}

						String URL = null;
						WebElement companyNameElement = getElementIfExists(driver,
								"(//div[@class='job-cols'])[" + i + "]//div[contains(@class,'company')]/a");
						if (companyNameElement != null) {
							companyName = companyNameElement.getText();
							URL = companyNameElement.getAttribute("href");

						}

						String script = "window.open(arguments[0], '_blank');";
						js.executeScript(script, URL);

						List<String> tabs = new ArrayList<>(driver.getWindowHandles());
						driver.switchTo().window(tabs.get(1));
						sleepRandom();

						WebElement companyUrlElement1 = getElementIfExists(driver, "//div[@class='company-links']/a");
						WebElement companyUrlElement2 = getElementIfExists(driver, "//div[@class='job-company']//a");

						companyWebsite = (companyUrlElement1 != null)? companyUrlElement1.getAttribute("href")
								: (companyUrlElement2 != null ? companyUrlElement2.getAttribute("href") : null);

						LocalDateTime now = LocalDateTime.now();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						dateCreated = now.format(formatter);

						jobDetailsList.add(new String[] { jobTitle, jobLocation, jobURL, companyName, employeeCount,
								companyWebsite, sources, dateCreated });

						driver.close();
						driver.switchTo().window(tabs.get(0));
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			 takeScreenshot( driver,"error");

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

				// Summary of results

				if (totalJobsAppended > 0) {
					System.out.println(totalJobsAppended + " jobs added to DB successfully.--" + sources);
				} else {
					System.out.println("No new jobs found.--" + sources);
				}

			} catch (Exception e) {
				System.out.println("Error in Jobs adding to data base. -- " + sources);
				e.printStackTrace();
			}

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

	private static void takeScreenshot(WebDriver driver, String fileName) {
		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			File destination = new File("C:/Users/svegi/eclipse-workspace/WebScrapers/ExtendReports/screenshots/"
					+ fileName + "_" + timestamp + ".png");
			FileUtils.copyFile(source, destination);
			System.out.println("Screenshot taken in "+sources+" :" + destination.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

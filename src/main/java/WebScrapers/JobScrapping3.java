package WebScrapers;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

public class JobScrapping3 {

	private static final String[] LOCATIONS = { "Europe", "Australia", "UK", "USA" };

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		System.out.println("ADDING JOBS FROM  \"jobgether.com\"");

		// Create a thread pool with a fixed number of threads, one for each location
		ExecutorService executor = Executors.newFixedThreadPool(LOCATIONS.length);

		// Submit a new task for each location to the thread pool
		for (String location : LOCATIONS) {
			executor.execute(new JobScraperTask1(location));
		}

		// Shutdown the executor service after all tasks are completed
		executor.shutdown();
	}
}

class JobScraperTask1 implements Runnable {

	private String location = null;

	public JobScraperTask1(String location) {
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
//			options.addArguments("--headless");
//			options.addArguments("--window-size=1920x1080");
//			options.addArguments("--disable-gpu");
			driver = new ChromeDriver(options);

			if (location == "USA") {
				driver.get(
						"https://jobgether.com/search-offers?locations=622a65bd671f2c8b98faca1a&industries=62448b478cb2bb9b3540b791&industries=62448b478cb2bb9b3540b78f&sort=date");
			} else if (location == "UK") {
				driver.get(
						"https://jobgether.com/search-offers?locations=622a65b4671f2c8b98fac83f&industries=62448b478cb2bb9b3540b791&industries=62448b478cb2bb9b3540b78f&sort=date");
			} else if (location == "Australia") {
				driver.get(
						"https://jobgether.com/search-offers?locations=622a65b0671f2c8b98fac759&industries=62448b478cb2bb9b3540b791&industries=62448b478cb2bb9b3540b78f&sort=date");
			} else if (location == "Europe") {
				driver.get(
						"https://jobgether.com/search-offers?locations=622a659af0bac38678ed1398&industries=62448b478cb2bb9b3540b791&industries=62448b478cb2bb9b3540b78f&sort=date");
			}
			driver.manage().window().maximize();

			Thread.sleep(8000);
			handlePopUp(driver);

			WebElement resultCountElement = getElementIfExists(driver,
					"//div[contains(@class,'sort_counter_container')]/div/div[1]");
			String resultText = resultCountElement.getText();
			String[] parts = resultText.split(" ");
			int totalJobCount = Integer.parseInt(parts[0].trim());

			for (int i = 1; i <= totalJobCount; i++) {

				System.out.println(
						"Adding Jobs for \"" + source + "\" please wait until it shows completed....." + location);

				try {
					List<WebElement> TotalJobsOnPage = getElementsIfExists(driver,
							"//div[@id='offer-body']/div/div/h3");

					if (TotalJobsOnPage.size() == i && i <= totalJobCount) {
						int j = i - 2;
						WebElement ScrollElement = getElementIfExists(driver,
								"(//div[@id='offer-body'])[" + j + "]/div/div/h3");
						if (ScrollElement != null) {
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
									ScrollElement);
							WebElement seemore = getElementIfExists(driver, "//a[normalize-space()='See more']");
							if (seemore != null) {
								seemore.click();
								sleepRandom();
							}
						}
					}
				} catch (Exception e) {
					System.out.println("inner break perforemed at 'See more'" + i + location);
					takeScreenshot(driver, "error", location);
					e.printStackTrace();
					break;
				}

				WebElement jobTitleElement = getElementIfExists(driver,
						"(//div[@id='offer-body'])[" + i + "]/div/div/h3");

				if (i % 2 == 0 && i <= totalJobCount) {
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

					WebElement jobLocationElement = getElementIfExists(driver,
							"//div[@id='offer_general_data']//span[contains(.,'Work from:')]/following-sibling::div");
					String jobLocation = jobLocationElement != null ? jobLocationElement.getText() : "";

					WebElement companyNameElement = getElementIfExists(driver,
							"//div[contains(@class,'flex justify-center')]/following-sibling::span[1]");
					String companyName = companyNameElement != null ? companyNameElement.getText() : "";

					WebElement companyUrlElement = getElementIfExists(driver,
							"//div[contains(@class,'flex justify-center')]/following-sibling::a");
					String companyWebsite = companyUrlElement != null ? companyUrlElement.getAttribute("href") : "";

					WebElement companySizeElement = getElementIfExists(driver,
							"//div[contains(@class,'flex justify-center')]/following-sibling::div//span");
					String companySize = companySizeElement != null ? companySizeElement.getText() : "";

					String dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

					List<String> validSizes = Arrays.asList("11 - 50", "2 - 10", "51 - 200");

					if (validSizes.contains(companySize)) {
						jobDetailsList.add(new String[] { jobTitle, jobLocation, jobURL, companyName, companySize,
								companyWebsite, source, dateCreated });
					}

				} catch (Exception e) {
					takeScreenshot(driver, "error", location);
					e.printStackTrace();
					System.out.println("Code Not executed completely for " + location + "--" + source);
				} finally {
					driver.close();
					driver.switchTo().window(tabs.get(0));
				}

			}

		} catch (Exception e) {
			takeScreenshot(driver, "error", location);
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
					System.out.println(totalJobsAppended + " jobs added to DB successfully.--" + source);
				} else {
					System.out.println("No new jobs found.--" + source);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println(location + " : " + totalJobsAppended);
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
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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

	private static void takeScreenshot(WebDriver driver, String fileName, String location) {

		try {

			String source = "jobtogrther.com";

			TakesScreenshot ts = (TakesScreenshot) driver;
			File sources = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			File destination = new File("C:/Users/user01/Desktop/Automation Scrapping Code Error Screenshots/"
					+ fileName + "_" + timestamp + ".png");
			FileUtils.copyFile(sources, destination);
			System.out.println("Screenshot taken in "+sources+" :" + location+ " :" + destination.getPath());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

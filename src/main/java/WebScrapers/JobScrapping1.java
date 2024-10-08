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
import org.openqa.selenium.interactions.Actions;

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

public class JobScrapping1 {
	public static void main(String[] args)
			throws IOException, InterruptedException, SQLException, ClassNotFoundException {

		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--window-size=1920x1080");
		options.addArguments("--disable-gpu");
		WebDriver driver = new ChromeDriver(options);
		Actions actions = new Actions(driver);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		
		String source = "ycombinator.com";

		
		driver.get("https://account.ycombinator.com/?continue=https%3A%2F%2Fwww.workatastartup.com%2F");
		driver.manage().window().maximize();
		sleepRandom();
		System.out.println("ADDING JOBS FROM \"www.ycombinator.com\"");

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("(//div[@class='MuiFormControl-root input-group'])[1]")));
		driver.findElement(By.xpath("//input[@id='ycid-input']")).sendKeys("vikram-katta");
		WebElement element = driver.findElement(By.xpath("//label[normalize-space()='Password']"));
		actions.moveToElement(element).click().perform();
		driver.findElement(By.xpath("//input[@id='password-input']")).sendKeys("ABCD@1432");
		driver.findElement(By.xpath("//span[@class='MuiButton-label']")).click();
		sleepRandom();

		openUrl(driver);
		int totalmatching = getTotalMatchingStartups(driver);
		System.out.println("total job records in \"ycombinator\" is : "+ totalmatching);
		List<String[]> jobDetailsList = new ArrayList<>();

		Connection connection = null;

		try {
			for (int i = 1; i <= totalmatching; i++) {
				System.out.println("Adding Jobs for "+source +" please wait until it shows completed.....");

				
				String xpathExpression = String.format(
						"(//div[contains(@class,'mb-5 rounded pb-4')])[%d]//div[contains(@class,'font-medium')]", i);
				WebElement jobListing = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpathExpression)));
				jobListing.click();
				sleepRandom();

			
				List<String> tab = new ArrayList<>(driver.getWindowHandles());
				sleepRandom();
				driver.switchTo().window(tab.get(2));

				
				List<WebElement> jobTitles = driver.findElements(
						By.xpath("//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a"));

				for (int j = 1; j <= jobTitles.size(); j++) {
					String companyName = driver.findElement(By.xpath("//span[@class='company-name hover:underline']"))
							.getText();
					String jobTitle = driver.findElement(By
							.xpath("(//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a)["
									+ j + "]"))
							.getText();
					String jobLocation = driver.findElement(
							By.xpath("(//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name'])["
									+ j + "]//following-sibling::div/span[1]"))
							.getText();
					String jobURL = driver.findElement(By
							.xpath("(//div[contains(@class,'justify-between sm:flex-row')]//div[@class='job-name']//a)["
									+ j + "]"))
							.getAttribute("href");
					String companyWebsite = driver
							.findElement(By.xpath("(//div[@class='text-sm'])[1]/div[1]/div/div[2]/a"))
							.getAttribute("href");
					String dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					String employeeCount = getEmployeeCount(driver);
					
					

					// Store job details in the two-dimensional array
					jobDetailsList.add(new String[] {jobTitle, jobLocation, jobURL, companyName, employeeCount,
							companyWebsite, source, dateCreated});
				}

				
				driver.close();
				driver.switchTo().window(tab.get(1));
			}

		} catch (Exception e) {
			System.out.println("Code did not execute completely.-- "+source);
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
				int totalJobsAppended=0;
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
					System.out.println(totalJobsAppended + " jobs added to DB successfully. -- "+source);
				} else {
					System.out.println("No new jobs found.-- "+source);
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

			} catch (Exception e) {
				System.out.println("Error in Jobs adding to data base "+source);
				e.printStackTrace();
				
			}
		}
	}

	private static int getTotalMatchingStartups(WebDriver driver) {
		WebElement resultCountElement = driver
				.findElement(By.xpath("//p[contains(normalize-space(.), 'matching startups')]"));
		String resultText = resultCountElement.getText();
		String[] parts = resultText.split(" ");
		return Integer.parseInt(parts[1].trim());
	}

	private static String getEmployeeCount(WebDriver driver) {
		String employeeCount = null;
		WebElement employeesElement1 = getElementIfExists(driver,
				"//i[contains(@title,'people')]/following-sibling::div");
		WebElement employeesElement2 = getElementIfExists(driver,
				"//i[contains(@title,'person')]/following-sibling::div");

		if (employeesElement1 != null) {
			employeeCount = employeesElement1.getText().split(" ")[0].trim();
		} else if (employeesElement2 != null) {
			employeeCount = employeesElement2.getText().split(" ")[0].trim();
		}
		return employeeCount;
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
			int delay = new Random().nextInt(2000) + 1000; // Delay between 1 and 3 seconds
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void openUrl(WebDriver driver) throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

		String URL = "https://www.workatastartup.com/companies?companySize=seed&companySize=small&demographic=any&hasEquity=any&hasSalary=any&industry=any&interviewProcess=any&jobType=any&layout=list-compact&locations=US&locations=GB&locations=AU&locations=AT&locations=BE&locations=BG&locations=HR&locations=CY&locations=CZ&locations=DK&locations=FI&locations=FR&locations=DE&locations=GR&locations=HU&locations=IT&locations=MT&locations=NL&role=eng&sortBy=created_desc&tab=any&usVisaNotRequired=any";
		String script = "window.open(arguments[0], '_blank');";
		sleepRandom();
		js.executeScript(script, URL);
		Thread.sleep(5000);

		List<String> tabs = new ArrayList<>(driver.getWindowHandles());
		driver.switchTo().window(tabs.get(1));

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//p[contains(normalize-space(.), 'matching startups')]")));
		sleepRandom();
	}
	
	private static void takeScreenshot(WebDriver driver, String fileName) {
		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			File destination = new File("C:/Users/user01/Desktop/Automation Scrapping Code Error Screenshots/"
					+ fileName + "_" + timestamp + ".png");
			FileUtils.copyFile(source, destination);
			System.out.println("Screenshot taken in "+source+" :" + destination.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package WebScrapers;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.interactions.Actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;

public class JobScrapping5 {
	public static void main(String[] args) {
		WebDriver driver = null;
		Connection connection = null;

		try {
			EdgeOptions options = new EdgeOptions();
//			options.addArguments("--headless");
//			options.addArguments("--window-size=1920x1080");
//			options.addArguments("--disable-gpu");
			driver = new EdgeDriver(options);

			JavascriptExecutor js = (JavascriptExecutor) driver;
			driver.get(
					"https://himalayas.app/jobs/countries/united-states?type=full-time%2Cpart-time%2Ccontractor%2Ctemporary%2Cintern%2Cother&markets=software-development%2Capp-development%2Centerprise-software%2Cweb-development%2Csoftware%2Cux-design%2Cwordpress%2Cproduct-development");
			driver.manage().window().maximize();

			System.out.println("ADDING JOBS FROM \"himalayas.app\"");

			Thread.sleep(5000);

			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionURL = "jdbc:sqlserver://10.0.2.34:1433;Database=Automation;User=mailscan;Password=MailScan@343260;encrypt=true;trustServerCertificate=true";
			connection = DriverManager.getConnection(connectionURL);

			String insertSQL = "INSERT INTO JobListings (jobTitle, jobLocations, jobUrl, companyName, employeeCount, companyWebsite, source, dateCreated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			String checkSQL = "SELECT COUNT(*) FROM JobListings WHERE jobUrl = ?";

			WebElement resultCountElement = driver.findElement(By.xpath("//a[contains(text(),'Jobs')]"));
			String resultText = resultCountElement.getText();
			String parts = resultText.split("\n")[1];
			NumberFormat format = NumberFormat.getInstance(Locale.US);
			int totalJobCount = 0;
			int itrate = 0;
			try {
				Number number = format.parse(parts);
				totalJobCount = number.intValue();
				itrate = (int) Math.round((double) totalJobCount / 20);

			} catch (Exception e) {
				e.printStackTrace();
			}

			int totalJobsAppended = 0;

			for (int k = 0; k <= itrate; k++) {
				for (int i = 1; i <= 20; i++) {
					String companyName = "";
					String jobTitle = "";
					String jobLocation = "";
					String jobURL = "";
					String companyWebsite = "";
					String source = "himalayas.app";
					String companySize = "";
					String dateCreated = "";

					System.out.println("Adding JObs to DB please wait untill it shows completed.....");

					WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
					
					wait.until(ExpectedConditions
							.presenceOfElementLocated(By.xpath("(//div[@class='w-full flex-1']/div/a)[" + i + "]")));
					
					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//div[@class='w-full flex-1']/div/a)[" + i + "]")));
					WebElement jobTitleElement = getElementIfExists(driver,
							"(//div[@class='w-full flex-1']/div/a)[" + i + "]");
					// Make webelement on focus
					if (i % 2 == 0) {
						int j = i - 1;
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
								driver.findElement(By.xpath("(//div[@class='w-full flex-1']/div/a)[" + j + "]")));
					}

					if (jobTitleElement != null) {
						jobTitle = jobTitleElement.getText();

					}

					WebElement companySizes = getElementIfExists(driver,
							"(//div[@class='w-full flex-1']//div/div/p)[" + i + "]");

					if (companySizes != null) {
						String companySizeText = companySizes.getText();
						companySize = companySizeText.split("\n")[1];
					}

					List<String> validSizes = Arrays.asList("11-50", "51-200", "1-10");

					if (validSizes.contains(companySize)) {
						jobURL = jobTitleElement.getAttribute("href");

						String script = "window.open(arguments[0], '_blank');";
						js.executeScript(script, jobURL);
						sleepRandom();

						List<String> tabs = new ArrayList<>(driver.getWindowHandles());
						driver.switchTo().window(tabs.get(1));

						List<WebElement> jobLocationElements = getElementsIfExists(driver,
								"(//h3[.='Location requirements'])[2]/parent::div/parent::div/div[2]/a");

						List<String> jobLocationList = new ArrayList<>();
						if (jobLocationElements != null) {
							for (WebElement Location : jobLocationElements) {

								String Locations = Location.getText();
								jobLocationList.add(Locations);
							}
							String[] jobLocations = jobLocationList.toArray(new String[0]);
							jobLocation = String.join(", ", jobLocations);
						}

						WebElement companyNameElement = getElementIfExists(driver,
								"//section[2]/a[contains(@href,'companies')]/div/h2");
						if (companyNameElement != null) {
							companyName = companyNameElement.getText();
						}

						WebElement companyUrlElement1 = getElementIfExists(driver,
								"//section[2]/a[contains(@href,'companies')]/div/h2/ancestor::a");
						if (companyUrlElement1 != null) {
							companyWebsite = companyUrlElement1.getAttribute("href");
						}

						LocalDateTime now = LocalDateTime.now();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						dateCreated = now.format(formatter);

						// Check if job URL already exists
						PreparedStatement checkStatement = connection.prepareStatement(checkSQL);
						checkStatement.setString(1, jobURL);
						ResultSet resultSet = checkStatement.executeQuery();

						if (resultSet.next() && resultSet.getInt(1) == 0) {

							// Insert new job listing
							PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
							insertStatement.setString(1, jobTitle);
							insertStatement.setString(2, jobLocation);
							insertStatement.setString(3, jobURL);
							insertStatement.setString(4, companyName);
							insertStatement.setString(5, companySize);
							insertStatement.setString(6, companyWebsite);
							insertStatement.setString(7, source);
							insertStatement.setString(8, dateCreated);
							insertStatement.executeUpdate();
							insertStatement.close();
							totalJobsAppended++;

						}

						resultSet.close();
						checkStatement.close();

						driver.close();
						driver.switchTo().window(tabs.get(0));
						if (i == 20) {
							System.out.println(i);
						}
						if (i % 20 == 0) {
							WebElement next = getElementIfExists(driver, "//span[normalize-space()='Next']");
							if (next != null) {
								next.click();
							} else {
								driver.quit();
							}
						}
					}
					if (i == 20) {
						System.out.println(i);
					}
				}

			}
			System.out.println("Total jobs appended: " + totalJobsAppended);

		} catch (Exception e) {
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
	}

	private static WebElement getElementIfExists(WebDriver driver, String xpath) {
		try {
			List<WebElement> elements = driver.findElements(By.xpath(xpath));
			if (elements.size() > 0) {
				return elements.get(0);
			}
		} catch (NoSuchElementException e) {
			// Element is not found, return null
		}
		return null;
	}

	private static List<WebElement> getElementsIfExists(WebDriver driver, String xpath) {
		try {
			List<WebElement> elements = driver.findElements(By.xpath(xpath));
			if (elements.size() > 0) {
				return elements;
			}
		} catch (NoSuchElementException e) {
			// Element is not found, return null
		}
		return null;
	}

	private static void sleepRandom() {
		try {
			int delay = new Random().nextInt(2000) + 1000; // Delay between 1 and 2 seconds
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

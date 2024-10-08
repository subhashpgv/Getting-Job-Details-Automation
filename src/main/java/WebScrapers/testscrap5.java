package WebScrapers;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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

import com.aventstack.extentreports.Status;

import Utilities.ExtentManager;

import org.openqa.selenium.support.ui.ExpectedConditions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.io.File;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
public class testscrap5 {
	static String source = "himalayas.app";

	private static final String[] LOCATIONS = {"united-states","united-kingdom","australia","france","germany","spain","italy" };
//	
	 public static void main(String[] args) throws SQLException, ClassNotFoundException {
	        ExecutorService executor = null;
	        try {
	            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
	            String reportPath = "C:/Users/user01/Desktop/Extended Reports/" + source + "_" + timestamp + ".html";
	            ExtentManager.initReport(reportPath);
	            System.out.println("ADDING JOBS FROM  \"himalayas.app\"");

	            // Create a thread pool with a fixed number of threads, one for each location
	            executor = Executors.newFixedThreadPool(LOCATIONS.length);

	            // List to hold Future objects representing the completion of submitted tasks
	            List<Future<?>> futures = new ArrayList<>();

	            // Submit a new task for each location to the thread pool
	            for (String location : LOCATIONS) {
	                futures.add(executor.submit(new JobScraperTask5(location)));
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


class JobScraperTask5 implements Runnable {

	private String location = null;

	public JobScraperTask5(String location) {
		this.location = location;
		
	}
	
	
	
	@Override
	public void run() {
		WebDriver driver = null;
		Connection connection = null;
		String source = "himalayas.app";
		
		List<String[]> jobDetailsList = new ArrayList<>();
	

		int totalJobCount = 0;
		int totalJobsAppended = 0;
		int itrate = 0;

		try {
			try {
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless");
			options.addArguments("--disable-gpu");
			options.addArguments("--window-size=1920,1080");
			options.addArguments("--disable-blink-features=AutomationControlled");
			options.addArguments("--disable-extensions");
			options.addArguments("--proxy-server='direct://'");
			options.addArguments("--proxy-bypass-list=*");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--no-sandbox");
			options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
					+ "Chrome/85.0.4183.83 Safari/537.36");
			
			driver = new ChromeDriver(options);

			ExtentManager.startTest("Job Scraping Test -" + source + ": " + location,
					"Automated job scraping from " + source + ": " + location);
			
			driver.get(
					"https://himalayas.app/jobs/countries/"+location+"?type=full-time%2Cpart-time%2Ccontractor%2Ctemporary%2Cintern&sort=recent&markets=fintech%2Centerprise-software%2Cweb-development%2Csoftware-development%2Cmachine-learning%2Csoftware%2Ccloud-computing%2Cweb-design%2Capp-development%2Cmobile-development%2Cinformation-technology%2Cdevops%2Cmobile-app-development%2Cui-design");
			driver.manage().window().maximize();
			Thread.sleep(5000);

			

			WebElement resultCountElement = driver.findElement(By.xpath("//a[contains(text(),'Jobs')]"));
			
			String resultText = resultCountElement.getText();
			String parts = resultText.split("\n")[1];
			NumberFormat format = NumberFormat.getInstance(Locale.US);

			Number number = format.parse(parts);
			totalJobCount = number.intValue();
			itrate = (int) Math.round((double) totalJobCount / 20);
			int h = 1;
			
			System.out.println("ADDING JOBS FROM \"himalayas.app\"-" +location + " - total job count :"+totalJobCount);
	 		ExtentManager.getTest().log(Status.INFO, "ADDING JOBS FROM \"himalayas.app\"-" +location + " - total job count :"+totalJobCount );
			
	 		 List<WebElement> NoOFjobTitle = driver.findElements(By.xpath("//div[@class='w-full flex-1']/div/a"));
	 		
	 		
				for (int k = 1; k <= itrate; k++) {
					for (int i = 1; i <= NoOFjobTitle.size(); i++) {
						String companyName = "";
						String jobTitle = "";
						String jobLocation = "";
						String jobURL = "";
						String companyWebsite = "";

						String employeeCount = "";
						String dateCreated = "";

						System.out.println("Adding JOb "+h+" please wait untill it shows completed.....-"+source+" : " +location );
						h++;

						WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

						wait.until(ExpectedConditions.presenceOfElementLocated(
								By.xpath("(//div[@class='w-full flex-1']/div/a)[" + i + "]")));

					
						WebElement jobTitleElement = getElementIfExists(driver,
								"(//div[@class='w-full flex-1']/div/a)[" + i + "]");
						// Make webelement on focus
						JavascriptExecutor js = (JavascriptExecutor) driver;
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
							employeeCount = companySizeText.split("\n")[1];
						}

						List<String> validSizes = Arrays.asList("11-50", "51-200", "1-10");

						if (validSizes.contains(employeeCount)) {
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

							jobDetailsList.add(new String[] { jobTitle, jobLocation, jobURL, companyName, employeeCount,
									companyWebsite, source, dateCreated });

							

							driver.close();
							driver.switchTo().window(tabs.get(0));
						}
							if (i % 20 == 0) {
								WebElement next = getElementIfExists(driver, "//span[normalize-space()='Next']");
								if (next != null) {
									next.click();
									sleepRandom();
									((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
											driver.findElement(By.xpath("(//div[@class='flex w-full flex-col'])[1]")));
									
								} else {
									System.out.println("Job list might be at the end at -" + h +source+" : " +location);
									ExtentManager.getTest().log(Status.INFO, "Job list might be at the end at -" + h +source+" : " +location);
									driver.quit();
									break;
								}
							}
						

					}

				}
				System.out.println("Total jobs appended: " + totalJobsAppended+" : "  + source+" : " +location);
				ExtentManager.getTest().log(Status.INFO, "Total jobs appended: " + totalJobsAppended +" : " +source+" : " +location);
			} catch (Exception e) {
				ExtentManager.getTest().log(Status.FAIL, "Code did not execute completely.-- " + source+" : " +location);
				System.out.println("Code did not execute completely.-- " + source+" : -" +location);
				e.printStackTrace();
				ExtentManager.getTest().fail("Error occurred: " + e.getMessage());
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
					
					if (totalJobsAppended > 0) {
						ExtentManager.getTest().log(Status.INFO,totalJobsAppended + " jobs added to DB successfully.--" + source + "--" + location);
						System.out.println(
								totalJobsAppended + " jobs added to DB successfully.--" + source + "--" + location);
					} else {
						ExtentManager.getTest().log(Status.INFO, "No new jobs found.--" + source+":-" +location);
						System.out.println("No new jobs found.--" + source +location);
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
					ExtentManager.getTest().log(Status.FAIL, "Error in Jobs adding to data base - " + source+" : " +location);
					System.out.println("Error in Jobs adding to data base - " + source+" : " +location);
					e.printStackTrace();
					ExtentManager.getTest().fail("Error occurred: " + e.getMessage());
				}
			}
		} finally {

			if (driver != null) {
				driver.quit();
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

	private static String takeScreenshot(WebDriver driver, String fileName) {
		String screenshotPath = null;
		String source = "himalayas.app";

		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File sources = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			screenshotPath = "C:/Users/user01/Desktop/Extended Reports/Automation Scrapping Code Error Screenshots/"
					+ fileName + "_" + timestamp + ".png";
			File destination = new File(screenshotPath);
			FileUtils.copyFile(sources, destination);

			System.out.println("Screenshot taken in " + source + " :" + destination.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return screenshotPath;
	}
}

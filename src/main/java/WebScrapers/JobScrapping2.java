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

public class JobScrapping2 {
	
	static String sources=null;
    public static void main(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=1920x1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--log-level=ALL"); 
        WebDriver driver = new ChromeDriver(options);
  
        JavascriptExecutor js = (JavascriptExecutor) driver;

        driver.get("https://weworkremotely.com/remote-jobs/search?search_uuid=&term=&sort=any_time&categories%5B%5D=2&categories%5B%5D=17&categories%5B%5D=18&region%5B%5D=1&region%5B%5D=5&region%5B%5D=6&region%5B%5D=7&company_size%5B%5D=1+-+10&company_size%5B%5D=11+-+50");
        driver.manage().window().maximize();
        sleepRandom();
        System.out.println("ADDING JOBS FROM \"weworkremotely.com\"");
        
        

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        
    	List<String[]> jobDetailsList = new ArrayList<>();
    	Connection connection = null;

    	wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//div//ul//li/a//span[@class='title']")));

       List<WebElement> totalJobs = driver.findElements(By.xpath("//div//ul//li/a//span[@class='title']"));
       int totalJobCount = totalJobs.size();

        int[] sections = {2, 17, 18};
        int totalJobsAppended = 0;
        int totalJobFinds =0;
      
        List<String> tabs = null;
      
        try {
        for (int sectionId : sections) {

        	String companyName = null;
            String jobTitle = null;
            String jobLocation = null;
            String jobURL = null;
            String employeeCount="1-50";
            String companyWebsite= null;
            sources= "weworkremotely.com";
            String dateCreated = null;
            
            String viewAll=null;
           try { 
        	  
        	   WebElement t = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//section[@id='category-"+ sectionId +"']//li[@class='view-all']/a")));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",t);
           }catch(Exception e){
        	   switch (sectionId) {
				case 2:
					System.out.println(" No jobs found for Full-stack programming ");
					break;
				case 17:
					System.out.println(" No jobs found for  Front-end programming ");
					break;
				case 18:
					System.out.println(" No jobs found for back-end programming ");
					break;
				}
        	 }
           
            WebElement viewAllElement  = getElementIfExists(driver, "//section[@id='category-"+ sectionId +"']//li[@class='view-all']/a");
            if (viewAllElement != null) {
            	viewAll= viewAllElement.getAttribute("href");
            	
            }
            
            String script = "window.open(arguments[0], '_blank');";
	        js.executeScript(script, viewAll);
	        sleepRandom();
	        
	        tabs = new ArrayList<>(driver.getWindowHandles());
			driver.switchTo().window(tabs.get(1));
            
        	
        	List<WebElement> resultCountElement = driver.findElements(By.xpath("//section[@id='category-"+ sectionId +"']//li/a//span[@class='title']"));
   
        	for (int i = 1; i <= resultCountElement.size(); i++) {
        		
        		System.out.println("Adding Jobs for "+sources +" please wait until it shows completed.....");
        		
        		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",driver.findElement(By.xpath("(//section[@id='category-"+ sectionId +"']//li/a//span[@class='title'])[" + i + "]")));

                // Handle each element and check if it exists
                WebElement companyNames = getElementIfExists(driver, "(//section[@id='category-"+ sectionId +"']//li/a//span[@class='company'][1])[" + i + "]");
                if (companyNames != null) {
                    companyName = companyNames.getText();
                }

                WebElement jobTitles = getElementIfExists(driver, "(//section[@id='category-"+ sectionId +"']//li/a//span[@class='title'])[" + i + "]");
                if (jobTitles != null) {
                    jobTitle = jobTitles.getText();
                }

                WebElement jobLocations = getElementIfExists(driver, "(//section[@id='category-"+ sectionId +"']//li/a//span[@class='region company'])[" + i + "]");
                if (jobLocations != null) {
                    jobLocation = jobLocations.getText();
                }

                WebElement jobURLs = getElementIfExists(driver, "(//section[@id='category-"+ sectionId +"']//li/a//span[@class='region company'])[" + i + "]/parent::a");
                if (jobURLs != null) {
                    jobURL = jobURLs.getAttribute("href");
                }
                
		       
		        js.executeScript(script, jobURL);
		        sleepRandom();

				tabs = new ArrayList<>(driver.getWindowHandles());
				driver.switchTo().window(tabs.get(2));
				
				
				WebElement CompanyWebsites  = getElementIfExists(driver, "//div[@class='company-card border-box']//a[normalize-space()='Website']");
				if (CompanyWebsites != null) {
					companyWebsite = CompanyWebsites.getAttribute("href");
                }
				
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				dateCreated = now.format(formatter);
                
				jobDetailsList.add(new String[] {jobTitle, jobLocation, jobURL, companyName, employeeCount,
						companyWebsite, sources, dateCreated});
				
				totalJobFinds++;
				
				driver.close();
				driver.switchTo().window(tabs.get(1));
        	}	
        	driver.close();
			driver.switchTo().window(tabs.get(0));
        }
        
        }catch(Exception e) {
        	System.out.println("Code did not execute completely.-- "+sources);
			e.printStackTrace();
			
			 takeScreenshot( driver,"error");

			 
        }finally {
        	
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
			            System.out.println("Searched all companies for new jobs.-- "+sources);
			        }
				 
				if (totalJobsAppended > 0) {
					System.out.println(totalJobsAppended + " jobs added to DB successfully. -"+sources);
				} else {
					System.out.println("No new jobs found.-- "+sources);
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
				
				System.out.println("Error in Jobs adding to data base - "+sources);
				e.printStackTrace();
			}
        	
        }

        driver.quit(); // Make sure to quit the WebDriver
        connection.close();
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
    
	private static void takeScreenshot(WebDriver driver, String fileName) {
		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
			File destination = new File("C:/Users/user01/Desktop/Automation Scrapping Code Error Screenshots/"
					+ fileName + "_" + timestamp + ".png");
			FileUtils.copyFile(source, destination);
			System.out.println("Screenshot taken in "+sources+" :" + destination.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

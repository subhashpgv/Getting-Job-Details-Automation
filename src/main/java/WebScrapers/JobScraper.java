package WebScrapers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;

public class JobScraper {
	public static void main(String[] args) throws IOException {

		ChromeOptions options = new ChromeOptions();
		// Uncomment the line below if you want to run in headless mode
		//options.addArguments("--headless");

		WebDriver driver = new ChromeDriver(options);
		Actions actions = new Actions(driver);

		driver.get("https://wellfound.com/role/l/software-engineer/united-states?page=1");
		driver.manage().window().maximize();

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("styles_resultsList__Q46cW")));

		FileWriter csvWriter = new FileWriter("job_listings.csv");
		
		
		
		csvWriter.append("Job Title,Job URL,Company Name,Company URL,Company Size,Company Website,Location\n");

		WebElement resultCountElement = driver.findElement(By.cssSelector(".styles_resultCount__Biln8"));
		String resultText = resultCountElement.getText();

		String[] parts = resultText.split("of ");
		String numberStr = parts[1].trim();
		int totalPages = Integer.parseInt(numberStr);

		

			List<WebElement> jobListings = driver.findElements(By.className("styles_result__rPRNG"));
			// Loop through each job listing
			for (WebElement listing : jobListings) {

				WebElement companyNameElement = listing
						.findElement(By.xpath(".//div[contains(@class,'styles_startupHeader')]//a//h4"));
				WebElement companyURLElement = listing
						.findElement(By.xpath(".//div[contains(@class,'styles_startupHeader')]//a"));
				String companyNameText = companyNameElement.getText().trim();
				String companyURLText = companyURLElement.getAttribute("href");
				
				//csvWriter.append(companyURLText).append("\n");

				WebElement companySizeElement = listing
						.findElement(By.xpath(".//div[contains(@class,'styles_companySize')]"));
				String companySize = companySizeElement.getText().trim();

				if (companySize.contains("1-10") || companySize.contains("11-50")) {
					
					companyURLElement.click();					

					//actions.keyDown(org.openqa.selenium.Keys.CONTROL).click(companyURLElement).keyUp(org.openqa.selenium.Keys.CONTROL).build().perform();

					// Switch to the new tab
					List<String> tabs = new ArrayList<>(driver.getWindowHandles());
				//	driver.switchTo().window(tabs.get(1));
					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'styles_name')]//h3//a")));
					String companyName = driver.findElement(By.xpath("//div[contains(@class,'styles_name')]//h3//a")).getText();
					String companyWebsite = driver.findElement(By.xpath("//aside//div//button[contains(@class,'styles_websiteLink')]")).getText();
					List<WebElement> companyLocations = driver.findElements(By.xpath("//div[@class='styles_main__EGHwE']//dt[2]"));

					StringBuilder locations = new StringBuilder();
					for (int i = 0; i < companyLocations.size() && i < 5; i++) {
						locations.append(companyLocations.get(i).getText()).append(";");
					}

					WebElement viewAllJobsButton = driver.findElement(By.xpath("//span[normalize-space()='View all jobs']"));
					if (viewAllJobsButton.isDisplayed()) {
						viewAllJobsButton.click();
					}
					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='styles_component__2UhSH styles_expanded__YwudJ']")));
					List<WebElement> jobList =  driver.findElements(By.xpath("//div[@class='styles_component__2UhSH styles_expanded__YwudJ']"));
					
					for(WebElement jobLists : jobList)	{
						
						String jobTitle = jobLists.findElement(By.xpath("//div[@class='styles_jobList__5MFDX']//a[2]//h4"))
								.getText();
						String jobUrl = jobLists.findElement(By.xpath("//div[@class='styles_jobList__5MFDX']//a[2]"))
								.getAttribute("href");
						
						csvWriter.append(jobTitle).append(jobUrl).append(",").append(companyName).append(",")
								.append(companyURLText).append(",").append(companySize).append(",").append(companyWebsite)
								.append(",").append(locations.toString()).append("\n");
					}		
					
				}
			}

		// Close the CSV writer
		csvWriter.flush();
		csvWriter.close();

		System.out.println("Scraping completed and data saved to job_listings.csv");

	}
}

package sff_practice10_final_RTM;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static sff_practice10_final_RTM.DriverType.FIREFOX;
import static sff_practice10_final_RTM.DriverType.CHROME;

interface Driver {
	public WebDriver getWebDriver();
}

enum DriverType implements Driver {
	CHROME{
		public WebDriver getWebDriver(){
			System.setProperty("webdriver.chrome.driver",
					System.getProperty("user.dir") + "\\lib\\chromedriver.exe");
			return new ChromeDriver();
		}
	},
	FIREFOX{
		public WebDriver getWebDriver(){
			return new FirefoxDriver();
		}
	};
}

class DriverFactory {
	private static final DriverType dt = CHROME;
	private static ThreadLocal<WebDriver> tl = 
			new ThreadLocal<WebDriver>();

	@BeforeMethod
	public WebDriver createWebDriver() throws IllegalAccessException{
		WebDriver driver = null;

		switch(dt){
		case CHROME:
			driver = CHROME.getWebDriver();
			break;
		case FIREFOX:
			driver = FIREFOX.getWebDriver();
			break;
		default:
			throw 
			new IllegalAccessException("undefined driver type.");
		}

		setWebDriver(driver);
		return getWebDriver();
	}

	public static WebDriver getWebDriver(){
		return tl.get();
	}

	private void setWebDriver(WebDriver driver){
		tl.set(driver);
	}

	@AfterMethod
	public void releaseWebDriver(){
		getWebDriver().quit();
		tl.remove();
	}

}

class SeleniumBase extends DriverFactory {
	protected long WAIT_TIMEOUT = 30; //secs
	protected Object[][] getTestData(){
		return new Object[][]{
				{"seattle"},
//				{"key arena"},
//				{"antique road show"},
//				{"seahawks"},{"seattle storm"},
				{"mariners"}
		};
	}	
}

class GoogleLoginPage {
	private String URL = "http://google.com";
	private long WAIT_TIMEOUT = 30; //secs

	public GoogleLoginPage() throws Exception{
		WebDriver driver = DriverFactory.getWebDriver();
		PageFactory.initElements(driver, this); //BUG WAS HERE! had WAIT_TIMEOUT instead of this!!
		driver.get(URL);
		driver.manage().window().maximize();
	}
	
	@FindBy(xpath = "//*[@name='q']")
	private WebElement searchBox;
	
//	@FindBy(name = "q")
//	private WebElement searchBox;

	public void searchGoogle(String term) throws Exception {
		WebDriver driver = DriverFactory.getWebDriver();

		try{
			//wait for search box to appear - by proxy
			new WebDriverWait(driver, WAIT_TIMEOUT)
			.until(ExpectedConditions.visibilityOf(searchBox));
		}
		catch(Exception e){
			System.out.println("searchGoogle exception - " + e.getMessage());
		}

		//send the search text
		searchBox.sendKeys(term);
		searchBox.submit();
	}
}


public class GoogleSearchTest extends SeleniumBase {

	private void googleSearchTest(String term){

		try {
			//arrange - create test object
			GoogleLoginPage glp = new GoogleLoginPage();

			//act - invoke search method
			glp.searchGoogle(term);

			//assert after search results page is displayed
			Assert.assertTrue(
					new WebDriverWait(getWebDriver(), WAIT_TIMEOUT)
					.until(ExpectedConditions.titleContains(term))
					);
		}
		catch(Exception e) {
			System.out.println("Exception in googleSearchTest: " + e.getMessage());
			Assert.assertTrue(false);

		}
	}

	@Test(dataProvider = "testDataProvider")
	public void test(String term){
		googleSearchTest(term);
	}

	@DataProvider(parallel = false)
	public Object[][] testDataProvider(){
		return getTestData();
	}

}

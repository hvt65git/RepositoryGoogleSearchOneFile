package singlefileframework;

import org.openqa.selenium.By;

import com.google.common.base.Predicate;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/***
 * 
 * @author focalpt - My Selenium and TestNG framework in a single file
 * 					 supports multithreaded testing when multiple test annotations 
 * 					 are used and when the dataprovider is used for a 
 * 					 single test annotation when parallel = true is set in
 *					 @DataProvider argument
 */

interface Browser{
	public WebDriver getWebDriver();
}

enum BrowserType implements Browser{
	CHROME{
		@Override
		public WebDriver getWebDriver(){
			System.setProperty("webdriver.chrome.driver", 
					System.getProperty("user.dir") + "\\lib\\chromedriver.exe");
			return new ChromeDriver();
		}
	},
	FIREFOX{
		@Override
		public WebDriver getWebDriver(){
			return new FirefoxDriver();
		}
	}
}

class WebDriverFactory{
	
	//use ThreadLocal to associate WebDriver object with a thread 
	private static ThreadLocal<WebDriver> tl = new ThreadLocal<WebDriver>();

	public WebDriverFactory(){
		System.out.println("WebDriverFactory constructor called...");
	}

	public static WebDriver initWebDriver(BrowserType bt) throws IllegalAccessException{
		WebDriver driver = null;
		switch(bt){
		case CHROME:
			driver = BrowserType.CHROME.getWebDriver();
			break;
		case FIREFOX:
			driver = BrowserType.FIREFOX.getWebDriver();
			break;
		default:
			throw 
			new IllegalAccessException("In initWebDriver. Error - Undefined BrowserType");
		};
		setWebDriver(driver);
		getWebDriver().manage().window().maximize();	
		return getWebDriver();
	}

	private static void setWebDriver(WebDriver driver){
		tl.set(driver);
	}

	public static WebDriver getWebDriver(){
		return tl.get();
	}

	protected static void releaseWebDriver(){
		getWebDriver().quit();
		tl.remove();
	}
}

class SeleniumBase extends WebDriverFactory{

	public SeleniumBase(){
		System.out.println("SeleniumBase constructor called...");
		System.setProperty("log4j.configuration", "set") ;
		System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.Jdk14Logger");
	}
}

public class GoogleSearchTest extends SeleniumBase {
	private static final long timeout = 10; //secs
	private static final String URL = "http://google.com";

	public GoogleSearchTest(){
		System.out.println("GoogleSearchTest constructor called....");
	}

	@BeforeMethod
	public void init(){
		try{
			initWebDriver(BrowserType.CHROME);
			getWebDriver().get(URL);
			getWebDriver().manage().window().maximize();
		}
		catch(Exception e){
			System.out.println("The following error occurred during init(): " + e.getMessage());
		}
	}

	@AfterMethod
	public void cleanup(){
		releaseWebDriver();
	}

	//the test
	public void executeGoogleSearchTest(final String term, BrowserType bt){
		
		try{
			//find the search txt element - note WebElement is an interface
			//lambda expression works fine here - good
			WebElement txtbox = new WebDriverWait(getWebDriver(), timeout).until(
					(WebDriver dr1) -> dr1.findElement(By.name("q")));
			txtbox.sendKeys(term);
			txtbox.submit();

			//use ExpectedCondition anon inner class implementation - good
			ExpectedCondition<Boolean> titleContainsTerm =	new ExpectedCondition<Boolean>(){ 
				public Boolean apply(WebDriver d){
					return d.getTitle().contains(term);
				}
			};
			new WebDriverWait(getWebDriver(),timeout).until(titleContainsTerm);

			//or we can use a Predicate implementation but we lose the return type
			Predicate<WebDriver> titleContainsTerm2 = d-> d.getTitle().contains(term);
			new WebDriverWait(getWebDriver(),timeout).until(titleContainsTerm2);

		}
		catch(Exception e){
			System.out.println("The following error occurred: " + e.getMessage());
		}
	}

	@Test(dataProvider="testData")
	public void test1(String term){
		executeGoogleSearchTest(term,BrowserType.CHROME);
	}

	@DataProvider(parallel = true)
	public Object[][] testData(){
		return new Object[][]{
				{"apple"},{"bengal tiger"},{"cats"},{"dogs"},{"dr.j"},
				{"frogs"},{"lion"},{"seattle"},{"seahawks"},{"sounders"},
				{"magic mountain"},{"disneyland"},{"marshawn lynch"},{"beast mode"},{"pete carroll"}
		};
	}
}



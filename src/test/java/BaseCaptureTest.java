import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.harreader.model.HarEntry;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.Inet4Address;
import java.util.List;

public class BaseCaptureTest {

    static protected WebDriver driver;
    static protected BrowserUpProxyServer proxyServer;

    @BeforeAll
    public static void beforeClass(){
        proxyServer = new BrowserUpProxyServer(); // initializing the proxy server
        proxyServer.start(1882); // starting the proxy server
        proxyServer.enableHarCaptureTypes // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
                (CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        WebDriverManager.chromedriver().setup(); // setting up the driver
    }

    @BeforeEach
    public void beforeMethod(){
        Proxy proxy = new Proxy();

        try {  // added this code block because the browser throws connection refused error.
            String hostIp = Inet4Address.getLocalHost().getHostAddress();
            proxy.setHttpProxy(hostIp + ":" + proxyServer.getPort());
            proxy.setSslProxy(hostIp + ":" + proxyServer.getPort());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        ChromeOptions options = new ChromeOptions()
                .setProxy(proxy) // add proxy config to chrome options
                .setAcceptInsecureCerts(true) // to allow insecure certification
                .addArguments("--headless"); // for headless browser
        driver = new ChromeDriver(options);
        proxyServer.newHar("google.com"); //create a new HAR with the label "google.com"

    }


    @AfterAll
    public static void afterClass(){
        proxyServer.stop(); // stop the proxy server
        driver.quit(); // quit the driver
    }

    protected void assertRequestMade(String url){
        List<HarEntry> harEntries = proxyServer.getHar().getLog().getEntries();
        boolean areRequestsMade = harEntries.stream().anyMatch(r -> r.getRequest().getUrl().contains(url)); // check if 'url' exists in har entries
        Assertions.assertTrue(areRequestsMade);
    }

    protected void assertStatusCode(HarEntry httpRequest){
        Assertions.assertEquals(200,httpRequest.getResponse().getStatus(), "Status code is not OK!");
    }

}

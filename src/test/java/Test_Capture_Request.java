import org.junit.jupiter.api.Test;

public class Test_Capture_Request extends BaseCaptureTest {

    @Test
    public void goToAmazon(){

        driver.get("https://www.google.com.tr/");
        assertRequestMade("https://www.google.com.tr/");

    }
}

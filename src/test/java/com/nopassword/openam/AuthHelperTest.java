package com.nopassword.openam;

import com.nopassword.openam.utils.AuthHelper;
import com.nopassword.openam.utils.Constants;
import java.util.Map;
import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.Test;

/**
 *
 * @author NoPassword
 */
public class AuthHelperTest {

    @Test
    public void makeValidAuthRequest() {
        Map<String, String> request
                = AuthHelper.makeAuthRequest("user@example.com", "00000000-0000-0000-0000-000000000000");

        Assert.assertNotEquals(request.get(Constants.USERNAME).length(), 0);
        Assert.assertEquals(request.get(Constants.API_KEY).length(), 36);
        Assert.assertEquals(request.get(Constants.COMMAND), Constants.USER_STATUS);
    }

    @Test
    public void makeInvalidAuthRequest() {
        ThrowingRunnable tr = () -> {
            AuthHelper.makeAuthRequest("user.com", "00000000-0000-0000-0000-000000000000");
        };
        Assert.assertThrows(IllegalArgumentException.class, tr);

        tr = () -> {
            AuthHelper.makeAuthRequest("user@example.com", "");
        };
        Assert.assertThrows(IllegalArgumentException.class, tr);
        

        tr = () -> {
            AuthHelper.makeAuthRequest(null, null);
        };
        Assert.assertThrows(IllegalArgumentException.class, tr);
    }

}

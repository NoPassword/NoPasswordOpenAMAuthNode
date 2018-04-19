package com.nopassword.openam;

import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author NoPassword
 */
public class NoPasswordAuthExternatClassIT {

    private static final String AM_AUTHENTICATE_ENDPOINT
            = "http://ec2-35-165-249-211.us-west-2.compute.amazonaws.com:8080/openam/json/realms/root/authenticate?module=NoPassword&authIndexType=service&authIndexValue=NoPassword";

    @Test
    public void testFail() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        //Get Initial Auth ID
        ResponseEntity<NoPasswordAuthCallback> entity
                = restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                        HttpMethod.POST,
                        new HttpEntity<>(httpHeaders),
                        NoPasswordAuthCallback.class);
        NoPasswordAuthCallback callback = entity.getBody();

        //Set incorrect username
        callback.setUsername("notexists");
        System.out.println(callback);

        //Authenticate to OpenAM
        try {
            restTemplate.exchange(AM_AUTHENTICATE_ENDPOINT,
                    HttpMethod.POST, new HttpEntity<>(callback, httpHeaders), String.class);
        } catch (HttpClientErrorException e) {
            //Assert response is 401
            Assert.assertEquals(e.getStatusCode(), HttpStatus.UNAUTHORIZED);
            return;
        }
        // Fail if 401 isn't received
        Assert.fail();
    }
}

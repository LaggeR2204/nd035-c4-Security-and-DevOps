package com.example.demo;

import com.auth0.jwt.JWT;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.security.SecurityConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<CreateUserRequest> json;


    @Test
    public void submitOrder() throws Exception {
        String token = createUserAndGetJWTToken("user_test1", "password123");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/order/submit/" + "user_test1")
                        .header(HttpHeaders.AUTHORIZATION, token)
        ).andReturn();

        Assert.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void submitOrderWithNotFoundUser() throws Exception {
        String token = createUserAndGetJWTToken("user_test2", "password123");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/order/submit/" + "not_found_user")
                        .header(HttpHeaders.AUTHORIZATION, token)
        ).andReturn();

        Assert.assertEquals(404, mvcResult.getResponse().getStatus());
    }

    @Test
    public void historyOrder() throws Exception {
        String token = createUserAndGetJWTToken("user_test3", "password123");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/api/order/history/" + "user_test3")
                        .header(HttpHeaders.AUTHORIZATION, token)
        ).andReturn();

        Assert.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void historyOrderWithNotFoundUser() throws Exception {
        String token = createUserAndGetJWTToken("user_test4", "password123");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/api/order/history/" + "not_found_user")
                        .header(HttpHeaders.AUTHORIZATION, token)
        ).andReturn();

        Assert.assertEquals(404, mvcResult.getResponse().getStatus());
    }

    public String createUserAndGetJWTToken(String username, String password) throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setPassword(password);
        createUserRequest.setConfirmPassword(password);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json.write(createUserRequest).getJson())
        );

        String token = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(HMAC512(SecurityConstants.SECRET.getBytes()));
        return "Bearer " + token;
    }
}

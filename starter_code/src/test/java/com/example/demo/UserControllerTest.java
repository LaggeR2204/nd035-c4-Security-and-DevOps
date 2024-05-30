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
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<CreateUserRequest> json;

    @Test
    public void createValidUser() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("admin_test1");
        createUserRequest.setPassword("admin123");
        createUserRequest.setConfirmPassword("admin123");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json.write(createUserRequest).getJson())
        ).andReturn();

        Assert.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void createUserWithAlreadyExistUsername() throws Exception {
        CreateUserRequest createExistedUserRequest = new CreateUserRequest();
        createExistedUserRequest.setUsername("admin_test2");
        createExistedUserRequest.setPassword("admin123");
        createExistedUserRequest.setConfirmPassword("admin123");

        CreateUserRequest createNewUserRequest = new CreateUserRequest();
        createNewUserRequest.setUsername("admin_test2");
        createNewUserRequest.setPassword("p@ssword123");
        createNewUserRequest.setConfirmPassword("p@ssword123");

        MvcResult mvcResultForExistedUser = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json.write(createExistedUserRequest).getJson())
        ).andReturn();

        Assert.assertEquals(200, mvcResultForExistedUser.getResponse().getStatus());

        MvcResult mvcResultForNewUser = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json.write(createNewUserRequest).getJson())
        ).andReturn();

        Assert.assertEquals(400, mvcResultForNewUser.getResponse().getStatus());
    }

    @Test
    public void createUserWithWeakPassword() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("admin_test3");
        createUserRequest.setPassword("admin");
        createUserRequest.setConfirmPassword("admin");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json.write(createUserRequest).getJson())
        ).andReturn();

        Assert.assertEquals(400, mvcResult.getResponse().getStatus());
    }

    @Test
    public void createUserWithWrongConfirmPassword() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("admin_test4");
        createUserRequest.setPassword("admin123");
        createUserRequest.setConfirmPassword("admin1234");

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json.write(createUserRequest).getJson())
        ).andReturn();

        Assert.assertEquals(400, mvcResult.getResponse().getStatus());
    }

    @Test
    public void findUserById() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("admin_test5");
        createUserRequest.setPassword("admin123");
        createUserRequest.setConfirmPassword("admin123");

        MvcResult createResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json.write(createUserRequest).getJson())
        ).andReturn();

        Assert.assertEquals(200, createResult.getResponse().getStatus());

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/api/user/id/1").header(HttpHeaders.AUTHORIZATION, createJWTToken(createUserRequest.getUsername()))
        ).andReturn();

        Assert.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void findUserByUsername() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("admin_test6");
        createUserRequest.setPassword("admin123");
        createUserRequest.setConfirmPassword("admin123");

        MvcResult createResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json.write(createUserRequest).getJson())
        ).andReturn();

        Assert.assertEquals(200, createResult.getResponse().getStatus());

        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/api/user/" + createUserRequest.getUsername()).header(HttpHeaders.AUTHORIZATION, createJWTToken(createUserRequest.getUsername()))
        ).andReturn();

        Assert.assertEquals(200, mvcResult.getResponse().getStatus());

        MvcResult mvcResultNotFoundUser = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/api/user/" + "someoneelse").header(HttpHeaders.AUTHORIZATION, createJWTToken(createUserRequest.getUsername()))
        ).andReturn();

        Assert.assertEquals(404, mvcResultNotFoundUser.getResponse().getStatus());
    }

    public String createJWTToken(String username) {
        String token = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .sign(HMAC512(SecurityConstants.SECRET.getBytes()));
        return "Bearer " + token;
    }
}

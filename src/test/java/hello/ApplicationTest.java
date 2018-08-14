package hello;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testHome() {
        ResponseEntity<String> entity = this.restTemplate.getForEntity("/helloWorld", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo("Hello World");
    }

    @Test
    public void testUsersList() {
        ResponseEntity<String> entity = this.restTemplate.getForEntity("/users/list", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).contains("John");
    }

    @Test
    public void testUsersGet() {
        ResponseEntity<String> entity = this.restTemplate.getForEntity("/users/get/5b71aeccd1fd0f68275a920f", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).contains("John");
    }

    @Test
    public void testUsersGetNotOk() {
        ResponseEntity<String> entity = this.restTemplate.getForEntity("/users/get/5b71aeccd1fd0f68275a920a", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testInsert() {
        HashMap requestBody = new HashMap();
        requestBody.put("firstName", "Thomas");
        requestBody.put("lastName", "Ersfeld");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json = null;
        try {
            json = new ObjectMapper().writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<String> e = this.restTemplate.postForEntity("/insert", entity, String.class);
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

}
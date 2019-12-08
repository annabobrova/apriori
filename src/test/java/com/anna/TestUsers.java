package com.anna;

import com.anna.util.Blogs;
import com.anna.util.Utils;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;

import java.util.List;

import static com.anna.util.Utils.isNotFound;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUsers {
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = Utils.baseURI;
    }

    @Test
    @DisplayName("Get all users")
    public void testGetUsers() {
        get("/users")
                .then()
                //verify response code
                .statusCode(200)
                .and()
                //verify that we have more than 0 objects
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Get a single user")
    public void testGetUseObject() {
        // get the first user id from the list of users
        String id = get("/users").then().extract().path("userId[0]");
        // verify that you can get an object for this user id
        get("/users/" + id)
                .then()
                // verify response code
                .statusCode(200)
                .and()
                .body("userId", equalTo("1"))
                .and()
                .body("createdAt", equalTo("2019-12-05T00:19:26.801Z"))
                .and()
                .body("name", equalTo("Brisa Jacobson"))
                .and()
                .body("email", equalTo("Bryon.Heidenreich48@example.com"))
                .and()
                .body("avatar", equalTo("https://s3.amazonaws.com/uifaces/faces/twitter/victorerixon/128.jpg"));
    }

    @Test
    @DisplayName("Add a new user")
    public void testPostUser() {
        String userBody = "{\n" +
                "\t\"name\": \"Dwight\",\n" +
                "    \"email\": \"aaa@example.net\",\n" +
                "    \"avatar\": \"https://s3.amazonaws.com/uifaces/faces/twitter/creartinc/128.jpg\"\n" +
                "}";

        // find out how many users there already
        int initialSize = Utils.countJsonObjects("/users");
        System.out.println(initialSize);

        // post a new user and verify that size is increased by one
        String newUserId = given()
                .contentType(ContentType.JSON)
                .body(userBody)
                .post("/users")
                .then()
                //verify response code
                .statusCode(201)
                .extract()
                .body()
                .path("userId");

        // find if you have one more user
        int newSize = Utils.countJsonObjects("/users");
        assertEquals(initialSize + 1, newSize);

        // get the new user and recheck its info
        get("/users/" + newUserId)
                .then()
                //verify response code
                .statusCode(200)
                .and()
                .body("userId", equalTo(newUserId))
                .and()
                .body("name", equalTo("Dwight"))
                .and()
                .body("email", equalTo("aaa@example.net"))
                .and()
                .body("avatar", equalTo("https://s3.amazonaws.com/uifaces/faces/twitter/creartinc/128.jpg"));
    }

    @Test
    @DisplayName("Change an existing user")
    public void testPutUser() {
        String name = "Dwight";
        String email = "aaaPut@example.net";
        String avatar = "https://s3.amazonaws.com/uifaces/faces/twitter/creartinc/128.jpg";
        String userBody = "{\n" +
                "\"name\":  \"" + name + "\",\n" +
                "\"email\":  \"" + email + "\",\n" +
                "\"avatar\": \"" + avatar + "\"\n" +
                "}";


        // find out how many users in the list
        int size = Utils.countJsonObjects("/users");

        // find the last user id
        String userId = get("/users").then().extract().path("userId[" + (size - 1) + "]");
        System.out.println(userId);

        // put a new value
        given()
                .contentType(ContentType.JSON)
                .body(userBody)
                .put("/users/" + userId)
                .then()
                // verify response code
                .statusCode(200)
                // verify that the user object was changed
                .body("userId", equalTo(userId))
                .and()
                .body("name", equalTo(name))
                .and()
                .body("email", equalTo(email))
                .and()
                .body("avatar", equalTo(avatar));

        // get the new user and recheck its info
        get("/users/" + userId)
                .then()
                // verify response code
                .statusCode(200)
                .and()
                .body("userId", equalTo(userId))
                .and()
                .body("name", equalTo(name))
                .and()
                .body("email", equalTo(email))
                .and()
                .body("avatar", equalTo(avatar));
    }

    @Test
    @DisplayName("Delete a user")
    public void testDelete() {
        // find out how many users in the list
        int size = Utils.countJsonObjects("/users");

        // find the last id
        String userId = get("/users").then().extract().path("userId[" + (size - 1) + "]");
        System.out.println(userId);

        // delete all blogs and likes for this user
        List<String> blogIds = Blogs.getBlogObjects(userId);

        //todo verify likes have been removed
        for(String blogId : blogIds)
            assertTrue(isNotFound("/users/" + userId + "/blogs/" + blogId));

        // delete the last user
        delete("/users/" + userId)
                .then()
                // verify the status code
                .statusCode(200);

        // find how many users in the list left
        int newSize = Utils.countJsonObjects("/users");
        assertEquals(size - 1, newSize);

        // run GET to verify the user not found
        assertTrue(isNotFound("/users/" + userId));
    }
}

package com.anna;

import com.anna.util.Blogs;
import com.anna.util.Likes;
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

public class TestBlogs {
    private static String userId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = Utils.baseURI;

        // get an id of the first user
        userId = get("/users").then().extract().path("userId[0]");
        System.out.println("id=" + userId);
    }

    @Test
    @DisplayName("Get all blogs")
    public void testGetBlogs() {
        get("/users/" + userId + "/blogs")
                .then()
                // verify response code
                .statusCode(200)
                .and()
                // verify that we have more than 0 blogs
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Get a single blog")
    public void testGetBlogObject() {
        // test is done for the blogId = 1. We assume it is always there
        String blogId = "1";
        get("/users/" + userId + "/blogs/" + blogId)
                .then()
                // verify response code
                .statusCode(200)
                .and()
                // verify that response has expected results;
                .body("userId", equalTo(userId))
                .and()
                .body("createdAt", equalTo("2019-12-05T09:50:16.937Z"))
                .and()
                .body("title", equalTo("system"))
                .and()
                .body("content", equalTo("Use the digital SSL transmitter, then you can back up the cross-platform interface!"))
                .and()
                .body("user.userId", equalTo(userId))
                .and()
                .body("user.createdAt", equalTo("2019-12-05T00:19:26.801Z"))
                .and()
                .body("user.name", equalTo("Brisa Jacobson"))
                .and()
                .body("user.email", equalTo("Bryon.Heidenreich48@example.com"))
                .and()
                .body("user.avatar", equalTo("https://s3.amazonaws.com/uifaces/faces/twitter/victorerixon/128.jpg"))
                .and()
                .body("likes.likeId[0]", equalTo("48"))
                .and()
                .body("likes.blogId[0]", equalTo("1"));
    }

    @Test
    @DisplayName("Add a new blog")
    public void postNewBlog() {
        String postTitle = "Postcapacitor";
        String content = "This is a post method test!!!";
        String blogBody = "{\n" +
                "    \"title\": \"" + postTitle + "\",\n" +
                "    \"content\": \"" + content + "\"\n" +
                "}";

        // find out how many blogs there already
        int initialSize = get("/users/" + userId + "/blogs").then().extract().path("list.size()");
        System.out.println(initialSize);

        // post a new blog. get blogId and verify that size is increased by one
        String blogId = given()
                .contentType(ContentType.JSON)
                .body(blogBody)
                .post("/users/" + userId + "/blogs")
                .then()
                //verify response code
                .statusCode(201)
                .extract()
                .body()
                .path("blogId");
        System.out.println("blogId=" + blogId);

        // find if you have one more blog
        int newSize = get("/users/" + userId + "/blogs").then().extract().path("list.size()");
        assertEquals(initialSize + 1, newSize);

        // if the size increased verify that a correct blog object was created
        get("/users/" + userId + "/blogs/" + blogId)
                .then()
                .body("userId", equalTo(userId))
                .and()
                //todo check createAt
                .body("title", equalTo(postTitle))
                .and()
                .body("content", equalTo(content))
                .and()
                .body("user.userId", equalTo(userId))
                .and()
                .body("likes", hasSize(0));

    }

    @Test
    @DisplayName(("Change an existing blog"))
    public void putBlog() {

        String postTitle = "Putcapacitor";
        String content = "This is a Put method test!!!";
        String blogBody = "{\n" +
                "    \"title\": \"" + postTitle + "\",\n" +
                "    \"content\": \"" + content + "\"\n" +
                "}";

        // find out how many objects in the array
        int size = get("/users/" + userId + "/blogs").then().extract().path("list.size()");

        // find the last id
        String blogId = get("/users/" + userId + "/blogs").then().extract().path("blogId[" + (size - 1) + "]");
        System.out.println("blogid=" + blogId);

        // put a new value
        given()
                .contentType(ContentType.JSON)
                .body(blogBody)
                .put("users/" + userId + "/blogs/" + blogId)
                .then()
                //verify response code
                .statusCode(200)
                //verify that the user object was changed
                .body("blogId", equalTo(blogId))
                .and()
                //I didn't check createdId
                .body("title", equalTo(postTitle))
                .and()
                .body("content", equalTo(content));

        // verify the blogs was changed
        get("/users/" + userId + "/blogs/" + blogId)
                .then()
                .body("userId", equalTo(userId))
                .and()
                //todo check createAt
                .body("title", equalTo(postTitle))
                .and()
                .body("content", equalTo(content))
                .and()
                .body("user.userId", equalTo(userId));
    }

    @Test
    @DisplayName("Delete a blog")
    public void deleteBlog() {
        String path = "/users/" + userId + "/blogs/";

        // find out how many objects there already
        int initialSize = Utils.countJsonObjects(path);
        System.out.println(initialSize);

        // get the last blog id
        String blogId = Blogs.getBlogId(path, initialSize - 1);

        List<String> likeIds = Likes.getLikeObjects(userId, blogId);

        // delete the blog itself and verify status code
        delete(path + blogId)
                .then()
                .statusCode(200);

        // verify likes have been removed
        for(String likeId : likeIds)
            assertTrue(isNotFound("/users/" + userId + "/blogs/" + blogId + "/" + likeId));

        // find out how many objects left after delete
        int newSize = Utils.countJsonObjects(path);

        // verify that blog was deleted
        assertEquals(initialSize - 1, newSize);
        System.out.println("The blog with blogId = " + blogId + " was successfully deleted");

        assertTrue(isNotFound(blogId));
    }

}

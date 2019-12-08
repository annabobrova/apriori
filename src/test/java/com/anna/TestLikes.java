package com.anna;

import com.anna.util.Likes;
import com.anna.util.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.anna.util.Utils.isNotFound;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLikes {

    private static String userId = "1";
    private static String blogId = "1";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = Utils.baseURI;

    }

    @Test
    @DisplayName("Get a list of likes")
    public void getArrayOfLikes() {
        String blogId = "1";
        get("/users/" + userId + "/blogs/" + blogId + "/likes")
                .then()
                //verify response code
                .statusCode(200)
                .and()
                //verify that we have more than 0 objects
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Get a like")
    public void getLikeObject() {
        // get the first like for userId=1, BlogId=1
        String likeId = Likes.getLikeObject(userId, blogId, 0);
        System.out.println("likeId=" + likeId);

        // get an array object
        get("/users/" + userId + "/blogs/" + blogId + "/likes/" + likeId)
                .then()
                // verify response code
                .statusCode(200)
                .body("likeId", equalTo(likeId))
                .and()
                .body("blogId", equalTo(blogId));
    }

    @Test
    @DisplayName("Post a new like")
    public void postNewLike() {
        String path = "/users/" + userId + "/blogs/" + blogId + "/likes";

        // find out how many objects there already
        int initialSizeOfArray = Utils.countJsonObjects(path);
        System.out.println(initialSizeOfArray);

        given()
                .contentType(ContentType.JSON)
                .post(path)
                .then()
                //verify response code
                .statusCode(201);

        // find how many objects after using Post method
        int newSizeOfArray = Utils.countJsonObjects(path);

        // verify that one more like was added to a blog with is = 1 for user (userId = 1)
        assertEquals(initialSizeOfArray + 1, newSizeOfArray);

    }

    @Test
    @DisplayName("Delete a like")
    public void deleteLike() {
        String path = "/users/" + userId + "/blogs/" + blogId + "/likes";

        // find out how many objects there already
        int initialSize = Utils.countJsonObjects(path);
        System.out.println(initialSize);

        // get the last like for userId=1, blogId=1
        String likeId = Likes.getLikeObject(userId, blogId, initialSize - 1);

        delete(path + "/" + likeId);

        // find out how many likes left after delete
        int newSize = Utils.countJsonObjects(path);

        // verify that like was deleted
        assertEquals(initialSize - 1, newSize);
        assertTrue(isNotFound(path + "/" + likeId));

    }

}

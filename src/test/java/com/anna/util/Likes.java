package com.anna.util;

import java.util.List;

import static io.restassured.RestAssured.*;

public class Likes {

    public static String getLikeObject(String blogId, String userId, int objectNumber) {
        return get("/users/" + userId + "/blogs/" + blogId + "/likes")
                .then()
                .extract().
                        path("likeId[" + objectNumber + "]");
    }

    public static List<String> getLikeObjects(String userId, String blogId) {
        return get("/users/" + userId + "/blogs/" + blogId + "/likes")
                .then()
                .extract()
                .response()
                .path("likeId");


    }

}

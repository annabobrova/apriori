package com.anna.util;

import java.util.List;

import static io.restassured.RestAssured.*;

public class Blogs {

    public static String getBlogId(String path, int arrayNumber) {
        return get(path)
                .then()
                .extract()
                .body()
                .path("blogId[" + arrayNumber + "]");
    }

    public static List<String> getBlogObjects(String userId) {
        return get("/users/" + userId + "/blogs")
                .then()
                .statusCode(200)
                .extract()
                .path("blogId");
    }

}

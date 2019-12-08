package com.anna.util;

import static io.restassured.RestAssured.get;

public class Utils {

    public final static String baseURI = "http://5de94d29cb3e3800141b905b.mockapi.io/api";

    public static int countJsonObjects(String path) {
        return get(path).then().extract().path("list.size()");
    }

    public static boolean isNotFound(String path) {
        return get(path).statusCode() == 404;
    }
}

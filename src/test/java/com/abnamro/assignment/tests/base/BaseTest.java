package com.abnamro.assignment.tests.base;

import com.abnamro.assignment.utils.ConfigReader;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public class BaseTest {

    protected String accessToken = ConfigReader.get("auth_token");
    protected String projectId = ConfigReader.get("project_id");

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = ConfigReader.get("base_url");
    }

}

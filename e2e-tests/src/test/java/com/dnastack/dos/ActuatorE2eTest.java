package com.dnastack.dos;

import io.restassured.RestAssured;
import org.junit.Assume;
import org.junit.Test;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assume.assumeFalse;

public class ActuatorE2eTest extends BaseE2eTest {

    @Test
    public void appNameAndVersionShouldBeExposed() {
        Assume.assumeFalse("Service info isn't set on local dev builds", RestAssured.baseURI.startsWith("http://localhost:"));
        //@formatter:off
        given()
            .log().method()
            .log().uri()
        .when()
            .get("/actuator/info")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("build.name", equalTo("Data Object Service"))
            .body("build.version", notNullValue());
        //@formatter:on
    }

    @Test
    public void sensitiveInfoShouldNotBeExposed() {
        //@formatter:off
        Stream.of("auditevents", "beans", "conditions", "configprops", "env", "flyway", "httptrace", "logfile", "loggers",
                "liquibase", "metrics", "mappings", "prometheus", "scheduledtasks", "sessions", "shutdown", "threaddump")
                //@formatter:off
                .forEach(endpoint -> {
                    given()
                        .log().method()
                        .log().uri()
                    .when()
                        .get("/actuator/" + endpoint)
                    .then()
                        .log().ifValidationFails()
                        .statusCode(anyOf(equalTo(401), equalTo(404)));
                    });
        //@formatter:on
    }

    @Test
    public void superAdminShouldNotHaveDefaultDevPassword() {
        Assume.assumeFalse("Dev credentials are allowed on localhost", RestAssured.baseURI.startsWith("http://localhost:"));
        //@formatter:off
        given()
            .log().method()
            .log().uri()
            .auth().basic("superadmin", "superadmin")
        .when()
            .get("/actuator/trace")
        .then()
            .log().ifValidationFails()
            .statusCode(equalTo(401));
        //@formatter:on
    }

}
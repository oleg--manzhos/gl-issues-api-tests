package com.abnamro.assignment.api;

import com.abnamro.assignment.dto.CreateIssueRequestDto;
import com.abnamro.assignment.dto.UpdateIssueRequestDto;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class IssuesApi {

    private final String CREATE_ISSUE_URI = "/projects/{project_id}/issues";
    private final String ISSUE_URI = "/projects/{project_id}/issues/{issue_iid}";
    private static final String CREATE_NOTE_URI = "/projects/{project_id}/issues/{issue_iid}/notes";


    private final String accessToken;
    private final String projectId;

    public IssuesApi(String accessToken, String projectId) {
        this.accessToken = accessToken;
        this.projectId = projectId;
    }

    public Response createIssue(CreateIssueRequestDto request) {
        return requestSpec()
                .body(request)
                .when()
                .post(CREATE_ISSUE_URI);
    }

    public Response getIssues() {
        return requestSpec()
                .when()
                .get(CREATE_ISSUE_URI);
    }

    public Response getIssues(Map<String, String> queryParams) {
        return requestSpec()
                .queryParams(queryParams)
                .when()
                .get(CREATE_ISSUE_URI);
    }

    public Response getIssueByIid(String iid) {
        return requestSpec()
                .pathParam("issue_iid", iid)
                .when()
                .get(ISSUE_URI);
    }

    public String getIidFromResponse(Response createIssueResponse) {
            return createIssueResponse.jsonPath().getString("iid");
    }

    public Response deleteIssueByIid(String iid) {
        return requestSpec()
                .pathParam("issue_iid", iid)
                .when()
                .delete(ISSUE_URI);
    }

    public Response updateIssueByIid(String iid, UpdateIssueRequestDto body) {
        return requestSpec()
                .pathParam("issue_iid", iid)
                .body(body)
                .when()
                .put(ISSUE_URI);
    }

    public Response updateIssueByIid(String iid, String body) {
        return requestSpec()
                .pathParam("issue_iid", iid)
                .body(body)
                .when()
                .put(ISSUE_URI);
    }

    private RequestSpecification requestSpec() {
        return given()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .pathParam("project_id", projectId)
                .log().all();
    }
}

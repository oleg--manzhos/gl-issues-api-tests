package com.abnamro.assignment.tests.issues_manipulations;

import com.abnamro.assignment.api.IssuesApi;
import com.abnamro.assignment.dto.CreateIssueRequestDto;
import com.abnamro.assignment.dto.UpdateIssueRequestDto;
import com.abnamro.assignment.test_data.IssueCreationData;
import com.abnamro.assignment.tests.base.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;


import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReadIssueTest extends BaseTest {

    private IssuesApi issuesApi;
    private String title;

    private CreateIssueRequestDto openedBackendIssue;
    private CreateIssueRequestDto closedFrontendIssue;
    private CreateIssueRequestDto confidentialIncidentIssue;

    private String openedBackendIid;
    private String closedFrontendIid;
    private String confidentialIncidentIid;

    private final List<String> createdIssueIids = new ArrayList<>();

    @BeforeAll
    void setUp() {
        issuesApi = new IssuesApi(accessToken, projectId);

        // Read functionality also requires testing of filtering.
        // Filter tests require a controlled dataset.
        // The configured project (the one on the GitLab, where issues are created) must be dedicated to automated testing,
        // because all existing issues are deleted before test data is created.
        Response response = issuesApi.getIssues();
        response.then().log().all();

        List<Integer> issueIids = response.jsonPath().getList("iid");

        if (issueIids.size() > 0) {
            for (Integer iid : issueIids) {
                issuesApi.deleteIssueByIid(iid.toString()).then().statusCode(204);
            }
        }

        // once all existing issues are removed, I can guarantee that the amount of
        // issues under test is controlled.
        // The idea was to create 3 issues that could cover all the READ/filter tests,
        // execute all the tests from this class on these 3 issues and delete them after the last test
        prepareReadTestData();
    }

    @Test
    @Tag("read")
    @DisplayName("Should display issues in Open state")
    void shouldFilterIssuesByOpenedState() {
        Map<String, String> filter = Map.of("state", "opened");

        issuesApi.getIssues(filter)
                .then()
                .statusCode(200)
                .body("iid", hasItems(Integer.parseInt(openedBackendIid),
                        Integer.parseInt(confidentialIncidentIid)))
                .body("iid", not(hasItem(Integer.parseInt(closedFrontendIid))));
    }

    @Test
    @Tag("read")
    @DisplayName("Should display issues in Closed status")
    void shouldFilterIssuesByClosedState() {
        Map<String, String> filter = Map.of("state", "closed");

        issuesApi.getIssues(filter)
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].iid", equalTo(Integer.parseInt(closedFrontendIid)))
                .body("[0].state", equalTo(filter.get("state")));
    }

    @Test
    @Tag("read")
    @DisplayName("Should filter issues by label: backend")
    void shouldFilterIssuesByBackendLabel() {
        Map<String, String>  filter = Map.of("labels", "backend");

        issuesApi.getIssues(filter).then()
                .log().all()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("iid", containsInAnyOrder(Integer.parseInt(openedBackendIid),
                                                Integer.parseInt(closedFrontendIid)))
                .body("labels", everyItem(hasItem("backend")));
    }

    @Test
    @Tag("read")
    @DisplayName("Should filter issues by label: backend, frontend")
    void shouldFilterIssuesByFrontendLabel() {
        Map<String, String> filter = Map.of("labels", "frontend,backend");

        issuesApi.getIssues(filter).then().statusCode(200).log().all()
                .body("size()", equalTo(1))
                .body("[0].iid", equalTo(Integer.parseInt(closedFrontendIid)))
                .body("[0].labels", containsInAnyOrder("frontend", "backend"));
    }

    @Test
    @Tag("read")
    @DisplayName("Search issue by unique attribute: title")
    void shouldSearchIssuesByUniqueText() {
        Map<String, String> filter = Map.of("search",  title);

        issuesApi.getIssues(filter)
                .then().log().all()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("title", everyItem(containsString(title)));
    }

    @Test
    @Tag("read")
    @DisplayName("Should filter issues by Confidential flag")
    void shouldFilterIssuesByConfidentialFlag() {
        Map<String, String> filter = Map.of("confidential", "true");

        issuesApi.getIssues(filter).then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].iid", equalTo(Integer.parseInt(confidentialIncidentIid)))
                .body("[0].confidential", equalTo(true));
    }

    @Test
    @Tag("read")
    @DisplayName("Should filter by Incident type")
    void shouldFilterIssuesByIncidentType() {
        Map<String, String> filter = Map.of("issue_type", "incident");

        issuesApi.getIssues(filter)
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].iid", equalTo(Integer.parseInt(confidentialIncidentIid)))
                .body("[0].issue_type",
                        equalTo("incident"));
    }

    @AfterAll
    void cleanUp() {
        for (String iid : createdIssueIids) {
            try {
                issuesApi.deleteIssueByIid(iid);
            } catch (Exception e) {
                System.out.println("Failed to delete test issue with iid: " + iid);
            }
        }
    }

    private void prepareReadTestData() {
        title = "autotest-" + UUID.randomUUID();

         // Issue 1 (status: opened, label: backend, type: issue
        openedBackendIssue = IssueCreationData.createIssue(title + "-issue-1", "Opened backend issue");

        Response openedResponse = issuesApi.createIssue(openedBackendIssue);
        openedResponse.then().statusCode(201);

        openedBackendIid = openedResponse.jsonPath().getString("iid");

        createdIssueIids.add(openedBackendIid);

        UpdateIssueRequestDto issueToUpdate1 = UpdateIssueRequestDto.builder()
                .labels("backend")
                .build();

        issuesApi.updateIssueByIid(openedBackendIid,issueToUpdate1).then().statusCode(200);

        //Issue 2 (status: closed, labels: [frontend, backend],
        closedFrontendIssue = IssueCreationData.createIssue(title + "-issue-2", "Closed frontend issue");

        Response closedResponse = issuesApi.createIssue(closedFrontendIssue);
        closedResponse.then().statusCode(201);

        closedFrontendIid = closedResponse.jsonPath().getString("iid");
        createdIssueIids.add(closedFrontendIid);

        UpdateIssueRequestDto issueToUpdate2 = UpdateIssueRequestDto.builder()
                .labels("frontend,backend")
                .stateEvent("close")
                .build();

        issuesApi.updateIssueByIid(closedFrontendIid,issueToUpdate2)
                .then()
                .statusCode(200)
                .body("state", equalTo("closed"));

        // Issue 3 (status: opened, confidential: true, type: incident
        confidentialIncidentIssue = IssueCreationData.createIssue(title + "-issue-3", "Confidential incident");

        Response confidentialResponse = issuesApi.createIssue(confidentialIncidentIssue);

        confidentialResponse.then().statusCode(201);

        confidentialIncidentIid = confidentialResponse.jsonPath().getString("iid");
        createdIssueIids.add(confidentialIncidentIid);

        UpdateIssueRequestDto issueToUpdate3 = UpdateIssueRequestDto.builder()
                .confidential(true)
                .issueType("incident")
                .build();

        issuesApi.updateIssueByIid(confidentialIncidentIid,issueToUpdate3)
                .then()
                .statusCode(200)
                .body("confidential", equalTo(true))
                .body("issue_type", equalTo("incident"));
    }
}
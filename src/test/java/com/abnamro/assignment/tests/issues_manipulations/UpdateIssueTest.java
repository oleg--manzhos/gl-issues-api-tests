package com.abnamro.assignment.tests.issues_manipulations;

import com.abnamro.assignment.api.IssuesApi;
import com.abnamro.assignment.dto.CreateIssueRequestDto;
import com.abnamro.assignment.dto.UpdateIssueRequestDto;
import com.abnamro.assignment.test_data.IssueCreationData;
import com.abnamro.assignment.tests.base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.abnamro.assignment.test_data.IssueCreationData.randomIssue;
import static com.abnamro.assignment.utils.DateUtil.selectDateFromNow;
import static org.hamcrest.Matchers.*;

public class UpdateIssueTest extends BaseTest {
    private IssuesApi issuesApi = new IssuesApi(accessToken, projectId);
    private String iid;

    @BeforeEach
    void createIssueForUpdate(){
        // populate random title and description for an issue
        CreateIssueRequestDto createIssueData = randomIssue(3, 5);

        //Create issue with generated title and description
        Response createIssueResponse = issuesApi.createIssue(createIssueData);
        createIssueResponse.then().log().all();
        createIssueResponse.then().statusCode(201);

        //get iid of created issue for deletion for the tear down
        iid = issuesApi.getIidFromResponse(createIssueResponse);
    }

    @Test
    @Tag("update")
    @DisplayName("Should update all mentioned fields in a created issue")
    void shouldUpdateAllIssueFieldsTest(){
        // This update implemented not like PUT, where you have to provide the complete JSON,
        // but like PATCH, where you need to provide a field that you need to change
        UpdateIssueRequestDto body = UpdateIssueRequestDto.builder()
                .title(IssueCreationData.randomDescription(3))
                .description(IssueCreationData.randomDescription(3))
                .startDate(selectDateFromNow(1))
                .dueDate(selectDateFromNow(5))
                .confidential(true)
                .discussionLocked(false)
                .issueType("incident")
                .labels("test")
                .assigneeIds(List.of(577706))
                .severity("high")
                .milestoneId(0)
                .build();

        // check update response attributes
        Response updatedIssue = issuesApi.updateIssueByIid(iid, body);
        updatedIssue.then().log().all();
        updatedIssue.then().statusCode(200)
                .body("title", equalTo(body.getTitle()),
                      "description", equalTo(body.getDescription()),
                      "start_date", equalTo(body.getStartDate()),
                      "due_date", equalTo(body.getDueDate()),
                      "confidential",  equalTo(body.getConfidential()),
                      "discussion_locked", equalTo(body.getDiscussionLocked()),
                      "issue_type", equalTo(body.getIssueType()));

        // get updated issue to validate the same attributes
        issuesApi.getIssueByIid(iid)
                .then()
                .statusCode(200)
                .body("title", equalTo(body.getTitle()),
                        "description", equalTo(body.getDescription()),
                        "start_date", equalTo(body.getStartDate()),
                        "due_date", equalTo(body.getDueDate()),
                        "confidential",  equalTo(body.getConfidential()),
                        "discussion_locked", equalTo(body.getDiscussionLocked()),
                        "issue_type", equalTo(body.getIssueType()));
    }

    @Test
    @Tag("update")
    @DisplayName("Should move created issue in 'open' state to 'closed' state")
    void shouldCloseCreatedIssueTest(){
        UpdateIssueRequestDto updateBody = UpdateIssueRequestDto.builder()
            .stateEvent("close")
            .build();

        Response updateResponse = issuesApi.updateIssueByIid(iid, updateBody);

        updateResponse.then().statusCode(200)
                .body("state", equalTo("closed"))
                .body("closed_at", notNullValue());

        // Verify persisted state
        issuesApi.getIssueByIid(iid)
                .then()
                .statusCode(200)
                .body("state", equalTo("closed"))
                .body("closed_at", notNullValue());
    }

    @Test
    @Tag("update")
    @DisplayName("Should reopen issue")
    void shouldReopenCreatedIssueTest(){
        UpdateIssueRequestDto updateBody = UpdateIssueRequestDto.builder()
                .stateEvent("close")
                .build();

        Response updateResponse = issuesApi.updateIssueByIid(iid, updateBody);

        updateResponse.then().statusCode(200)
                .body("state", equalTo("closed"))
                .body("closed_at", notNullValue());

        // Verify persisted state
        issuesApi.getIssueByIid(iid)
                .then()
                .log().all()
                .statusCode(200)
                .body("state", equalTo("closed"))
                .body("closed_at", notNullValue())
                .body("closed_by",  notNullValue());

        System.out.println("<<<<<<<-----------------Reopening issue----------------->>>>>>>>>");
        UpdateIssueRequestDto updateClosedBody = UpdateIssueRequestDto.builder()
                .stateEvent("reopen")
                .build();

        Response updateReopenResponse = issuesApi.updateIssueByIid(iid, updateClosedBody);

        updateReopenResponse.then()
                .log().all()
                .statusCode(200)
                .body("state", equalTo("opened"))
                .body("closed_at", nullValue())
                .body("closed_by", nullValue());
    }

    @Test
    @Tag("update")
    @DisplayName("Should assign create issue to a user (myself)")
    void assignToUserTest(){
        // an interesting observation here: I can assign the issue from my project to any existing user of GL,
        // for example 123456
        // that is why the id = 577706 is mine. But a reviewer (possible you) can execute this test successfully with high probability
        int assignee = 577706;
        UpdateIssueRequestDto body = UpdateIssueRequestDto.builder()
                .assigneeIds(List.of(assignee))
                .build();

        Response updatedIssue = issuesApi.updateIssueByIid(iid, body);
        updatedIssue.then().log().all();
        updatedIssue.then().statusCode(200).body("assignee.id", equalTo(assignee));
    }

    @AfterEach
    public void cleanUp() {
        //check that iid was populated
        if (iid != null) {
            //delete a created issue by iid
            issuesApi.deleteIssueByIid(iid);
        }
    }

}

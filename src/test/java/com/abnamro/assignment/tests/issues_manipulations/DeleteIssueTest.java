package com.abnamro.assignment.tests.issues_manipulations;

import com.abnamro.assignment.api.IssuesApi;
import com.abnamro.assignment.dto.CreateIssueRequestDto;
import com.abnamro.assignment.tests.base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static com.abnamro.assignment.test_data.IssueCreationData.randomIssue;
import static org.hamcrest.Matchers.equalTo;

public class DeleteIssueTest extends BaseTest {

    private IssuesApi issuesApi = new IssuesApi(accessToken, projectId);
    private String iid;
    public static final String ISSUE_IID_IS_INVALID = "issue_iid is invalid";
    private final String RESPONSE_404_MESSAGE = "404 Issue Not Found";

    @BeforeEach
    void createIssueForDeletion(){
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
    @Tag("delete")
    @DisplayName("Should delete n existing issue")
    void deleteExistingIssueTest(){
        // delete issue with existing iid
        issuesApi.deleteIssueByIid(iid);

        // check that issue is deleted
        Response getIssuesResponse = issuesApi.getIssueByIid(iid);
        getIssuesResponse.then().statusCode(404);
    }

    @Test
    @Tag("delete")
    @Tag("negative")
    @DisplayName("Should return an error for an unexisting issue")
    void testUnexistingIssueDeletion() {
        // delete unexisting issue
        Response getDeletedIssue = issuesApi.deleteIssueByIid("9999");
        getDeletedIssue.then().statusCode(404)
                .body("message", equalTo(RESPONSE_404_MESSAGE));
    }

    @Test
    @Tag("delete")
    @Tag("negative")
    @DisplayName("Should return and error for the malformed issue iid")
    void testIssueDeletionInvalidIid() {
        // delete issue with invalid iid
        Response getDeletedIssue = issuesApi.deleteIssueByIid("abc");
        getDeletedIssue.then().log().all();
        getDeletedIssue.then().statusCode(400)
                .body("error", equalTo(ISSUE_IID_IS_INVALID));
    }

    @Test
    @Tag("delete")
    @DisplayName("Should remove a removed issue without errors")
    void testIssueDeletionTwice() {
        // delete existing issue 1st time
        issuesApi.deleteIssueByIid(iid);
        Response getIssuesResponse = issuesApi.getIssueByIid(iid);
        getIssuesResponse.then().statusCode(404);

        // delete deleted issue again
        issuesApi.deleteIssueByIid(iid);
        Response getIssuesResponseAgain = issuesApi.getIssueByIid(iid);
        getIssuesResponseAgain.then().statusCode(404);
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

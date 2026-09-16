package com.abnamro.assignment.tests.issues_manipulations;

import com.abnamro.assignment.api.IssuesApi;
import com.abnamro.assignment.dto.CreateIssueRequestDto;
import com.abnamro.assignment.dto.UpdateIssueRequestDto;
import com.abnamro.assignment.tests.base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static com.abnamro.assignment.test_data.IssueCreationData.randomIssue;
import static org.hamcrest.Matchers.equalTo;

public class UpdateIssueNegativeTest extends BaseTest {
    public static final String CANT_BE_BLANK = "can't be blank";
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
    @Tag("negative")
    @DisplayName("Should prevent the title from update with an empty field")
    void updateToEmptyTitleTest(){
        UpdateIssueRequestDto body = UpdateIssueRequestDto.builder()
                .title("")
                .build();

        Response updatedIssue = issuesApi.updateIssueByIid(iid, body);
        updatedIssue.then().log().all();
        updatedIssue.then().statusCode(400).body("message.title[0]", equalTo(CANT_BE_BLANK));
    }

    @Test
    @Tag("update")
    @Tag("negative")
    @DisplayName("Should ignore the update fields, those are not intended for the update")
    void updateServiceFieldIsIgnoredTest(){
        // a try to update non-updatable field, like "iid". This should be ignored
        Response updatedIssue = issuesApi.updateIssueByIid(iid, "{\"iid\":9999, \"title\":\"123\"}");
        updatedIssue.then().log().all();
        updatedIssue.then().statusCode(200)
                .body("iid", equalTo(Integer.valueOf(iid)),
                        "title", equalTo("123"));
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

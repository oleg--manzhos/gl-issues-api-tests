package com.abnamro.assignment.tests.issues_manipulations;

import com.abnamro.assignment.api.IssuesApi;
import com.abnamro.assignment.dto.CreateIssueRequestDto;
import com.abnamro.assignment.test_data.IssueCreationData;
import com.abnamro.assignment.tests.base.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.abnamro.assignment.test_data.IssueCreationData.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class CreateIssueTest extends BaseTest {

    private IssuesApi issuesApi = new IssuesApi(accessToken, projectId);
    private String iid;

    @Test
    @Tag("create")
    @DisplayName("Should create a valid issue")
    void testIssueCreation() {
        // populate random title and description for an issue
        CreateIssueRequestDto createIssueData = randomIssue(3, 2);

        //Create issue with generated title and description
        Response createIssueResponse = issuesApi.createIssue(createIssueData);
        createIssueResponse.then().log().all();

        createIssueResponse.then()
                .statusCode(201)
                // since lots of fields are dynamic and will be different for different reviewers or
                // users, I did schema validation here not to miss them or their rules. Once something will be changed
                // in the response body, this schema will make this check fail
                .body(matchesJsonSchemaInClasspath("schemas/create_issue_schema.json"))
                //these fields will be the same, do makes sense to check them explicitly
                .body("title", equalTo(createIssueData.getTitle()))
                .body("description", equalTo(createIssueData.getDescription()))
                .body("state", equalTo("opened"))
                .body("type", equalTo("ISSUE"));

        //get iid of created issue for deletion
        iid = issuesApi.getIidFromResponse(createIssueResponse);

        Response getIssuesResponse = issuesApi.getIssueByIid(iid);
        // get the issue by iid and check its attributes
        getIssuesResponse.then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/create_issue_schema.json"))
                //these fields will be the same, do makes sense to check them explicitly
                .body("title", equalTo(createIssueData.getTitle()))
                .body("description", equalTo(createIssueData.getDescription()))
                .body("state", equalTo("opened"))
                .body("type", equalTo("ISSUE"));
    }

    @Test
    @Tag("create")
    @DisplayName("Should create a valid task")
    void testTaskCreation() {
        // populate random title and description for an issue
        CreateIssueRequestDto createIssueData = createRandomTask(3, 4);

        //Create issue with generated title and description
        Response createTaskResponse = issuesApi.createIssue(createIssueData);
        createTaskResponse.then().log().all();

        createTaskResponse.then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/create_issue_schema.json"))
                .body("title", equalTo(createIssueData.getTitle()))
                .body("description", equalTo(createIssueData.getDescription()))
                .body("state", equalTo("opened"))
                .body("type", equalTo("TASK"));

        //get iid of created issue for deletion
        iid = issuesApi.getIidFromResponse(createTaskResponse);

        Response getIssuesResponse = issuesApi.getIssueByIid(iid);
        // get the issue by iid and check its attributes
        getIssuesResponse.then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/create_issue_schema.json"))
                //these fields will be the same, do makes sense to check them explicitly
                .body("title", equalTo(createIssueData.getTitle()))
                .body("description", equalTo(createIssueData.getDescription()))
                .body("state", equalTo("opened"))
                .body("type", equalTo("TASK"));
    }

    //invalid and boundary values source
    static Stream<Arguments> invalidIssueData() {
        return Stream.of(
                Arguments.of(
                        "missing title",
                        null,
                        IssueCreationData.randomDescription(3)
                ),
                Arguments.of(
                        "empty title",
                        "",
                        IssueCreationData.randomDescription(2)
                ),
                Arguments.of(
                        "blank title",
                        "   ",
                        IssueCreationData.randomDescription(1)
                ),
                // exploratory testing revealed that the title has no documented max length
                Arguments.of(
                        "long title",
                        IssueCreationData.randomDescription(50000),
                        IssueCreationData.randomDescription(30000)
                )
        );
    }

    @Tag("create")
    @ParameterizedTest(name = "{0}")
    @DisplayName("Should test all the values, lead to code 400 as test values are intentionally invalid")
    @MethodSource("invalidIssueData")
    void shouldReturn400ForInvalidIssue(
            String caseName,
            String title,
            String description) {

        CreateIssueRequestDto request = createIssue(title, description);
        issuesApi.createIssue(request).then().statusCode(400);
    }

    @Test
    @Tag("create")
    @DisplayName("Should create a valid issue with emoji")
    void testIssueCreationWithEmoji() {
        // populate random title and description for an issue with emoji. Emoji usually requires another DB encoding
        CreateIssueRequestDto createIssueData = createIssue("🥸🚥", "🏁⛸️");

        //Create issue with generated title and description
        Response createIssueResponse = issuesApi.createIssue(createIssueData);
        createIssueResponse.then().log().all();

        //get iid of created issue for deletion
        iid = issuesApi.getIidFromResponse(createIssueResponse);

        issuesApi.getIssueByIid(iid).then().statusCode(200)
                .body("title", equalTo(createIssueData.getTitle()),
                        "description", equalTo(createIssueData.getDescription()));
    }

    @Test
    @Tag("create")
    @Tag("negative")
    @DisplayName("Shouldn't create an issue with invalid issue_type")
    void testIssueCreationWithInvalidIssueType() {
        // populate random title and description for an issue
        CreateIssueRequestDto createIssueData = createIssueEntity("Valid Title", "Valid Description", "report");

        //Create issue with generated title and description
        Response createTaskResponse = issuesApi.createIssue(createIssueData);
        createTaskResponse.then().log().all().statusCode(400)
                .body("error", equalTo( "issue_type does not have a valid value"));

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

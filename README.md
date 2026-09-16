# Test Assignment. Solution Overview

## GitLab Issues API Test Automation

Automated API test suite for the GitLab Issues API, implemented as part of the backend QA automation assignment.

The solution focuses not only on endpoint-level CRUD verification, but also  on the main business workflows exposed by GitLab for Issue, Task and Incident
entities.

## Test Strategy and Scope

The GitLab Issues API provides a broad set of endpoints, parameters, and  configuration options. Since exhaustive coverage of the entire API was outside
the scope of this assignment, the test scope was prioritised based on actual business usage and the main user-facing workflows.

The API documentation was used as the source of truth for request/response contracts and supported operations. The official GitLab UI was additionally
reviewed to understand how these API capabilities are used in practice and which workflows represent the primary business use cases.

Based on this approach, the automated tests focus on the lifecycle of the  issue types exposed through the GitLab UI: Issue, Task, and Incident.
Implemented tests include their creation, retrieval, update, and deletion, as well as selected  filtering, validation, and negative scenarios.

API capabilities that are not involved in these primary business workflows  were considered lower priority and were not included in the current test scope.

### Covered Functionality

CRUD lifecycle for:
- Issue (CRUD)
- Task (UD)
- Incident (UD)

The same Issues API endpoints and largely the same data model are used for these entity types.
Therefore, the default `issue` type receives the most extensive CRUD, validation, filtering, and negative coverage.

Task and Incident are additionally covered where their behaviour or lifecycle is relevant, while avoiding unnecessary duplication of tests that exercise the same API contract.

### Additional Coverage

The suite also covers:

- filtering by selected supported parameters
- request validation
- invalid and non-existing issue identifiers
- invalid request payloads
- selected boundary and edge cases
- state transitions
- response structure/schema validation
- resource deletion and verification of the resulting state


### Test Data and Cleanup

- tests create their own data
- unique/random values are used where appropriate
- created resources are cleaned up

## Test Design

Tests are designed to be independent where possible and create the data required for their scenarios.
Random or unique values are used for test data where uniqueness is relevant. Deterministic values are preferred for negative and boundary tests where the tested condition should remain explicit and reproducible.

For scenarios such as filtering, a controlled dataset is prepared before execution so that assertions can verify the complete result rather than relying on pre-existing project data.

Both HTTP status codes and relevant response data are verified. For CRUD operations, the resulting resource state is verified where appropriate instead of relying only on the response of the modifying request.

## Solution Tools & Technology Stack

- Java 17 (Amazon Corretto JDK)
- Maven 3.6+ (3.9.12)
- Junit 5.10.0
- REST Assured 6.0.1
- Jackson
- Lombok
- DataFaker
- JSON Schema Validator
- GitHub Actions

The project was developed using IntelliJ IDEA.

## Prerequisites and Configuration

To execute the tests, the following values are required:

- GitLab project ID
- valid GitLab OAuth2 access token with `api` scope (checkbox)

Configuration values can be provided either through `resources\config.properties` for local execution or as Java system properties.

Example:

```bash
mvn clean test \
  -Dproject_id=<PROJECT_ID> \
  -Dauth_token=<ACCESS_TOKEN>
```

Sensitive authentication data should not be committed to the repository.

## How to Obtain a GitLab OAuth2 Access Token

The tests use GitLab OAuth 2.0 authentication. To run them, you need a valid OAuth2 access_token with the api scope.

### Create an OAuth Application

In GitLab, go to: Avatar - Preferences - Access - Applications - Add new application (Enable the `api` scope below)
Configure a redirect URI, for example: http://localhost:8080/ (this shouldn't be a real server, but the path should be absolute).

The Application ID is your `CLIENT_ID`, the Secret is your `CLIENT_SECRET`. Please save these data, you won't see it later.

Open the following URL in your browser (replace `CLIENT_ID` and `redirect_uri` with the values obtained after application creation)
```
https://gitlab.com/oauth/authorize?client_id=<CLIENT_ID>&redirect_uri=<redirect_uri>&response_type=code&scope=api
```
Authorize your application. After that you will get the link like:
```aiignore
http://localhost:8080/?code=4b819dee21ccd7e2628176473773dadbe505bfab01f80b4b22934d11909b5fc4
```
Get the code part after the '=' sign.

There is a Postman collection with POST HTTP Request. Provide your values against given fields: `client_id`, `client_secret`, `code` and `redirect_uri`.
The value of `grant_type` is predefined and should be `authorization_code`.

Once the response is returned, you will need a value of  `access_token` (like `3755efb91cb96c611427195cc589ded203966ef553b8b5a292e97a9a4ed37ae2`).
Copy and paste it as the value of `auth_token` key in `resources\config.properties`

## Running the Tests

### Local execution

Compile the project and run the tests, from Maven: 

```bash
mvn clean test
```
Configuration can also be executed from the command line:

```bash
mvn clean test \
  -Dproject_id=<PROJECT_ID> \
  -Dauth_token=<ACCESS_TOKEN>
```

### Running Selected Test Groups

Tests are classified using JUnit 5 tags.

The currently used categories include:

```text
create
read
update
delete
positive
negative
```

For example:
```bash
mvn test -Dgroups="read"
```
or:
```bash
mvn test -Dgroups="read & negative"
```
Tests can also be excluded:
```bash
mvn test -DexcludedGroups="delete,negative"
```
This tagging strategy is also used by GitHub Actions to allow selective execution.

### GitHub Actions

The repository contains a manually triggered GitHub Actions workflow.

The workflow allows the user to provide the GitLab project ID and authentication information and select which categories of tests should be executed.
By default, all test categories are enabled:

```text
Create
Read
Update
Delete
Positive
Negative
```
Individual categories can be disabled before starting the workflow. Disabled categories are passed to Maven as excluded JUnit tags.
This allows, for example, running only non-destructive scenarios or excluding negative tests without modifying the test code.

## Report the execution results
### Test Reports

JUnit 5 test execution is handled by Maven Surefire.
To get the test execution report, run Maven command: 

```
mvn clean test
```
Run
```
mvn surefire-report:report-only && open target/reports/surefire.html
```
and enjoy HTML-report.

The reports contain information about executed, passed, failed, and skipped tests together with failure details.

The same test results are available as part of GitHub Actions execution logs when the suite is executed in CI.


## Assumptions and Limitations

The solution intentionally focuses on the primary Issues API workflows rather than exhaustive coverage of every GitLab Issues API parameter.

Task and Incident use the same underlying Issues API endpoints as Issue. Repeating identical contract tests for every type was avoided where this would not provide additional coverage.

GitLab also supports parent-child relationships between work items, for example attaching a Task to an Issue. These relationships belong to the broader GitLab Work Items model and were considered outside the core CRUD scope of this assignment.

The tests are designed for execution against a dedicated GitLab test project.

## Contact details
Any questions? Email: oleg.manzhos@gmail.com


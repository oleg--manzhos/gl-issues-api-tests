package com.abnamro.assignment.test_data;

import com.abnamro.assignment.dto.CreateIssueRequestDto;
import net.datafaker.Faker;

public class IssueCreationData {

    private static final Faker faker = new Faker();

    public static CreateIssueRequestDto randomIssue(int wordCount, int sentenceCount) {
        return CreateIssueRequestDto.builder()
                .title(faker.lorem().sentence(wordCount))
                .description(faker.lorem().paragraph(sentenceCount))
                .build();
    }

    public static String randomDescription(int sentenceCount) {
        return faker.lorem().paragraph(sentenceCount);
    }

    public static CreateIssueRequestDto createIssue(String title, String description) {
        return CreateIssueRequestDto.builder()
                .title(title)
                .description(description)
                .build();
    }

    public static CreateIssueRequestDto createIssueEntity(String title, String description, String type) {
        return CreateIssueRequestDto.builder()
                .title(title)
                .description(description)
                .issueType(type)
                .build();
    }

    public static CreateIssueRequestDto createRandomTask(int wordCountT, int wordCountD) {
        return CreateIssueRequestDto.builder()
                .title(faker.lorem().sentence(wordCountT))
                .description(faker.lorem().sentence(wordCountD))
                .issueType("task")
                .build();
    }
}

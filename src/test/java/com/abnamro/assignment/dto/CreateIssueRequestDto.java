package com.abnamro.assignment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateIssueRequestDto {
    @JsonProperty("issue_type")
    private String issueType;
    private String title;
    private String description;
}

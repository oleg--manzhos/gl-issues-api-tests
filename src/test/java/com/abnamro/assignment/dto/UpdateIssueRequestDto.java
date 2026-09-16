package com.abnamro.assignment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateIssueRequestDto {
//https://docs.gitlab.com/api/issues/#update-an-issue
    private String title;
    private String description;
    private Boolean confidential;

    @JsonProperty("discussion_locked")
    private Boolean discussionLocked;

    @JsonProperty("due_date")
    private String dueDate;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("issue_type")
    private String issueType;

    private String labels;

    @JsonProperty("add_labels")
    private String addLabels;

    @JsonProperty("remove_labels")
    private String removeLabels;

    @JsonProperty("assignee_ids")
    private List<Integer> assigneeIds;

    @JsonProperty("milestone_id")
    private Integer milestoneId;

    private String milestone;
    private String severity;

    @JsonProperty("state_event")
    private String stateEvent;

    private Integer weight;
}



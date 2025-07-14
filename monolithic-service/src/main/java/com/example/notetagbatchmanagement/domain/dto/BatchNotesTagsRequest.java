package com.example.notetagbatchmanagement.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchNotesTagsRequest {
    private List<Long> noteIds;
    private List<Long> tagIds;
}

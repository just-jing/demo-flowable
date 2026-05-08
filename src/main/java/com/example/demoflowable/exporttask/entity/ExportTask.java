package com.example.demoflowable.exporttask.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTask {

    private String id;
    private String taskName;
    private String creator;
    private Long creatorId;
    private String status;
    private String handlerType;
    private String exportParams;
    private String exportFile;
    private Long fileSize;
    private String errorMessage;
    private Integer version;
    private Date createdTime;
    private Date startTime;
    private Date completedTime;
}

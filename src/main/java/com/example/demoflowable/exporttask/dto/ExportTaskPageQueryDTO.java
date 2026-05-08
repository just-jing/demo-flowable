package com.example.demoflowable.exporttask.dto;

import lombok.Data;

@Data
public class ExportTaskPageQueryDTO {

    private int page = 1;
    private int size = 10;
    private String status;
}

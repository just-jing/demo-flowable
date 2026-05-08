package com.example.demoflowable.exporttask.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ExportTaskSubmitDTO {

    @NotEmpty(message = "任务名称不能为空")
    private String taskName;

    @NotEmpty(message = "处理器类型不能为空")
    private String handlerType;

    private String exportParams;
}

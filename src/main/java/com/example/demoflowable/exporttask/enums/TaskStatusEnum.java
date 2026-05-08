package com.example.demoflowable.exporttask.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {

    PENDING("PENDING", "待执行"),
    RUNNING("RUNNING", "执行中"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "失败"),
    CANCELED("CANCELED", "已取消");

    private final String code;
    private final String desc;
}

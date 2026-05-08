package com.example.demoflowable.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    TASK_NOT_FOUND(1001, "导出任务不存在"),
    TASK_CANNOT_CANCEL(1002, "当前状态不可取消，仅待执行状态的任务可取消"),
    TASK_DOWNLOAD_FAILED(1003, "导出文件不存在或已丢失"),
    HANDLER_NOT_FOUND(1004, "未找到对应的导出处理器: %s"),
    INVALID_PARAMS(1005, "请求参数错误");

    private final Integer code;
    private final String message;
}

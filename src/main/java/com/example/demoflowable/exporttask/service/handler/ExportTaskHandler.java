package com.example.demoflowable.exporttask.service.handler;

import com.example.demoflowable.exporttask.entity.ExportTask;

/**
 * 导出任务处理器接口
 * 其他模块实现此接口以提供具体的导出逻辑
 */
public interface ExportTaskHandler {

    /**
     * 返回处理器类型标识，与 ExportTask.handlerType 对应
     */
    String getType();

    /**
     * 执行导出任务
     *
     * @param task 导出任务实体
     * @return 生成的文件名
     * @throws Exception 执行过程中抛出的异常
     */
    String execute(ExportTask task) throws Exception;
}

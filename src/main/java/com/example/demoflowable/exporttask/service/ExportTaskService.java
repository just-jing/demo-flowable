package com.example.demoflowable.exporttask.service;

import com.example.demoflowable.exporttask.dto.ExportTaskPageQueryDTO;
import com.example.demoflowable.exporttask.dto.ExportTaskSubmitDTO;
import com.example.demoflowable.exporttask.dto.PageResult;
import com.example.demoflowable.exporttask.entity.ExportTask;
import com.example.demoflowable.exporttask.vo.ExportTaskVO;

public interface ExportTaskService {

    /**
     * 提交导出任务（异步执行）
     */
    String submit(ExportTaskSubmitDTO dto);

    /**
     * 分页查询导出任务列表
     */
    PageResult<ExportTaskVO> pageQuery(ExportTaskPageQueryDTO dto);

    /**
     * 取消导出任务（仅 PENDING 状态可取消）
     */
    void cancel(String id);

    /**
     * 根据 ID 获取导出任务
     */
    ExportTask getById(String id);

    /**
     * 更新导出任务（内部使用）
     */
    void updateById(ExportTask task);
}

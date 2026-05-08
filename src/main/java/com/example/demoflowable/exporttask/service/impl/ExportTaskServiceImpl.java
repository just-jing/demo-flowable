package com.example.demoflowable.exporttask.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.demoflowable.configs.AuthenticationFilter;
import com.example.demoflowable.exception.BizException;
import com.example.demoflowable.exception.ErrorCode;
import com.example.demoflowable.exporttask.dto.ExportTaskPageQueryDTO;
import com.example.demoflowable.exporttask.dto.ExportTaskSubmitDTO;
import com.example.demoflowable.exporttask.dto.PageResult;
import com.example.demoflowable.exporttask.entity.ExportTask;
import com.example.demoflowable.exporttask.enums.TaskStatusEnum;
import com.example.demoflowable.exporttask.mapper.ExportTaskMapper;
import com.example.demoflowable.exporttask.service.ExportTaskService;
import com.example.demoflowable.exporttask.service.handler.ExportTaskHandler;
import com.example.demoflowable.exporttask.vo.ExportTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExportTaskServiceImpl implements ExportTaskService {

    @Resource
    private ExportTaskMapper exportTaskMapper;

    @Resource(name = "exportTaskExecutor")
    private Executor exportTaskExecutor;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private List<ExportTaskHandler> handlers;

    @Value("${export.task.file-dir}")
    private String exportFileDir;

    @Value("${export.task.max-concurrent:3}")
    private int maxConcurrent;

    @Value("${export.task.timeout-seconds:300}")
    private int timeoutSeconds;

    private Map<String, ExportTaskHandler> handlerMap;
    private Semaphore semaphore;

    @PostConstruct
    public void init() {
        // 初始化处理器映射
        handlerMap = handlers.stream()
                .collect(Collectors.toMap(ExportTaskHandler::getType, Function.identity()));
        // 确保导出目录存在
        File dir = new File(exportFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.semaphore = new Semaphore(maxConcurrent);
        log.info("导出目录: {}", dir.getAbsolutePath());
        log.info("导出任务最大并发数: {}", maxConcurrent);
    }

    @Override
    public String submit(ExportTaskSubmitDTO dto) {
        // 校验处理器是否存在
        if (!handlerMap.containsKey(dto.getHandlerType())) {
            throw new BizException(ErrorCode.HANDLER_NOT_FOUND, dto.getHandlerType());
        }

        ExportTask task = ExportTask.builder()
                .id(IdUtil.fastSimpleUUID())
                .taskName(dto.getTaskName())
                .creator(Optional.ofNullable(AuthenticationFilter.getLoginUser())
                        .map(u -> u.getUsername())
                        .orElse("未知"))
                .creatorId(Optional.ofNullable(AuthenticationFilter.getLoginUserId())
                        .orElse(0L))
                .status(TaskStatusEnum.PENDING.getCode())
                .handlerType(dto.getHandlerType())
                .exportParams(dto.getExportParams())
                .createdTime(new Date())
                .build();

        exportTaskMapper.insert(task);

        return task.getId();
    }

    @Scheduled(fixedDelayString = "${export.task.poll-interval:5000}")
    public void pollPendingTasks() {
        // 超时检查：将执行中超时的任务标记为失败
        if (timeoutSeconds > 0) {
            Date cutoff = DateUtil.offsetSecond(new Date(), -timeoutSeconds);
            int timedOut = exportTaskMapper.timeoutRunningTasks(cutoff, "执行超时", new Date());
            if (timedOut > 0) {
                log.warn("任务执行超时: {} 个", timedOut);
            }
        }

        int available = semaphore.availablePermits();
        if (available <= 0) {
            return;
        }

        List<ExportTask> pendingTasks = exportTaskMapper.selectPendingTopN(available);
        if (pendingTasks.isEmpty()) {
            return;
        }

        log.info("调度导出任务: {} 个", pendingTasks.size());
        for (ExportTask task : pendingTasks) {
            if (!semaphore.tryAcquire()) {
                break;
            }
            String taskId = task.getId();
            exportTaskExecutor.execute(() -> {
                try {
                    executeTask(taskId);
                } finally {
                    semaphore.release();
                }
            });
        }
    }

    /**
     * 异步执行导出任务
     */
    private void executeTask(String taskId) {
        transactionTemplate.execute((TransactionStatus status) -> {
            // 带锁查询，校验任务状态
            ExportTask task = exportTaskMapper.selectByIdForUpdate(taskId);
            if (task == null) {
                log.warn("任务不存在: {}", taskId);
                return null;
            }
            if (!TaskStatusEnum.PENDING.getCode().equals(task.getStatus())) {
                log.warn("任务状态不是待执行，跳过: {} status={}", taskId, task.getStatus());
                return null;
            }

            // 更新为执行中（乐观锁 version 校验）
            task.setStatus(TaskStatusEnum.RUNNING.getCode());
            task.setStartTime(new Date());
            int affected = exportTaskMapper.updateById(task);
            if (affected == 0) {
                log.warn("乐观锁冲突，任务已被其他线程处理: {}", taskId);
            }
            return null;
        });

        // 执行导出
        ExportTask task = exportTaskMapper.selectById(taskId);
        if (task == null || !TaskStatusEnum.RUNNING.getCode().equals(task.getStatus())) {
            log.warn("任务状态不是执行中，跳过执行: {}", taskId);
            return;
        }

        try {
            ExportTaskHandler handler = handlerMap.get(task.getHandlerType());
            if (handler == null) {
                throw new BizException(ErrorCode.HANDLER_NOT_FOUND, task.getHandlerType());
            }

            String fileName = handler.execute(task);

            // 重新查询，确认任务未被标记为超时或取消
            ExportTask current = exportTaskMapper.selectById(taskId);
            if (!TaskStatusEnum.RUNNING.getCode().equals(current.getStatus())) {
                log.warn("任务状态已变更，跳过完成更新: {} status={}", taskId, current.getStatus());
                return;
            }

            // 乐观锁方式更新为完成
            task.setStatus(TaskStatusEnum.COMPLETED.getCode());
            task.setExportFile(fileName);
            File file = new File(exportFileDir, fileName);
            if (file.exists()) {
                task.setFileSize(file.length());
            }
            task.setCompletedTime(new Date());
            task.setVersion(current.getVersion());
            int affected = exportTaskMapper.updateById(task);
            if (affected == 0) {
                log.warn("乐观锁冲突，任务完成状态更新失败（可能已被超时或取消）: {}", taskId);
                return;
            }
            log.info("导出任务完成: {} file={}", taskId, fileName);
        } catch (Exception e) {
            log.error("导出任务执行失败: {}", taskId, e);
            ExportTask current = exportTaskMapper.selectById(taskId);
            task.setStatus(TaskStatusEnum.FAILED.getCode());
            task.setErrorMessage(StrUtil.maxLength(e.getMessage(), 500));
            task.setCompletedTime(new Date());
            task.setVersion(current.getVersion());
            int affected = exportTaskMapper.updateById(task);
            if (affected == 0) {
                log.warn("乐观锁冲突，任务失败状态无法更新（可能已被处理）: {}", taskId);
            }
        }
    }

    @Override
    public PageResult<ExportTaskVO> pageQuery(ExportTaskPageQueryDTO dto) {
        String status = StrUtil.emptyToNull(dto.getStatus());
        int offset = (dto.getPage() - 1) * dto.getSize();

        long total = exportTaskMapper.count(status);
        List<ExportTask> list = exportTaskMapper.selectPage(status, offset, dto.getSize());

        List<ExportTaskVO> records = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(total, dto.getPage(), dto.getSize(), records);
    }

    @Override
    public void cancel(String id) {
        ExportTask task = exportTaskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        if (!TaskStatusEnum.PENDING.getCode().equals(task.getStatus())) {
            throw new BizException(ErrorCode.TASK_CANNOT_CANCEL);
        }
        task.setStatus(TaskStatusEnum.CANCELED.getCode());
        task.setCompletedTime(new Date());
        int affected = exportTaskMapper.updateById(task);
        if (affected == 0) {
            throw new BizException(ErrorCode.TASK_CANNOT_CANCEL);
        }
    }

    @Override
    public ExportTask getById(String id) {
        ExportTask task = exportTaskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    @Override
    public void updateById(ExportTask task) {
        exportTaskMapper.updateById(task);
    }

    private ExportTaskVO convertToVO(ExportTask task) {
        return ExportTaskVO.builder()
                .id(task.getId())
                .taskName(task.getTaskName())
                .creator(task.getCreator())
                .status(task.getStatus())
                .statusDesc(TaskStatusEnum.valueOf(task.getStatus()).getDesc())
                .exportFile(task.getExportFile())
                .fileSize(task.getFileSize())
                .errorMessage(task.getErrorMessage())
                .createdTime(task.getCreatedTime())
                .completedTime(task.getCompletedTime())
                .build();
    }
}

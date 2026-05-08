package com.example.demoflowable.listeners;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 监听 {@link Task} 的开始与完成
 *
 * @author jason
 */
@Slf4j
@Component
public class BpmTaskEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    @Lazy // 解决循环依赖
    private HistoryService historyService;

    @Resource
    @Lazy // 解决循环依赖
    private TaskService taskService;

    public static final Set<FlowableEngineEventType> TASK_EVENTS = ImmutableSet.<FlowableEngineEventType>builder()
            .add(FlowableEngineEventType.TASK_CREATED)
            .add(FlowableEngineEventType.TASK_ASSIGNED)
            .add(FlowableEngineEventType.TASK_COMPLETED)
            .add(FlowableEngineEventType.ACTIVITY_CANCELLED)
            .add(FlowableEngineEventType.TIMER_FIRED) // 监听审批超时
            .build();

    public BpmTaskEventListener() {
        super(TASK_EVENTS);
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        log.info("taskCreated: {} - {}", task.getName(), task.getId());
    }

    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        log.info("taskCompleted: {} - {}", task.getName(), task.getId());
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        log.info("taskAssigned: {} - {}", task.getName(), task.getId());
    }

    @Override
    protected void activityCancelled(FlowableActivityCancelledEvent event) {
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().executionId(event.getExecutionId()).list();
        activities.forEach(activity -> {
            Task task = getTask(activity.getActivityId());
            if (Objects.isNull(task)) {
                return;
            }
            log.info("taskCancelled: {} - {}", task.getName(), task.getId());
        });

    }

    @Override
    protected void timerFired(FlowableEngineEntityEvent event) {
        log.info("timerFired: {}", event.getScopeDefinitionId());
    }

    public Task getTask(String id) {
        return taskService.createTaskQuery().taskId(id).includeTaskLocalVariables().singleResult();
    }

}

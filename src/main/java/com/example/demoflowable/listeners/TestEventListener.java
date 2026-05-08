package com.example.demoflowable.listeners;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestEventListener implements TaskListener, JavaDelegate {
    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("{} {} {}", delegateTask.getId(), delegateTask.getName(), delegateTask.getEventName());
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("{} {}", execution.getId(), execution.getEventName());
    }
}

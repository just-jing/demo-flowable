package com.example.demoflowable.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.example.demoflowable.constance.FlowableConstance;
import com.example.demoflowable.entity.vo.TaskRespVO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class TestController {

    @Resource
    TaskService taskService;

    @Resource
    RuntimeService runtimeService;

    @GetMapping("/startProcess")
    public ResponseEntity<String> startProcess() {
        log.info("开始流程");
        Map<String, Object> map = MapUtil.builder(new HashMap<String,Object>())
                .put("p001_assignees", Collections.singleton("1000"))
                .put("p002_assignee", "1000")
                .build();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(FlowableConstance.FLOW_ABLE_TEST_PROCESS_ID, map);
        log.info("流程id: {}", processInstance.getProcessInstanceId());
        return ResponseEntity.ok().body(processInstance.getProcessInstanceId());
    }

    @GetMapping("/todoTask")
    public ResponseEntity<List<TaskRespVO>> todoTask() {
        List<Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey(FlowableConstance.FLOW_ABLE_TEST_PROCESS_ID)
                .active()
                .list();
        return ResponseEntity.ok().body(convert(taskList));
    }

    @GetMapping("/completeTask")
    public ResponseEntity<String> completeTask(String taskId) {
        log.info("处理任务： {}", taskId);
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .active()
                .singleResult();

        Map<String, Object> map = MapUtil.builder(new HashMap<String,Object>())
                .put("p001_assignee", "1000")
                .build();
        taskService.complete(task.getId(), map);
        return ResponseEntity.ok().body("ok");
    }

    @GetMapping("/addTask")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> addTask(String taskId) {
        log.info("任务加签： {}", taskId);
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .active()
                .singleResult();

        Map<String, Object> map = MapUtil.builder(new HashMap<String,Object>())
                .put("p001_assignee", "1000")
                .build();
        Execution p001 = runtimeService.addMultiInstanceExecution("p001", task.getProcessInstanceId(), map);
        return ResponseEntity.ok().body(p001.getId());
    }

    private static List<TaskRespVO> convert(List<Task> taskList) {
        if (CollectionUtil.isEmpty(taskList)) {
            return Collections.emptyList();
        }
        return taskList.stream()
                .map(task -> TaskRespVO.builder()
                        .taskId(task.getId())
                        .taskKey(task.getTaskDefinitionKey())
                        .taskName(task.getName())
                        .assignee(task.getAssignee())
                        .startTime(task.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }
}

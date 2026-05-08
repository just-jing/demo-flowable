package com.example.demoflowable.exporttask.controller;

import com.example.demoflowable.exception.BizException;
import com.example.demoflowable.exception.ErrorCode;
import com.example.demoflowable.exporttask.dto.ExportTaskPageQueryDTO;
import com.example.demoflowable.exporttask.dto.ExportTaskSubmitDTO;
import com.example.demoflowable.exporttask.dto.PageResult;
import com.example.demoflowable.exporttask.entity.ExportTask;
import com.example.demoflowable.exporttask.enums.TaskStatusEnum;
import com.example.demoflowable.exporttask.service.ExportTaskService;
import com.example.demoflowable.exporttask.vo.ExportTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/export-task")
public class ExportTaskController {

    @Resource
    private ExportTaskService exportTaskService;

    @Value("${export.task.file-dir}")
    private String exportFileDir;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submit(@Valid @RequestBody ExportTaskSubmitDTO dto) {
        String taskId = exportTaskService.submit(dto);
        return ResponseEntity.ok(Collections.singletonMap("id", taskId));
    }

    @GetMapping("/page")
    public ResponseEntity<PageResult<ExportTaskVO>> page(ExportTaskPageQueryDTO dto) {
        return ResponseEntity.ok(exportTaskService.pageQuery(dto));
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable String id) {
        exportTaskService.cancel(id);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String id) {
        ExportTask task = exportTaskService.getById(id);
        if (!TaskStatusEnum.COMPLETED.getCode().equals(task.getStatus())) {
            throw new BizException(ErrorCode.TASK_DOWNLOAD_FAILED);
        }
        File file = new File(exportFileDir, task.getExportFile());
        if (!file.exists()) {
            throw new BizException(ErrorCode.TASK_DOWNLOAD_FAILED);
        }
        org.springframework.core.io.Resource resource = new FileSystemResource(file);
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(task.getExportFile(), "UTF-8")
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .contentLength(file.length())
                .body(resource);
    }
}

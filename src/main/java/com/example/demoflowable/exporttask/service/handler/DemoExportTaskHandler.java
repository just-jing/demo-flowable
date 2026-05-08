package com.example.demoflowable.exporttask.service.handler;

import cn.hutool.core.date.DateUtil;
import com.example.demoflowable.exporttask.entity.ExportTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

@Slf4j
@Component
public class DemoExportTaskHandler implements ExportTaskHandler {

    @Value("${export.task.file-dir}")
    private String exportFileDir;

    @Override
    public String getType() {
        return "demoExport";
    }

    @Override
    public String execute(ExportTask task) throws Exception {
        String fileName = "demo_export_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".txt";

        StringBuilder content = new StringBuilder();
        content.append("导出任务: ").append(task.getTaskName()).append("\n");
        content.append("任务ID: ").append(task.getId()).append("\n");
        content.append("创建人: ").append(task.getCreator()).append("\n");
        content.append("创建时间: ").append(DateUtil.formatDateTime(task.getCreatedTime())).append("\n");
        content.append("导出参数: ").append(task.getExportParams()).append("\n");
        content.append("导出时间: ").append(DateUtil.formatDateTime(new Date())).append("\n");

        File file = new File(exportFileDir, fileName);
        Files.write(file.toPath(), content.toString().getBytes(StandardCharsets.UTF_8));

        log.info("Demo导出文件已生成: {}", file.getAbsolutePath());
        return fileName;
    }
}

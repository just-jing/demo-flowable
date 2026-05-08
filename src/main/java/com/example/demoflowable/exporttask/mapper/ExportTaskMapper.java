package com.example.demoflowable.exporttask.mapper;

import com.example.demoflowable.exporttask.entity.ExportTask;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ExportTaskMapper {

    int insert(ExportTask task);

    int updateById(ExportTask task);

    ExportTask selectById(String id);

    ExportTask selectByIdForUpdate(String id);

    List<ExportTask> selectPage(@Param("status") String status,
                                @Param("offset") int offset,
                                @Param("size") int size);

    long count(@Param("status") String status);

    List<ExportTask> selectPendingTopN(@Param("limit") int limit);

    int timeoutRunningTasks(@Param("cutoff") Date cutoff,
                            @Param("errorMessage") String errorMessage,
                            @Param("completedTime") Date completedTime);
}

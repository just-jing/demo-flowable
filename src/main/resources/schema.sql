CREATE TABLE IF NOT EXISTS export_task (
    id              VARCHAR(32)  NOT NULL PRIMARY KEY,
    task_name       VARCHAR(200) NOT NULL,
    creator         VARCHAR(50)  NOT NULL,
    creator_id      BIGINT       NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    handler_type    VARCHAR(100) NOT NULL,
    export_params   TEXT,
    export_file     VARCHAR(500),
    file_size       BIGINT,
    error_message   TEXT,
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time      DATETIME,
    completed_time  DATETIME,
    version         INT          NOT NULL DEFAULT 0
);

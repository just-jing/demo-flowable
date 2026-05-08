# demo-flowable 项目指南

## 项目概览

Spring Boot 2.7.6 + Flowable 6.8.0 工作流引擎演示项目。使用 H2 文件数据库，集成了 Spring Security 进行认证模拟，演示 Flowable 的核心流程操作（启动、查待办、审批、加签）。

**技术栈**：Java 8 / Maven / Spring Boot 2.7.6 / Flowable 6.8.0 / Spring Security / MyBatis / H2 Database / Lombok / Hutool / Guava

## 目录说明

```
src/main/java/com/example/demoflowable/
├── configs/                    # Spring 配置层
│   ├── AuthenticationFilter.java   # 认证过滤器，自动设置默认用户
│   ├── FlowableConfig.java         # Flowable 引擎配置（注册事件监听器）
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   └── SecurityConfig.java         # Spring Security 配置（无状态、token 模式）
├── constance/
│   └── FlowableConstance.java      # 流程定义常量
├── controller/
│   └── TestController.java         # 流程操作 REST API
├── entity/
│   ├── LoginUser.java              # 用户实体
│   └── vo/TaskRespVO.java          # 任务返回 VO
├── listeners/
│   ├── BpmTaskEventListener.java   # 引擎事件监听（创建/完成/指派/取消/超时）
│   └── TestEventListener.java      # 任务/执行监听器
└── DemoFlowableApplication.java    # 启动类

src/main/resources/
├── processes/flowable_test.bpmn20.xml  # BPMN 流程定义
├── application.properties              # 应用配置
└── logback-spring.xml                  # 日志配置

src/test/java/
└── DemoFlowableConstanceApplicationTests.java  # 启动上下文测试
```

## 编码规范

### 命名
- **类名**：PascalCase（`FlowableConfig`, `LoginUser`）
- **方法/变量**：camelCase（`startProcess`, `todoTask`）
- **常量**：UPPER_SNAKE_CASE（`FLOW_ABLE_TEST_PROCESS_ID`）
- **包名**：全小写，反域名风格
- **配置文件**：kebab-case（`flowable.async-executor-activate`）

### 代码风格
- 使用 `@Slf4j` 日志，避免直接使用 `System.out`
- 使用 `@Resource`（`javax.annotation.Resource`）进行依赖注入
- REST API 返回 `ResponseEntity<T>`，统一响应格式
- 全局异常通过 `@RestControllerAdvice` 处理
- 认证信息通过 `SecurityContextHolder` 传递
- Flowable 流程变量通过 `Map<String, Object>` 传递

### 技术约定
- **Lombok**：`@Data`, `@Builder`, `@Slf4j`, `@AllArgsConstructor`, `@NoArgsConstructor`
- **Hutool**：集合工具（`CollectionUtil`）、Map 构建（`MapUtil.builder`）、JSON（`JSONUtil`）
- **Guava**：不可变集合（`ImmutableSet`）
- **配置类**：使用 `@Configuration(proxyBeanMethods = false)` 避免代理开销
- **Spring Boot 2.x**：使用 `javax.*`（非 `jakarta.*`）

### 注释
- 中文注释，简洁为主
- 关键逻辑处添加必要说明（如"解决循环依赖"、"基于 token 机制"等）

## 常用命令

```bash
# 打包（跳过测试）
mvn clean package -DskipTests

# 运行
mvn spring-boot:run

# 测试
mvn test

# 打包并运行
mvn clean package -DskipTests && java -jar target/demo-flowable-0.0.1-SNAPSHOT.jar
```

## 流程 API

| 端点 | 说明 |
|------|------|
| `GET /startProcess` | 启动流程实例 |
| `GET /todoTask` | 查询待办任务列表 |
| `GET /completeTask?taskId=` | 完成任务 |
| `GET /addTask?taskId=` | 任务加签（添加多实例执行） |

H2 控制台：`http://localhost:8080/h2-console`

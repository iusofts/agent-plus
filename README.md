<p align="center">
	<img alt="logo" width="100" src="https://raw.gitcode.com/iusoft/vue-agent-template/raw/master/public/logo/ap.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">Agent Plus</h1>
<h4 align="center">开源AI基座
</h4>
<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-blue" alt="version">
  <img src="https://img.shields.io/badge/vue-3.5%2B-orange" alt="rust">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="license">
    <a href="https://gitcode.com/iusofts/agent-plus" target="_blank">
    <img src="https://gitcode.com/iusofts/agent-plus/star/badge.svg" alt="AtomGit Star"/>
  </a>
</p>

基于 Spring Boot 3.5 + JDK 21 + LangChain4j 构建的企业级 AI Agent 应用开发平台，提供模型接入、知识库（RAG）、工具调用、工作流编排、智能体（Agent）等完整能力。

**[📖 使用文档](http://iusofts.com/)** |
**[🚀 在线体验](https://iusofts.com/agent-plus/dist/)** |
**[🐛 问题反馈](https://gitcode.com/iusofts/agent-plus/issues)** |
**[💡 功能建议](https://gitcode.com/iusofts/agent-plus/discussions)**


### 项目源码

| 项目模块         | GitHub 仓库                                                                     | Gitee 仓库                                             | GitCode 仓库                                             |
|--------------|-------------------------------------------------------------------------------|------------------------------------------------------|--------------------------------------------------------|
| 🔧 后端服务      | [agent-plus](https://github.com/iusofts/agent-plus)                           | [agent-plus](https://gitee.com/iusofts/agent-plus)       | [agent-plus](https://gitcode.com/iusofts/agent-plus)       |
| 🛠️ 管理后台     | [agent-plus-admin-front](https://github.com/iusofts/agent-plus-admin-front)   | [agent-plus-admin-front](https://gitee.com/iusofts/agent-plus-admin-front) | [agent-plus-admin-front](https://gitcode.com/iusofts/agent-plus-admin-front) |
| 🎨 用户前端(计划中) | [agent-plus-client-front](https://github.com/iusofts/agent-plus-client-front) | [agent-plus-client-front](https://gitee.com/iusofts/agent-plus-client-front)     | [agent-plus-client-front](https://gitcode.com/iusofts/agent-plus-client-front)     |
| 💻 客户端(计划中)  | [agent-plus-client](https://github.com/iusofts/agent-plus-client)             | [agent-plus-client](https://gitee.com/iusofts/agent-plus-client) | [agent-plus-client](https://gitcode.com/iusofts/agent-plus-client) |
| 📱 移动端(计划中)  | [agent-plus-app](https://github.com/iusofts/agent-plus-app)                | [agent-plus-app](https://gitee.com/iusofts/agent-plus-app) | [agent-plus-app](https://gitcode.com/iusofts/agent-plus-app) |

## 模块结构

```
agent-plus-root                # 父工程，统一版本管理
├── agent-plus-common          # 公共工具、异常、SSE、常量、工具类
├── agent-plus-id              # ID 生成服务
├── agent-plus-core            # 核心抽象（接口、实体、枚举、DTO，包内分包 model / knowledge / tool / workflow / agent）
├── agent-plus-plugin          # 统一插件层（不再拆多个独立 plugin 模块，内部分包）
├── agent-plus-engine          # 执行引擎（LangChain4j 全部运行逻辑：LLM / RAG / Tool / Workflow / Agent）
├── agent-plus-interface       # 业务 CRUD 服务层（模型 / 知识库 / 工具 / 流程 / 智能体管理）的接口定义
├── agent-plus-service         # 业务 CRUD 服务层（模型 / 知识库 / 工具 / 流程 / 智能体管理）的业务实现
└── agent-plus-web             # 接口控制器（admin 管理接口 + execute 流式 SSE 接口 + 客户端接口），可运行启动模块
```

## 模块介绍

### agent-plus-common
公共基础模块，被所有其他模块依赖。承载与业务无关的通用能力：

- 统一响应结构、全局异常与错误码
- SSE 推送工具封装（用于流式对话）
- 通用常量、枚举、工具类（JSON、日期、加解密、反射等）
- 通用配置基类

### agent-plus-id
分布式 ID 生成服务，为业务侧提供统一、有序、可扩展的主键生成能力：

- 支持雪花算法（Snowflake）等常见 ID 策略
- 屏蔽底层实现，向上暴露简单的 ID 获取 API
- 便于后续接入号段、Leaf、UidGenerator 等方案

### agent-plus-core
核心抽象层，只定义"是什么"，不涉及具体实现。用于跨模块共享领域模型，避免循环依赖。内部按业务领域分包：

- `model`：LLM 模型定义、参数、Provider 抽象
- `knowledge`：知识库、文档、切片、向量抽象
- `tool`：工具（Function Calling）接口与元数据
- `workflow`：工作流节点、边、上下文抽象
- `agent`：智能体定义、角色、记忆等抽象

包含各领域的接口、实体（DO/BO）、枚举、DTO/VO。

### agent-plus-plugin
统一插件层。相较于把每个第三方接入（如 OpenAI、Milvus、飞书、企业微信等）拆成独立模块的做法，这里统一收敛到一个模块内，通过内部分包管理，降低模块粒度、提升构建效率：

- LLM Provider 适配（OpenAI / DeepSeek / 通义 / Ollama 等）
- 向量数据库适配（Milvus / PgVector / Redis 等）
- 文档加载器与解析器（PDF / Word / Markdown / Web 等）
- 外部工具适配（搜索、邮件、Webhook 等）

### agent-plus-engine
执行引擎，承载运行时的所有 LangChain4j 逻辑。是"真正干活"的地方：

- LLM 调用与流式输出
- RAG 检索增强：Embedding、召回、重排、Prompt 组装
- Tool 工具调用与结果回填
- Workflow 编排执行、节点调度、上下文流转
- Agent 循环推理、记忆管理、多轮对话

### agent-plus-interface
业务服务层的接口定义模块，只包含 `Service` 接口、入参 / 出参 DTO 与相关契约，不含任何实现：

- 定义模型 / 知识库 / 工具 / 流程 / 智能体等管理服务的对外 API 契约
- 通过接口与实现分离，方便上层（`web`）依赖接口而不感知具体实现

### agent-plus-service
业务服务层的实现模块，实现 `interface` 中定义的服务契约，聚焦管理端的增删改查与领域编排：

- 模型管理（模型注册、鉴权、Key 管理）
- 知识库管理（知识库、文档、切片）
- 工具管理（工具注册与参数配置）
- 工作流管理（流程草稿、发布、版本）
- 智能体管理（Agent 定义、发布、调试）
- 承担事务边界、数据访问（MyBatis / MyBatis-Plus）与领域校验

### agent-plus-web
系统的入口模块，同时也是**唯一可运行的启动模块**，对外暴露 HTTP 接口：

- `bapi/*`：管理端 REST 接口，配合前端进行配置与运营
- `execute/*`：运行时接口，基于 SSE 提供流式对话、Agent 执行、工作流执行
- `api/*`：客户端 / 第三方对接接口
- 承载 Spring Boot 启动类、`application.yml` 等运行时配置，负责最终装配打包

## 依赖关系

```
web ──► service ──► interface ──► engine ──► plugin ──► core ──► common
                                                          ▲
                                                    id ───┘
```

- `common` 位于最底层，被所有上层依赖；`id` 作为基础服务被业务层复用
- `core` 提供领域抽象，`plugin` 依赖 `core` 提供各类第三方实现
- `engine` 负责运行时执行逻辑（LLM / RAG / Tool / Workflow / Agent）
- `interface` 定义业务服务契约，`service` 实现该契约，二者组合完成管理态 CRUD
- `web` 作为入口层聚合所有依赖，是唯一可启动的服务

## 技术栈

- JDK 21
- Spring Boot 3.5.2
- LangChain4j 1.17.2
- Langgraph4j 1.8.20
- MySQL 5.7/8 + MyBatis / MyBatis-Plus
- 缓存和向量库：Redis 8.8
- SpringDoc OpenAPI、Fastjson2、Caffeine、Hibernate Validator

## 环境要求
| 组件 | 版本要求   |
|------|--------|
| JDK | 21     |
| Maven | 3.9+   |
| MySQL | 5.7/8.0 |
| Redis | 8.8    |

本地可以用Docker Desktop安装和启动MySQL8.0和Redis8.8

## 前置检查和配置
```yml


# 在application.yml中修改配置 或者 将application-dev-template.yml重命名为application-dev.yml并在其中添加配置

# Redis和 MySQL的配置酌情修改

# 目前只支持阿里云OSS 必须要严格根据要求配置 否则图标和文件无法上传 
storage:
  oss:
    accessKeyId: XXX
    accessKeySecret: XXX
    endPoint: XXX
    regionId: XXX
    roleArn: XXX
    bucket: XXX
    domain: https://xxxxxxx.aliyuncs.com

```

### 启动

> 检查一下 Maven版本 和 Jdk版本

#### IDEA运行项目

运行入口：
```bash
agent-plus-web/src/main/java/com/iusofts/agentplus/web/Application.java
```

后端服务默认启动在 `http://localhost:8080`

Swagger 文档地址：`http://localhost:8080/swagger-ui.html`

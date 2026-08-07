/*
 Navicat MySQL Dump SQL

 Source Server         : agent-plus
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 127.0.0.1:3306
 Source Schema         : agent-plus

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 27/07/2026 15:12:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_agent
-- ----------------------------
DROP TABLE IF EXISTS `ai_agent`;
CREATE TABLE `ai_agent`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '智能体名称',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '类型 1:自主规划 2:对话流',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '功能介绍',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '智能体图标',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '设定描述',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '使用模型ID',
  `workflow_ids` json NULL COMMENT '绑定工作流ID列表(JSON数组存储)',
  `chat_flow_id` bigint(20) NULL DEFAULT NULL COMMENT '对话流ID',
  `tool_ids` json NULL COMMENT '绑定工具ID列表(JSON数组存储)',
  `opening_statement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '开场白文案',
  `opening_questions` json NULL COMMENT '开场白预置问题(JSON数组存储)',
  `knowledge_base_ids` json NULL COMMENT '绑定知识库ID列表(JSON数组存储)',
  `retrieval_top_k` int(11) NULL DEFAULT 3 COMMENT '知识库召回条数',
  `temperature` decimal(3, 2) NOT NULL DEFAULT 0.70 COMMENT '生成随机性(temperature)',
  `context_rounds` int(11) NOT NULL DEFAULT 5 COMMENT '携带上下文轮数',
  `max_reply_length` int(11) NULL DEFAULT 2000 COMMENT '最大回复长度',
  `max_inference_length` int(11) NULL DEFAULT 4000 COMMENT '最大推理回答长度',
  `enable_question_suggestion` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用用户问题建议 0:否 1:是',
  `custom_suggestion_prompt` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自定义建议提示词 0:否 1:是',
  `suggestion_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '自定义建议提示词',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_agent_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai智能体' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation`  (
  `id` bigint(20) NOT NULL COMMENT '会话id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体id',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '使用模型ID',
  `current_rounds` int(11) NOT NULL DEFAULT 1 COMMENT '当前轮次',
  `last_chat_time` datetime NULL DEFAULT NULL COMMENT '最后聊天时间',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_conversation_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话会话' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_flow
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow`;
CREATE TABLE `ai_flow`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '类型 1:工作流Workflow 2:对话流Chatflow',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '未命名流程' COMMENT '流程名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程唯一编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '流程描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '图标地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `publish_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发布状态 0:未发布 1:已发布',
  `latest_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'v1.0.0' COMMENT '当前最新版本号(v1.0.0格式)',
  `online_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '线上发布版本号，空=未发布',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uk_code_delete`(`code`, `delete_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI流程主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_flow_runtime
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_runtime`;
CREATE TABLE `ai_flow_runtime`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行实例ID',
  `flow_id` bigint(20) NOT NULL COMMENT '流程ID',
  `flow_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程名称(冗余)',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行使用的语义化版本v1.0.0',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '全局追踪ID',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '运行状态 0等待 1运行中 2成功 3失败 4终止',
  `trial_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '试运行标记 0:正式运行 1:流程试运行 2:节点试运行',
  `start_time` datetime(6) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(6) NULL DEFAULT NULL COMMENT '结束时间',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `cost_ms` bigint(20) NULL DEFAULT 0 COMMENT '耗时毫秒',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '触发人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新操作人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flow_run`(`flow_id`, `run_status`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 110 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程运行实例' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_flow_runtime_node
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_runtime_node`;
CREATE TABLE `ai_flow_runtime_node`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `runtime_id` bigint(20) NOT NULL COMMENT '关联运行实例ID',
  `node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'VueFlow节点唯一id',
  `node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点名称(冗余)',
  `node_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型(Start/LLM/Knowledge/Condition/End)',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0未执行 1执行中 2成功 3失败 4跳过',
  `error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '异常堆栈信息',
  `start_time` datetime(6) NULL DEFAULT NULL COMMENT '节点开始时间',
  `end_time` datetime(6) NULL DEFAULT NULL COMMENT '节点结束时间',
  `cost_ms` bigint(20) NULL DEFAULT 0 COMMENT '节点耗时(毫秒)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_runtime_id`(`runtime_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 340 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '运行节点明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_flow_version
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_version`;
CREATE TABLE `ai_flow_version`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_id` bigint(20) NOT NULL COMMENT '关联ai_flow主键',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语义化版本 v1.0.0',
  `version_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '版本别名/标题',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '修改备注',
  `flow_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'VueFlow画布完整数据JSON(nodes/edges/viewport)',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '流程全局配置(入参、超时、重试、权限等)',
  `publishing_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发布状态 0草稿 1已发布 2待审核',
  `publishing_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `publishing_by` bigint(20) NULL DEFAULT NULL COMMENT '发布人ID',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '保存操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '版本生成时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_version`(`flow_id`, `version_no`, `delete_flag`) USING BTREE,
  INDEX `idx_flow_id`(`flow_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI流程版本画布表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库描述',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库图标',
  `collection_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '向量库集合名称',
  `embedding_model_id` bigint(20) NULL DEFAULT NULL COMMENT '嵌入模型ID',
  `chunk_size` int(11) NOT NULL DEFAULT 512 COMMENT '分块大小',
  `chunk_overlap` int(11) NOT NULL DEFAULT 100 COMMENT '分块重叠大小',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_knowledge_base_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_knowledge_chunk
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_chunk`;
CREATE TABLE `ai_knowledge_chunk`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `document_id` bigint(20) NOT NULL COMMENT '文档ID',
  `vector_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量库中的ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分块内容',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '分块序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '分块状态 0:停用 1:启用',
  `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '元数据JSON',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_knowledge_chunk_kb_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_ai_knowledge_chunk_doc_id`(`document_id`) USING BTREE,
  INDEX `idx_ai_knowledge_chunk_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档分块' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_knowledge_doc_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_doc_log`;
CREATE TABLE `ai_knowledge_doc_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `knowledge_base_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库名称',
  `doc_id` bigint(20) NOT NULL COMMENT '文档ID',
  `doc_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档名称',
  `operation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型(ADD/UPDATE/DELETE)',
  `chunk_count` int(11) NULL DEFAULT 0 COMMENT '分块数量',
  `total_char_count` int(11) NULL DEFAULT 0 COMMENT '总字符数',
  `total_embedding_tokens` int(11) NULL DEFAULT 0 COMMENT 'embedding总消耗token',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `start_time` datetime(6) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(6) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_knowledge_base_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_doc_id`(`doc_id`) USING BTREE,
  INDEX `idx_time_sign`(`time_sign`) USING BTREE,
  INDEX `idx_time_hour`(`time_sign`, `hour_sign`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档处理日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_knowledge_document
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_document`;
CREATE TABLE `ai_knowledge_document`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档名称',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text' COMMENT '文档类型 txt/pdf/docx/md/url',
  `doc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档URL/路径',
  `summary` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档内容摘要',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '文档状态 0:待处理 1:处理中 2:可用 3:失败 4:已禁用 5:已归档',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理失败原因',
  `chunk_count` int(11) NOT NULL DEFAULT 0 COMMENT '分块数量',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_knowledge_document_kb_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_ai_knowledge_document_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_knowledge_retrieval_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_retrieval_log`;
CREATE TABLE `ai_knowledge_retrieval_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路追踪ID(关联llm调用)',
  `call_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用来源(AGENT/CHAT/FLOW/API)',
  `source_id` bigint(20) NULL DEFAULT NULL COMMENT '来源ID(智能体ID/会话ID/流程ID)',
  `source_node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源节点ID(工作流节点ID)',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `knowledge_base_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库名称',
  `query_char_count` int(11) NULL DEFAULT 0 COMMENT '查询字符数',
  `query_embedding_tokens` int(11) NULL DEFAULT 0 COMMENT '查询向量化消耗token',
  `top_k` int(11) NULL DEFAULT NULL COMMENT '召回条数',
  `retrieved_count` int(11) NULL DEFAULT 0 COMMENT '实际召回数量',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `start_time` datetime(6) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(6) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_call_source`(`call_source`) USING BTREE,
  INDEX `idx_knowledge_base_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_time_sign`(`time_sign`) USING BTREE,
  INDEX `idx_org_id`(`org_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_time_hour`(`time_sign`, `hour_sign`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库检索日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_llm_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_llm_call_log`;
CREATE TABLE `ai_llm_call_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `call_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用来源(AGENT/CHAT/FLOW/API)',
  `source_id` bigint(20) NULL DEFAULT NULL COMMENT '来源ID(智能体ID/会话ID/流程ID)',
  `source_flow_id` bigint(20) NULL DEFAULT NULL COMMENT '来源流程ID',
  `source_node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源节点ID(工作流节点ID)',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '模型ID',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `model_provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型提供商(QWEN/DOUBAO/OPENAI/CUSTOM)',
  `temperature` decimal(4, 2) NULL DEFAULT NULL COMMENT '生成温度',
  `max_tokens` int(11) NULL DEFAULT NULL COMMENT '最大生成长度',
  `input_char_count` int(11) NULL DEFAULT 0 COMMENT '输入字符数',
  `input_tokens` int(11) NULL DEFAULT 0 COMMENT '输入消耗token数',
  `output_char_count` int(11) NULL DEFAULT 0 COMMENT '输出字符数',
  `output_tokens` int(11) NULL DEFAULT 0 COMMENT '输出消耗token数',
  `total_tokens` int(11) NULL DEFAULT 0 COMMENT '总消耗token数',
  `finish_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结束原因(STOP/TOOL_EXECUTION等)',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误码',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `start_time` datetime(6) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(6) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_call_source`(`call_source`) USING BTREE,
  INDEX `idx_source_id`(`source_id`) USING BTREE,
  INDEX `idx_model_id`(`model_id`) USING BTREE,
  INDEX `idx_time_sign`(`time_sign`) USING BTREE,
  INDEX `idx_org_id`(`org_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_call_status`(`call_status`) USING BTREE,
  INDEX `idx_time_hour`(`time_sign`, `hour_sign`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 237 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI大模型调用日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message`  (
  `id` bigint(20) NOT NULL COMMENT '消息id',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话id',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色(user/assistant)',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容',
  `file_list` json NULL COMMENT '文件列表(JSON数组)',
  `struct_res` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '结构返回值',
  `agent_id` bigint(20) NULL DEFAULT NULL COMMENT '智能体ID',
  `input_tokens` int(11) NULL DEFAULT 0 COMMENT '输入消耗token数',
  `output_tokens` int(11) NULL DEFAULT 0 COMMENT '输出消耗token数',
  `total_tokens` int(11) NULL DEFAULT 0 COMMENT '总消耗token数',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_ai_message_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_model
-- ----------------------------
DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `model_type` int(11) NOT NULL DEFAULT 1 COMMENT '模型类型 1:LLM 2:Embedding',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型提供商：dashscope-阿里云(百炼平台)，volcengine-字节跳动(火山引擎)，openai-OpenAI',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型显示名称',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型图标',
  `api_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API密钥',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'API基础URL',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '模型配置JSON',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认模型 0:否 1:是',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_model_provider`(`provider`) USING BTREE,
  INDEX `idx_ai_model_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模型配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_plugin
-- ----------------------------
DROP TABLE IF EXISTS `ai_plugin`;
CREATE TABLE `ai_plugin`  (
  `id` bigint(20) NOT NULL COMMENT '主键编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '插件名称',
  `plugin_type` tinyint(4) NOT NULL COMMENT '插件类型 1:内置工具 2:服务接口 3:MCP',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '插件描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '插件图标地址',
  `plugin_config` json NULL COMMENT '插件专属配置，按类型区分结构',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0正常 1已删除',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plugin_type`(`plugin_type`, `delete_flag`, `status`) USING BTREE,
  INDEX `idx_plugin_org_id`(`org_id`, `delete_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI插件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_tool
-- ----------------------------
DROP TABLE IF EXISTS `ai_tool`;
CREATE TABLE `ai_tool`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工具名称',
  `plugin_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '插件ID',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '工具类型 1:内置工具 2:服务接口',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工具描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标地址',
  `params_schema` json NULL COMMENT '参数定义(JSON Schema格式)',
  `response_schema` json NULL COMMENT '响应定义(JSON Schema格式)',
  `http_config` json NULL COMMENT 'HTTP配置(JSON格式)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_tool_org_id`(`org_id`) USING BTREE,
  INDEX `idx_plugin_id_delete`(`plugin_id`, `delete_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai工具表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_trace_span
-- ----------------------------
DROP TABLE IF EXISTS `ai_trace_span`;
CREATE TABLE `ai_trace_span`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `trace_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OTel 128-bit traceId(32hex)',
  `span_id` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OTel 64-bit spanId(16hex)',
  `parent_span_id` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父 spanId',
  `span_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'span名称',
  `span_kind` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INTERNAL' COMMENT 'span类型: INTERNAL/SERVER/CLIENT/PRODUCER/CONSUMER',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OK' COMMENT 'span状态: OK/ERROR',
  `status_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息(仅status=ERROR时)',
  `attributes` json NULL COMMENT 'span attribute键值对(含入参/出参等业务信息)',
  `start_time` datetime(3) NOT NULL COMMENT 'span开始时间(毫秒精度)',
  `end_time` datetime(3) NOT NULL COMMENT 'span结束时间(毫秒精度)',
  `duration_ms` bigint(20) NOT NULL COMMENT 'span耗时(毫秒)',
  `org_id` int(11) NULL DEFAULT NULL COMMENT '组织ID',
  `trial_flag` tinyint(4) NULL DEFAULT 0 COMMENT '试运行标记 0:正式 1:试运行',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_start_time`(`start_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 283 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI Trace Span记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_trace_span_payload
-- ----------------------------
DROP TABLE IF EXISTS `ai_trace_span_payload`;
CREATE TABLE `ai_trace_span_payload`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'traceId',
  `span_id` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'spanId，联合唯一',
  `input_payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点入参',
  `output_payload` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点返回值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_trace_span`(`trace_id`, `span_id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 275 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Span入参返回值载荷附表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for id_generator
-- ----------------------------
DROP TABLE IF EXISTS `id_generator`;
CREATE TABLE `id_generator`  (
  `type` int(11) NOT NULL AUTO_INCREMENT COMMENT '类型',
  `uid` int(11) NOT NULL COMMENT '自增ID',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '名称',
  `step_min` int(11) NOT NULL DEFAULT 1 COMMENT '最小步长',
  `step_max` int(11) NOT NULL DEFAULT 1 COMMENT '最大步长',
  PRIMARY KEY (`type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_industry
-- ----------------------------
DROP TABLE IF EXISTS `t_industry`;
CREATE TABLE `t_industry`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '行业名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态（1.启用 2.停用）',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '行业表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

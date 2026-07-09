/*
 AI大模型调用日志 & 知识库检索日志 & 知识库文档处理日志

 Date: 09/07/2026
*/

-- ----------------------------
-- Table structure for ai_llm_call_log (大模型调用日志)
-- ----------------------------
DROP TABLE IF EXISTS `ai_llm_call_log`;
CREATE TABLE `ai_llm_call_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(128) NULL DEFAULT NULL COMMENT '链路追踪ID',
  `call_source` varchar(50) NOT NULL COMMENT '调用来源(AGENT/CHAT/FLOW/API)',
  `source_id` bigint(20) NULL DEFAULT NULL COMMENT '来源ID(智能体ID/会话ID/流程ID)',
  `source_node_id` varchar(100) NULL DEFAULT NULL COMMENT '来源节点ID(工作流节点ID)',
  `business_type` int(11) NULL DEFAULT 0 COMMENT '业务类型',
  `business_id` bigint(20) NULL DEFAULT 0 COMMENT '业务ID',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '模型ID',
  `model_name` varchar(100) NULL DEFAULT NULL COMMENT '模型名称',
  `model_provider` varchar(50) NULL DEFAULT NULL COMMENT '模型提供商(QWEN/DOUBAO/OPENAI/CUSTOM)',
  `temperature` decimal(4,2) NULL DEFAULT NULL COMMENT '生成温度',
  `max_tokens` int(11) NULL DEFAULT NULL COMMENT '最大生成长度',
  `input_messages` JSON NULL DEFAULT NULL COMMENT '输入消息列表',
  `input_char_count` int(11) NULL DEFAULT 0 COMMENT '输入字符数',
  `input_tokens` int(11) NULL DEFAULT 0 COMMENT '输入消耗token数',
  `output_content` text NULL DEFAULT NULL COMMENT '输出内容',
  `output_char_count` int(11) NULL DEFAULT 0 COMMENT '输出字符数',
  `output_tokens` int(11) NULL DEFAULT 0 COMMENT '输出消耗token数',
  `total_tokens` int(11) NULL DEFAULT 0 COMMENT '总消耗token数',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_code` varchar(100) NULL DEFAULT NULL COMMENT '错误码',
  `error_message` text NULL DEFAULT NULL COMMENT '错误信息',
  `start_time` datetime(3) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`),
  INDEX `idx_trace_id`(`trace_id`),
  INDEX `idx_call_source`(`call_source`),
  INDEX `idx_source_id`(`source_id`),
  INDEX `idx_model_id`(`model_id`),
  INDEX `idx_time_sign`(`time_sign`),
  INDEX `idx_org_id`(`org_id`),
  INDEX `idx_create_time`(`create_time`),
  INDEX `idx_call_status`(`call_status`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI大模型调用日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_knowledge_retrieval_log (知识库检索日志)
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_retrieval_log`;
CREATE TABLE `ai_knowledge_retrieval_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(128) NULL DEFAULT NULL COMMENT '链路追踪ID(关联llm调用)',
  `call_source` varchar(50) NOT NULL COMMENT '调用来源(AGENT/CHAT/FLOW/API)',
  `source_id` bigint(20) NULL DEFAULT NULL COMMENT '来源ID(智能体ID/会话ID/流程ID)',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `knowledge_base_name` varchar(100) NULL DEFAULT NULL COMMENT '知识库名称',
  `query` text NULL DEFAULT NULL COMMENT '检索查询内容',
  `query_char_count` int(11) NULL DEFAULT 0 COMMENT '查询字符数',
  `query_embedding_tokens` int(11) NULL DEFAULT 0 COMMENT '查询向量化消耗token',
  `top_k` int(11) NULL DEFAULT NULL COMMENT '召回条数',
  `retrieved_chunks` JSON NULL DEFAULT NULL COMMENT '召回文档块列表',
  `retrieved_count` int(11) NULL DEFAULT 0 COMMENT '实际召回数量',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_message` text NULL DEFAULT NULL COMMENT '错误信息',
  `start_time` datetime(3) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`),
  INDEX `idx_trace_id`(`trace_id`),
  INDEX `idx_call_source`(`call_source`),
  INDEX `idx_knowledge_base_id`(`knowledge_base_id`),
  INDEX `idx_time_sign`(`time_sign`),
  INDEX `idx_org_id`(`org_id`),
  INDEX `idx_create_time`(`create_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库检索日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_knowledge_doc_log (知识库文档处理日志)
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_doc_log`;
CREATE TABLE `ai_knowledge_doc_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `knowledge_base_name` varchar(100) NULL DEFAULT NULL COMMENT '知识库名称',
  `doc_id` bigint(20) NOT NULL COMMENT '文档ID',
  `doc_name` varchar(255) NULL DEFAULT NULL COMMENT '文档名称',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型(ADD/UPDATE/DELETE)',
  `chunk_count` int(11) NULL DEFAULT 0 COMMENT '分块数量',
  `total_char_count` int(11) NULL DEFAULT 0 COMMENT '总字符数',
  `total_embedding_tokens` int(11) NULL DEFAULT 0 COMMENT 'embedding总消耗token',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_message` text NULL DEFAULT NULL COMMENT '错误信息',
  `start_time` datetime(3) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`),
  INDEX `idx_knowledge_base_id`(`knowledge_base_id`),
  INDEX `idx_doc_id`(`doc_id`),
  INDEX `idx_time_sign`(`time_sign`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档处理日志' ROW_FORMAT = Dynamic;

/*
 AI知识库和模型管理表

 Date: 08/07/2026
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_model
-- ----------------------------
DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model` (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `model_type` int(11) NOT NULL DEFAULT 1 COMMENT '模型类型 1:LLM 2:Embedding',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'qwen' COMMENT '提供商 qwen:千问 doubao:豆包 openai:OpenAI',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型显示名称',
  `api_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API密钥',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'API基础地址',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '模型配置JSON',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模型配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_model
-- ----------------------------
INSERT INTO `ai_model` VALUES (1, 1, 'qwen', 'qwen-plus', '千问-Plus', '', 'https://dashscope.aliyuncs.com/compatible-mode/v1', NULL, 1, 1, 0, NOW(), 0, NULL, 0, 1);
INSERT INTO `ai_model` VALUES (2, 2, 'qwen', 'text-embedding-v3', '千问-Embedding-v3', '', 'https://dashscope.aliyuncs.com/compatible-mode/v1', NULL, 1, 1, 0, NOW(), 0, NULL, 0, 1);
INSERT INTO `ai_model` VALUES (3, 1, 'doubao', 'ep-20240606181916-xxxxx', '豆包-pro', '', 'https://ark.cn-beijing.volces.com/api/v3', NULL, 1, 0, 0, NOW(), 0, NULL, 0, 1);

-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base` (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库描述',
  `collection_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '向量库集合名称',
  `embedding_model_id` bigint(20) NULL DEFAULT NULL COMMENT '嵌入模型ID',
  `chunk_size` int(11) NOT NULL DEFAULT 512 COMMENT '分块大小',
  `chunk_overlap` int(11) NOT NULL DEFAULT 100 COMMENT '分块重叠大小',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_knowledge_base_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_knowledge_base
-- ----------------------------
INSERT INTO `ai_knowledge_base` VALUES (1, '测试知识库', '用于测试的知识库', 'kb_test_1', 2, 512, 100, 1, 0, NOW(), 0, NULL, 0, 1);

-- ----------------------------
-- Table structure for ai_knowledge_document
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_document`;
CREATE TABLE `ai_knowledge_document` (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档名称',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text' COMMENT '文档类型 txt/pdf/docx/md/url',
  `doc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档存储URL/路径',
  `doc_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文档内容（小文档可直接存）',
  `summary` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档内容摘要',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态 0:待处理 1:处理中 2:已完成 3:失败',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理失败原因',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_knowledge_document
-- ----------------------------
INSERT INTO `ai_knowledge_document` VALUES (1, 1, '测试文档.txt', 'text', NULL, '这是一个测试文档的内容。这是第一段。这是第二段。这是第三段。这是第四段。这是第五段。这是第六段。这是第七段。这是第八段。这是第九段。这是第十段。', NULL, 2, NULL, 2, 0, NOW(), 0, NULL, 0, 1);

-- ----------------------------
-- Table structure for ai_knowledge_chunk
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_chunk`;
CREATE TABLE `ai_knowledge_chunk` (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `document_id` bigint(20) NOT NULL COMMENT '文档ID',
  `vector_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量库中的ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分块内容',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '分块序号',
  `metadata_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '元数据JSON',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档分块' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_knowledge_chunk
-- ----------------------------
INSERT INTO `ai_knowledge_chunk` VALUES (1, 1, 1, NULL, '这是一个测试文档的内容。这是第一段。这是第二段。这是第三段。这是第四段。', 0, NULL, 0, NOW(), 0, NULL, 0, 1);
INSERT INTO `ai_knowledge_chunk` VALUES (2, 1, 1, NULL, '这是第四段。这是第五段。这是第六段。这是第七段。这是第八段。这是第九段。这是第十段。', 1, NULL, 0, NOW(), 0, NULL, 0, 1);

-- ----------------------------
-- Update id_generator for new types
-- ----------------------------
INSERT INTO `id_generator` (`type`, `uid`, `name`, `step_min`, `step_max`) VALUES
(3, 2, 'knowledge_base', 1, 1),
(4, 10, 'knowledge_document', 1, 1),
(5, 100, 'knowledge_chunk', 1, 1),
(6, 10, 'ai_model', 1, 1)
ON DUPLICATE KEY UPDATE `uid` = `uid`;

SET FOREIGN_KEY_CHECKS = 1;

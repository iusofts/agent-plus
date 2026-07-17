-- ----------------------------
-- 增量变更: ai_knowledge_retrieval_log 新增来源节点ID字段
-- 日期: 2026-07-16
-- ----------------------------

ALTER TABLE `ai_knowledge_retrieval_log`
    ADD COLUMN `source_node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源节点ID(工作流节点ID)' AFTER `source_id`;

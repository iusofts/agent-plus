-- ----------------------------
-- 增量变更: ai_knowledge_base 新增图标字段
-- 日期: 2026-07-14
-- ----------------------------

ALTER TABLE `ai_knowledge_base`
    ADD COLUMN `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库图标' AFTER `description`;

-- ----------------------------
-- 增量变更: ai_knowledge_base 新增启用状态字段
-- 日期: 2026-07-14
-- ----------------------------

ALTER TABLE `ai_knowledge_base`
    ADD COLUMN `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用' AFTER `chunk_overlap`;

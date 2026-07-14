-- ----------------------------
-- 增量变更: ai_agent 新增图标字段
-- 日期: 2026-07-14
-- ----------------------------

ALTER TABLE `ai_agent`
    ADD COLUMN `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '智能体图标' AFTER `description`;

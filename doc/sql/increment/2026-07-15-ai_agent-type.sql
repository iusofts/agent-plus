-- ----------------------------
-- 增量变更: ai_agent 新增类型字段
-- 日期: 2026-07-15
-- ----------------------------

ALTER TABLE `ai_agent`
    ADD COLUMN `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '类型 1:自主规划 2:对话流' AFTER `name`;

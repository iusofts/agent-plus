-- ----------------------------
-- 增量变更: ai_agent 新增状态字段
-- 日期: 2026-07-16
-- ----------------------------

ALTER TABLE `ai_agent`
    ADD COLUMN `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用' AFTER `org_id`;

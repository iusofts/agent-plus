-- ----------------------------
-- 增量变更: ai_flow 新增发布状态字段
-- 日期: 2026-07-15
-- ----------------------------

ALTER TABLE `ai_flow`
    ADD COLUMN `publish_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发布状态 0:未发布 1:已发布' AFTER `status`;

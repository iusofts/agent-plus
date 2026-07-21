-- ----------------------------
-- 增量变更: ai_agent 新增对话流ID字段
-- 日期: 2026-07-21
-- ----------------------------

ALTER TABLE `ai_agent`
    ADD COLUMN `chat_flow_id` bigint(20) NULL DEFAULT NULL COMMENT '对话流ID' AFTER `workflow_ids`;

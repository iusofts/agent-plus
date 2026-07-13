-- ----------------------------
-- 增量变更: ai_agent 新增绑定工具ID列表字段
-- 关联优化: chat 模块智能体绑定工具(Tool)
-- 日期: 2026-07-13
-- ----------------------------

ALTER TABLE `ai_agent`
    ADD COLUMN `tool_ids` json NULL DEFAULT NULL COMMENT '绑定工具ID列表(JSON数组存储)' AFTER `workflow_ids`;

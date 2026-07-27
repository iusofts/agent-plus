-- ----------------------------
-- 增量变更: ai_llm_call_log 新增工具调用/结束原因/工具规格字段
-- 日期: 2026-07-23
-- ----------------------------

ALTER TABLE `ai_llm_call_log`
    ADD COLUMN `finish_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结束原因(STOP/TOOL_EXECUTION等)' AFTER `total_tokens`,
    ADD COLUMN `tool_definitions` json NULL COMMENT '下发给模型的工具规格列表' AFTER `finish_reason`,
    ADD COLUMN `tool_calls` json NULL COMMENT '模型请求的工具调用列表' AFTER `tool_definitions`;

-- ----------------------------
-- 2026-08-07: 为 ai_llm_call_log 表添加来源流程ID字段
-- ----------------------------
ALTER TABLE `ai_llm_call_log` ADD COLUMN `source_flow_id` bigint(20) NULL COMMENT '来源流程ID' AFTER `source_id`;

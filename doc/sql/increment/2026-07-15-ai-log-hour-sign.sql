-- ----------------------------
-- 增量变更: 三类AI日志表新增 hour_sign 小时字段(支撑按小时用量统计)
-- 日期: 2026-07-15
-- 说明: hour_sign 由 start_time 的小时派生(0-23),配合 time_sign 支持按天/按小时聚合。
--       回填语句在历史数据量大时会较慢/锁表,建议低峰执行。
-- ----------------------------

-- 大模型调用日志
ALTER TABLE `ai_llm_call_log`
    ADD COLUMN `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)' AFTER `time_sign`;
ALTER TABLE `ai_llm_call_log`
    ADD INDEX `idx_time_hour`(`time_sign`, `hour_sign`);
UPDATE `ai_llm_call_log` SET `hour_sign` = HOUR(`start_time`) WHERE `hour_sign` IS NULL AND `start_time` IS NOT NULL;

-- 知识库检索日志
ALTER TABLE `ai_knowledge_retrieval_log`
    ADD COLUMN `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)' AFTER `time_sign`;
ALTER TABLE `ai_knowledge_retrieval_log`
    ADD INDEX `idx_time_hour`(`time_sign`, `hour_sign`);
UPDATE `ai_knowledge_retrieval_log` SET `hour_sign` = HOUR(`start_time`) WHERE `hour_sign` IS NULL AND `start_time` IS NOT NULL;

-- 知识库文档处理日志
ALTER TABLE `ai_knowledge_doc_log`
    ADD COLUMN `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)' AFTER `time_sign`;
ALTER TABLE `ai_knowledge_doc_log`
    ADD INDEX `idx_time_hour`(`time_sign`, `hour_sign`);
UPDATE `ai_knowledge_doc_log` SET `hour_sign` = HOUR(`start_time`) WHERE `hour_sign` IS NULL AND `start_time` IS NOT NULL;

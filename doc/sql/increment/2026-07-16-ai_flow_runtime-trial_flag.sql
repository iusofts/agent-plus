-- ----------------------------
-- 增量变更: ai_flow_runtime 新增是否试运行字段
-- 日期: 2026-07-16
-- ----------------------------

ALTER TABLE `ai_flow_runtime`
    ADD COLUMN `trial_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否试运行 0:正式 1:试运行' AFTER `run_status`;

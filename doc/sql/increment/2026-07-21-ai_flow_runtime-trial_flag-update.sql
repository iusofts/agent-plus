-- ----------------------------
-- 增量变更: ai_flow_runtime.trial_flag 区分流程试运行/节点试运行
-- 日期: 2026-07-21
-- ----------------------------

ALTER TABLE `ai_flow_runtime`
    MODIFY COLUMN `trial_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '试运行标记 0:正式运行 1:流程试运行 2:节点试运行' AFTER `run_status`;

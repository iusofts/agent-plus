-- ----------------------------
-- 增量变更: ai_flow_runtime 冗余流程名称、ai_flow_runtime_node 冗余节点名称
-- 日期: 2026-07-23
-- ----------------------------

ALTER TABLE `ai_flow_runtime`
    ADD COLUMN `flow_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程名称(冗余)' AFTER `flow_id`;

ALTER TABLE `ai_flow_runtime_node`
    ADD COLUMN `node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点名称(冗余)' AFTER `node_id`;

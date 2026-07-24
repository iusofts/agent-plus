-- 增量脚本: AI Trace Span 记录表
-- 用于存储 OpenTelemetry Span 数据，与 ai_flow_runtime、ai_llm_call_log 等业务表通过 trace_id 关联
-- trace_id 为 OTel 128-bit traceId（32位hex），索引 idx_trace_id 用于按链路查询

DROP TABLE IF EXISTS `ai_trace_span`;
CREATE TABLE `ai_trace_span` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `trace_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OTel 128-bit traceId(32hex)',
  `span_id` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OTel 64-bit spanId(16hex)',
  `parent_span_id` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '父 spanId',
  `span_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'span名称',
  `span_kind` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INTERNAL' COMMENT 'span类型: INTERNAL/SERVER/CLIENT/PRODUCER/CONSUMER',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OK' COMMENT 'span状态: OK/ERROR',
  `status_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息(仅status=ERROR时)',
  `attributes` json DEFAULT NULL COMMENT 'span attribute键值对(含入参/出参等业务信息)',
  `start_time` datetime(3) NOT NULL COMMENT 'span开始时间(毫秒精度)',
  `end_time` datetime(3) NOT NULL COMMENT 'span结束时间(毫秒精度)',
  `duration_ms` bigint(20) NOT NULL COMMENT 'span耗时(毫秒)',
  `org_id` int(11) DEFAULT NULL COMMENT '组织ID',
  `trial_flag` tinyint(4) DEFAULT 0 COMMENT '试运行标记 0:正式 1:试运行',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_start_time`(`start_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI Trace Span记录' ROW_FORMAT = Dynamic;
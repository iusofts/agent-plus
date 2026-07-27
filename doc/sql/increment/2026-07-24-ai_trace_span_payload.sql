-- 增量脚本: AI Trace Span 入参返回值载荷附表
-- 从主表 ai_trace_span 拆出大字段(入参/出参),主表保持轻量。
-- 按 (trace_id, span_id) 与主表关联。

DROP TABLE IF EXISTS `ai_trace_span_payload`;
CREATE TABLE `ai_trace_span_payload` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_id` varchar(32) NOT NULL COMMENT 'traceId',
  `span_id` varchar(16) NOT NULL COMMENT 'spanId，联合唯一',
  `input_payload` TEXT DEFAULT NULL COMMENT '节点入参',
  `output_payload` TEXT DEFAULT NULL COMMENT '节点返回值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_span` (`trace_id`,`span_id`),
  KEY `idx_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Span入参返回值载荷附表';
-- ============================================================
-- 2026-08-10: AI Trace 采样率配置表
-- ============================================================
-- 背景:
--   AI Trace 链路追踪(OTel)开启后,每次 chat/flow/knowledge 都会落 span,
--   高并发场景下采样率需可配。设计一张配置表 ai_trace_sample_config 支持:
--     1) 全局级别(整租户唯一一份)
--     2) 组织级别(每个 orgId 一份,覆盖全局)
--     3) 用户级别(每个 userId 一份,覆盖组织)
--   解析优先级:用户 > 组织 > 全局 > yml 兜底(default-sample-rate)。
--   表中带软删除(delete_flag)和启用状态(status)两个开关位,
--   前端通过此表维护采样策略。
--
-- 字段说明:
--   id            自增主键
--   config_type   配置类型 1:全局 2:组织 3:用户
--   target_id     目标ID(全局=0;组织=orgId;用户=userId),配合 config_type 唯一定位
--   sample_rate   采样率,取值 0.0000 ~ 1.0000(decimal(5,4))
--   status        启用状态 0:禁用 1:启用(默认 1)
--   remark        备注
--   create_by     创建人
--   create_time   创建时间
--   update_by     最后更新人
--   update_time   最后更新时间
--   delete_flag   软删除标记 0:正常 1:已删除
--
-- 唯一键:
--   uk_type_target (config_type, target_id, delete_flag)
--     约束:同一 (config_type, target_id) 在未删除态下只能有一条记录,
--     软删除的数据不参与唯一性检查(允许历史回滚/重启用)。
-- ============================================================

DROP TABLE IF EXISTS `ai_trace_sample_config`;
CREATE TABLE `ai_trace_sample_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '配置类型 1:全局 2:组织 3:用户',
  `target_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '目标ID(全局=0;组织=orgId;用户=userId)',
  `sample_rate` decimal(5,4) NOT NULL DEFAULT 1.0000 COMMENT '采样率(0.0000 ~ 1.0000)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除 0:正常 1:已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_type_target` (`config_type`, `target_id`, `delete_flag`) USING BTREE,
  KEY `idx_config_type` (`config_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI Trace 采样率配置表' ROW_FORMAT=DYNAMIC;


-- ============================================================
-- 初始化数据(可选):全局默认采样率 1.0(全量采集)
--   - 用户在 yml 中关闭 trace 或调整 default-sample-rate 时,这条
--     全局配置也仍然可以单独调整并被解析器使用。
-- ============================================================
INSERT INTO `ai_trace_sample_config`
  (config_type, target_id, sample_rate, status, remark, create_by, delete_flag)
VALUES
  (1, 0, 1.0000, 1, '系统初始化:全局默认采样率', 0, 0);

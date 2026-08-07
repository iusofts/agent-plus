-- ============================================================
-- 2026-08-07: sys_oper_log 大字段拆分 —— 新增 sys_oper_log_payload 附表
-- ============================================================
-- 背景:
--   原 sys_oper_log 主表同时存放轻量字段(标题/方法/URL/IP/操作人/时间等)
--   与大字段(oper_param / json_result / error_msg)。在分表场景下大字段
--   会拉宽每行、放大 IO 与缓存,故参考 ai_trace_span_payload 的设计
--   把大字段拆分到附表 sys_oper_log_payload,主表与附表按天分表、生命周期一致。
--
-- 拆分后主表字段:
--   id, title, business_type, method, request_method, operator_type,
--   oper_name, dept_name, oper_url, user_id, token, oper_ip, oper_location,
--   status, oper_time, execute_time, create_time, update_time, delete_flag
--
-- 附表 sys_oper_log_payload 字段:
--   id, oper_log_id, oper_param, json_result, error_msg, create_time
--   关联键:oper_log_id → sys_oper_log.id (UNIQUE)
--
-- 说明:
--   1. 附表与主表一样按天分表,实际表名:sys_oper_log_payload_yyyy_MM_dd
--      运行时由 LogTableManagementService 与 MySqlSpanExporter 的
--      DynamicTableNameInnerInterceptor 共同维护,本脚本给出基础 DDL 供参考。
--   2. 动态分表建表 DDL 已编码在 MySQLMapper.createOperLogTable /
--      createOperLogPayloadTable,生产环境无需手工执行。
--   3. 若线上已有运行中的分表(含历史数据),需先迁移存量数据再删除大字段,
--      参考下方「存量迁移」一节按需执行。
-- ============================================================


-- ----------------------------
-- 1) 附表基础结构(供文档/工具参考,生产中由动态分表 DDL 维护)
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log_payload`;
CREATE TABLE `sys_oper_log_payload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `oper_log_id` bigint(20) NOT NULL COMMENT '关联 sys_oper_log.id,唯一',
  `oper_param` longtext COMMENT '请求参数',
  `json_result` longtext COMMENT '返回参数',
  `error_msg` longtext COMMENT '错误消息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_oper_log_id` (`oper_log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='操作日志大字段载荷附表';


-- ----------------------------
-- 2) 存量迁移(按需在历史分表上执行,无历史数据可跳过)
-- ----------------------------
-- 以下 SQL 块生成针对每个 sys_oper_log_YYYY_MM_DD 分表建附表、
-- 迁移大字段到附表、并从主表删除大字段。生成完成后请人工审阅后再执行。
--
-- 步骤 1:为每个历史分表创建附表
-- SELECT CONCAT(
--   'CREATE TABLE `', table_name, ''' (...) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;'
-- ) FROM information_schema.tables
-- WHERE table_schema = DATABASE() AND table_name LIKE 'sys_oper_log_%';
--
-- 步骤 2:迁移存量大字段(以某分表为例)
-- INSERT INTO `sys_oper_log_payload_2026_08_06`(oper_log_id, oper_param, json_result, error_msg, create_time)
-- SELECT id, oper_param, json_result, error_msg, NOW(3)
-- FROM `sys_oper_log_2026_08_06`
-- WHERE oper_param IS NOT NULL OR json_result IS NOT NULL OR error_msg IS NOT NULL;
--
-- 步骤 3:主表删除大字段
ALTER TABLE `sys_oper_log_2026_08_06`
  DROP COLUMN `oper_param`,
  DROP COLUMN `json_result`,
  DROP COLUMN `error_msg`;


-- ----------------------------
-- 3) 索引建议(已在 MySQLMapper 动态 DDL 中包含,这里仅作记录)
-- ----------------------------
-- sys_oper_log:
--   PRIMARY KEY (id)
--   KEY idx_user_id (user_id)
--
-- sys_oper_log_payload:
--   PRIMARY KEY (id)
--   UNIQUE KEY uk_oper_log_id (oper_log_id)
-- ============================================================

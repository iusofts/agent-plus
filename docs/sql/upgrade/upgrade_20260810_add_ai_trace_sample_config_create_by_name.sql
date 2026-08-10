-- ============================================================
-- 2026-08-10: ai_trace_sample_config 新增 create_by_name 展示字段
-- ============================================================
-- 背景:
--   列表页需要展示"创建人"的可读姓名,避免前端再发起额外的用户查询。
--   create_by 仍是定位 ID(对应 sys_user.user_id),create_by_name 仅作
--   冗余展示,不参与解析逻辑。
--   服务端在新增配置时按 create_by 反查 sys_user.name 落库,无需前端传入。
--
-- 字段说明:
--   create_by_name   创建人姓名(冗余展示,来源于 sys_user.name),
--                     可空,允许历史数据保留为空
--
-- 索引:
--   不加索引:该列只用于展示,无查询/排序诉求
-- ============================================================

ALTER TABLE `ai_trace_sample_config`
  ADD COLUMN `create_by_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名(展示用,来源于 sys_user.name)' AFTER `create_by`;

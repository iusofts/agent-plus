-- ============================================================
-- 2026-08-10: ai_trace_sample_config 新增 target_name 展示字段
-- ============================================================
-- 背景:
--   列表页与管理界面需要把 target_id 直接展示为可读名称
--   (组织名 / 用户昵称 / 全局标识),避免前端再发起额外的
--   用户/组织查询。
--   target_id 仍是定位主键,target_name 仅作冗余展示/模糊搜索,
--   不参与解析逻辑。
--
-- 字段说明:
--   target_name   目标名称(组织名/用户昵称/全局占位"全局"),
--                 可空,允许后续手工/批任务回填
--
-- 索引:
--   在 target_name 上加普通索引,支持列表模糊搜索 (LIKE '%xxx%')
--   长度 200,前缀索引 64 即可覆盖大部分场景
-- ============================================================

ALTER TABLE `ai_trace_sample_config`
  ADD COLUMN `target_name` varchar(200) DEFAULT NULL COMMENT '目标名称(展示/搜索用,组织名/用户昵称/全局占位)' AFTER `target_id`,
  ADD KEY `idx_target_name` (`target_name`(64)) USING BTREE;

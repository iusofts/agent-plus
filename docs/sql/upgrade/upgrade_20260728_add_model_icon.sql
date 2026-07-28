-- ----------------------------
-- 2026-07-28: 为 ai_model 表添加图标字段
-- ----------------------------
ALTER TABLE `ai_model` ADD COLUMN `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型图标' AFTER `display_name`;

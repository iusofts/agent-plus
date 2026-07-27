-- ----------------------------
-- 增量变更: ai_message 新增文件列表字段
-- 日期: 2026-07-17
-- ----------------------------

ALTER TABLE `ai_message`
    ADD COLUMN `file_list` json NULL COMMENT '文件列表(JSON数组)' AFTER `content`;

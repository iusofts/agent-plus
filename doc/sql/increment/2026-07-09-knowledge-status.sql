-- ----------------------------
-- 增量变更: 知识库文档/分块 状态管理
-- 1. 文档状态枚举扩展: 原 2:已完成 -> 2:可用, 新增 4:已禁用 5:已归档
-- 2. 分块新增启用/停用状态字段
-- 关联逻辑: 文档禁用/归档时联动停用其所有分块并删除向量;恢复可用时重新启用并重建向量
-- 日期: 2026-07-09
-- ----------------------------

-- 文档状态注释更新(枚举值语义变更,无需数据迁移)
ALTER TABLE `ai_knowledge_document`
    MODIFY COLUMN `status` tinyint(4) NOT NULL DEFAULT 0
    COMMENT '文档状态 0:待处理 1:处理中 2:可用 3:失败 4:已禁用 5:已归档';

-- 分块新增状态字段(默认启用)
ALTER TABLE `ai_knowledge_chunk`
    ADD COLUMN `status` tinyint(4) NOT NULL DEFAULT 1
    COMMENT '分块状态 0:停用 1:启用' AFTER `sort_order`;

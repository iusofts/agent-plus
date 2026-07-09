-- ----------------------------
-- 增量变更: ai_agent 新增知识库召回条数字段
-- 关联优化: chat 模块智能体接入知识库检索(RAG)
-- 日期: 2026-07-09
-- ----------------------------

ALTER TABLE `ai_agent`
    ADD COLUMN `retrieval_top_k` int(11) NULL DEFAULT 3 COMMENT '知识库召回条数' AFTER `knowledge_base_ids`;

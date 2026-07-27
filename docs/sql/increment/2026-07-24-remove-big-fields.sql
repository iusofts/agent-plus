-- 移除各表的输入输出大字段（数据统一存储在 ai_trace_span_payload）
-- 执行前请确认已有数据备份

-- ai_flow_runtime
ALTER TABLE ai_flow_runtime DROP COLUMN IF EXISTS input_params;
ALTER TABLE ai_flow_runtime DROP COLUMN IF EXISTS output_result;

-- ai_flow_runtime_node
ALTER TABLE ai_flow_runtime_node DROP COLUMN IF EXISTS node_input;
ALTER TABLE ai_flow_runtime_node DROP COLUMN IF EXISTS node_output;

-- ai_llm_call_log
ALTER TABLE ai_llm_call_log DROP COLUMN IF EXISTS input_messages;
ALTER TABLE ai_llm_call_log DROP COLUMN IF EXISTS input_content;
ALTER TABLE ai_llm_call_log DROP COLUMN IF EXISTS output_content;
ALTER TABLE ai_llm_call_log DROP COLUMN IF EXISTS tool_definitions;
ALTER TABLE ai_llm_call_log DROP COLUMN IF EXISTS tool_calls;

-- ai_knowledge_retrieval_log
ALTER TABLE ai_knowledge_retrieval_log DROP COLUMN IF EXISTS query;
ALTER TABLE ai_knowledge_retrieval_log DROP COLUMN IF EXISTS retrieved_chunks;

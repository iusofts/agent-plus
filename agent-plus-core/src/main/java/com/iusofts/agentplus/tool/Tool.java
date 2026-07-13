package com.iusofts.agentplus.tool;

import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.tool.dto.ToolParam;
import com.iusofts.agentplus.tool.dto.ToolResponseParam;

import java.util.List;

/**
 * 工具执行接口.
 *
 * @author Ivan
 */
public interface Tool {

    /**
     * 获取工具名称.
     *
     * @return 工具名称
     */
    String getName();

    /**
     * 获取工具描述.
     *
     * @return 工具描述
     */
    String getDescription();

    /**
     * 获取入参定义.
     *
     * @return 入参定义列表
     */
    default List<ToolParam> getInputParams() {
        return null;
    }

    /**
     * 获取出参定义.
     *
     * @return 出参定义列表
     */
    default List<ToolResponseParam> getOutputParams() {
        return null;
    }

    /**
     * 执行工具.
     *
     * @param request 执行请求
     * @return 执行结果
     */
    ToolExecuteResult execute(ToolExecuteRequest request);
}

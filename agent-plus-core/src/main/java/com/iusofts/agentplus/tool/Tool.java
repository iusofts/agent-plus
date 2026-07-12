package com.iusofts.agentplus.tool;

import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;

/**
 * 工具执行接口.
 *
 * @author Ivan
 */
public interface Tool {

    /**
     * 获取工具编码.
     *
     * @return 工具编码
     */
    String getCode();

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
     * 执行工具.
     *
     * @param request 执行请求
     * @return 执行结果
     */
    ToolExecuteResult execute(ToolExecuteRequest request);
}

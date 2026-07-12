package com.iusofts.agentplus.plugin.tool;

import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 当前时间工具.
 *
 * @author Ivan
 */
@Component
public class CurrentTimeTool implements Tool {

    private static final String CODE = "current_time";
    private static final String NAME = "当前时间";
    private static final String DESCRIPTION = "获取当前系统时间";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public ToolExecuteResult execute(ToolExecuteRequest request) {
        Map<String, Object> params = request.getParams();
        String pattern = params != null && params.get("pattern") != null
            ? params.get("pattern").toString()
            : "yyyy-MM-dd HH:mm:ss";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            String time = LocalDateTime.now().format(formatter);

            Map<String, Object> result = new HashMap<>();
            result.put("time", time);
            result.put("timestamp", System.currentTimeMillis());
            result.put("pattern", pattern);

            return ToolExecuteResult.success(result);
        } catch (Exception e) {
            return ToolExecuteResult.error("时间格式化失败: " + e.getMessage());
        }
    }
}

package com.iusofts.agentplus.plugin.tool;

import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.tool.dto.ToolParam;
import com.iusofts.agentplus.tool.dto.ToolResponseParam;
import com.iusofts.agentplus.common.enums.ParamTypeEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前时间工具.
 *
 * @author Ivan
 */
@Component
public class CurrentTimeTool implements Tool {

    private static final String NAME = "当前时间";
    private static final String DESCRIPTION = "获取当前系统时间";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public List<ToolParam> getInputParams() {
        ToolParam pattern = new ToolParam();
        pattern.setName("pattern");
        pattern.setDescription("时间格式，如 yyyy-MM-dd HH:mm:ss");
        pattern.setType(ParamTypeEnum.STRING.getValue());
        pattern.setRequired(false);
        pattern.setDefaultValue("yyyy-MM-dd HH:mm:ss");
        pattern.setEnabled(true);
        return List.of(pattern);
    }

    @Override
    public List<ToolResponseParam> getOutputParams() {
        ToolResponseParam time = new ToolResponseParam();
        time.setName("time");
        time.setDescription("格式化后的时间字符串");
        time.setType(ParamTypeEnum.STRING.getValue());
        time.setEnabled(true);

        ToolResponseParam timestamp = new ToolResponseParam();
        timestamp.setName("timestamp");
        timestamp.setDescription("时间戳（毫秒）");
        timestamp.setType(ParamTypeEnum.NUMBER.getValue());
        timestamp.setEnabled(true);

        ToolResponseParam pattern = new ToolResponseParam();
        pattern.setName("pattern");
        pattern.setDescription("使用的时间格式");
        pattern.setType(ParamTypeEnum.STRING.getValue());
        pattern.setEnabled(true);

        return List.of(time, timestamp, pattern);
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

package com.iusofts.agentplus.plugin.tool;

import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.tool.dto.ToolParam;
import com.iusofts.agentplus.tool.dto.ToolResponseParam;
import com.iusofts.agentplus.common.enums.ParamTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前时间工具
 * 获取服务器真实系统当前时间，支持自定义格式化
 *
 * @author Ivan
 */
@Slf4j
@Component
public class CurrentTimeTool implements Tool {

    private static final String NAME = "当前时间";
    private static final String DESCRIPTION = "获取服务器真实当前系统时间，可自定义时间格式化模板";
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

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
        pattern.setDefaultValue(DEFAULT_PATTERN);
        pattern.setEnabled(true);
        return List.of(pattern);
    }

    @Override
    public List<ToolResponseParam> getOutputParams() {
        ToolResponseParam time = new ToolResponseParam();
        time.setName("time");
        time.setDescription("格式化后的标准时间字符串");
        time.setType(ParamTypeEnum.STRING.getValue());
        time.setEnabled(true);

        ToolResponseParam timestamp = new ToolResponseParam();
        timestamp.setName("timestamp");
        timestamp.setDescription("当前毫秒时间戳");
        timestamp.setType(ParamTypeEnum.NUMBER.getValue());
        timestamp.setEnabled(true);

        ToolResponseParam pattern = new ToolResponseParam();
        pattern.setName("pattern");
        pattern.setDescription("本次使用的格式化模板");
        pattern.setType(ParamTypeEnum.STRING.getValue());
        pattern.setEnabled(true);

        return List.of(time, timestamp, pattern);
    }

    @Override
    public ToolExecuteResult execute(ToolExecuteRequest request) {
        Map<String, Object> params = request.getParams();
        String pattern = DEFAULT_PATTERN;
        if (params != null && params.get("pattern") != null) {
            pattern = params.get("pattern").toString().trim();
        }

        try {
            // 获取真实系统当前时间
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            String timeStr = now.format(formatter);

            Map<String, Object> result = new HashMap<>(3);
            result.put("time", timeStr);
            result.put("timestamp", now.toInstant().toEpochMilli());
            result.put("pattern", pattern);

            return ToolExecuteResult.success(result);
        } catch (IllegalArgumentException e) {
            String errMsg = "非法时间格式化模板[" + pattern + "]：" + e.getMessage();
            log.error("当前时间工具，格式模板错误 pattern={}", pattern, e);
            return ToolExecuteResult.error(errMsg);
        } catch (Exception e) {
            String errMsg = "获取系统时间失败：" + e.getMessage();
            log.error("当前时间工具执行异常", e);
            return ToolExecuteResult.error(errMsg);
        }
    }
}
package com.iusofts.agentplus.plugin.tool;

import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 计算器工具.
 *
 * @author Ivan
 */
@Component
public class CalculatorTool implements Tool {

    private static final String CODE = "calculator";
    private static final String NAME = "计算器";
    private static final String DESCRIPTION = "执行基本数学运算：加减乘除";

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
        if (params == null) {
            return ToolExecuteResult.error("参数不能为空");
        }

        String operation = params.get("operation") != null ? params.get("operation").toString() : null;
        if (operation == null) {
            return ToolExecuteResult.error("运算类型(operation)不能为空");
        }

        try {
            BigDecimal a = new BigDecimal(params.get("a").toString());
            BigDecimal b = new BigDecimal(params.get("b").toString());
            BigDecimal result;

            switch (operation.toLowerCase()) {
                case "add":
                case "+":
                    result = a.add(b);
                    break;
                case "subtract":
                case "-":
                    result = a.subtract(b);
                    break;
                case "multiply":
                case "*":
                    result = a.multiply(b);
                    break;
                case "divide":
                case "/":
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        return ToolExecuteResult.error("除数不能为零");
                    }
                    int scale = params.get("scale") != null
                        ? Integer.parseInt(params.get("scale").toString())
                        : 10;
                    result = a.divide(b, scale, RoundingMode.HALF_UP);
                    break;
                default:
                    return ToolExecuteResult.error("不支持的运算类型: " + operation);
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("a", a);
            resultMap.put("b", b);
            resultMap.put("operation", operation);
            resultMap.put("result", result);

            return ToolExecuteResult.success(resultMap);
        } catch (Exception e) {
            return ToolExecuteResult.error("计算失败: " + e.getMessage());
        }
    }
}

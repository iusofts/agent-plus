package com.iusofts.agentplus.plugin.tool;

import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.tool.dto.ToolParam;
import com.iusofts.agentplus.tool.dto.ToolResponseParam;
import com.iusofts.agentplus.common.enums.ParamTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
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
    public List<ToolParam> getInputParams() {
        ToolParam a = new ToolParam();
        a.setName("a");
        a.setDescription("第一个操作数");
        a.setType(ParamTypeEnum.NUMBER.getValue());
        a.setRequired(true);
        a.setEnabled(true);

        ToolParam b = new ToolParam();
        b.setName("b");
        b.setDescription("第二个操作数");
        b.setType(ParamTypeEnum.NUMBER.getValue());
        b.setRequired(true);
        b.setEnabled(true);

        ToolParam operation = new ToolParam();
        operation.setName("operation");
        operation.setDescription("运算类型：add(+)、subtract(-)、multiply(*)、divide(/)");
        operation.setType(ParamTypeEnum.STRING.getValue());
        operation.setRequired(true);
        operation.setEnabled(true);

        ToolParam scale = new ToolParam();
        scale.setName("scale");
        scale.setDescription("除法精度（小数位数）");
        scale.setType(ParamTypeEnum.INTEGER.getValue());
        scale.setRequired(false);
        scale.setDefaultValue("10");
        scale.setEnabled(true);

        return List.of(a, b, operation, scale);
    }

    @Override
    public List<ToolResponseParam> getOutputParams() {
        ToolResponseParam a = new ToolResponseParam();
        a.setName("a");
        a.setDescription("第一个操作数");
        a.setType(ParamTypeEnum.NUMBER.getValue());
        a.setEnabled(true);

        ToolResponseParam bParam = new ToolResponseParam();
        bParam.setName("b");
        bParam.setDescription("第二个操作数");
        bParam.setType(ParamTypeEnum.NUMBER.getValue());
        bParam.setEnabled(true);

        ToolResponseParam operation = new ToolResponseParam();
        operation.setName("operation");
        operation.setDescription("运算类型");
        operation.setType(ParamTypeEnum.STRING.getValue());
        operation.setEnabled(true);

        ToolResponseParam result = new ToolResponseParam();
        result.setName("result");
        result.setDescription("计算结果");
        result.setType(ParamTypeEnum.NUMBER.getValue());
        result.setEnabled(true);

        return List.of(a, bParam, operation, result);
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

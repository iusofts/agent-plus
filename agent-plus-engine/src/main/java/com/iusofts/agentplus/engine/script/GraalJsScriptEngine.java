package com.iusofts.agentplus.engine.script;

import com.alibaba.fastjson2.JSON;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * GraalVM JavaScript 脚本引擎实现。
 *
 * <p>支持两种写法:</p>
 * <ul>
 *   <li>简化写法: 直接使用 params 和 ret 变量</li>
 *   <li>标准写法: 提供 main 函数</li>
 * </ul>
 *
 * @author Ivan
 */
public class GraalJsScriptEngine implements ScriptEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraalJsScriptEngine.class);
    private static final long DEFAULT_TIMEOUT = 30000L;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public Map<String, Object> execute(String script, Map<String, Object> params, Long timeout) throws Exception {
        long effectiveTimeout = timeout != null ? timeout : DEFAULT_TIMEOUT;

        Callable<Map<String, Object>> task = () -> executeInternal(script, params);
        Future<Map<String, Object>> future = executor.submit(task);

        try {
            return future.get(effectiveTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("脚本执行超时: " + effectiveTimeout + "ms", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeInternal(String script, Map<String, Object> params) {
        // 构建包装脚本，支持两种写法
        String wrappedScript = wrapScript(script, params);

        try (Context context = Context.newBuilder("js")
                .allowAllAccess(false)
                .allowHostAccess(HostAccess.NONE)
                .allowCreateProcess(false)
                .allowCreateThread(false)
                .allowIO(false)
                .allowNativeAccess(false)
                .build()) {

            // 执行脚本
            Value result = context.eval("js", wrappedScript);

            // 转换结果
            Object converted = convertValue(result);
            if (converted instanceof Map) {
                return (Map<String, Object>) converted;
            }
            return new HashMap<>();
        }
    }

    private String wrapScript(String script, Map<String, Object> params) {
        // 以 JSON 形式注入 params，使其成为原生 JS 对象（沙箱下也可正常访问）
        String paramsJson = JSON.toJSONString(params != null ? params : new HashMap<>());
        String header = "const params = JSON.parse(" + JSON.toJSONString(paramsJson) + ");\n";

        // 检查是否包含 main 函数
        boolean hasMain = script.contains("function main") || script.contains("const main") || script.contains("let main") || script.contains("var main");

        if (hasMain) {
            // 标准写法: 调用 main({ params }) 并返回结果
            return header + script + "\n\n" +
                    "(function() {\n" +
                    "  if (typeof main === 'function') {\n" +
                    "    return main({ params: params });\n" +
                    "  }\n" +
                    "  return typeof ret !== 'undefined' ? ret : {};\n" +
                    "})();";
        } else {
            // 简化写法: 直接执行，返回 ret 对象
            return header + script + "\n\n" +
                    "(typeof ret !== 'undefined' ? ret : {});";
        }
    }

    /**
     * 将 GraalVM {@link Value} 递归转换为 Java 原生对象(Map / List / 基本类型)。
     */
    private Object convertValue(Value value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isNumber()) {
            if (value.fitsInLong()) {
                return value.asLong();
            }
            return value.asDouble();
        }
        if (value.isString()) {
            return value.asString();
        }
        if (value.hasArrayElements()) {
            long size = value.getArraySize();
            List<Object> list = new ArrayList<>((int) size);
            for (long i = 0; i < size; i++) {
                list.add(convertValue(value.getArrayElement(i)));
            }
            return list;
        }
        if (value.hasMembers()) {
            Map<String, Object> map = new HashMap<>();
            for (String key : value.getMemberKeys()) {
                map.put(key, convertValue(value.getMember(key)));
            }
            return map;
        }
        // 兜底: 转为字符串
        return value.toString();
    }

}

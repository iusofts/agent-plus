package com.iusofts.agentplus.engine.script;

import com.alibaba.fastjson2.JSON;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
        String wrappedScript = wrapScript(script);

        try (Context context = Context.newBuilder("js")
                .allowAllAccess(false)
                .allowHostAccess(HostAccess.NONE)
                .allowCreateProcess(false)
                .allowCreateThread(false)
                .allowIO(false)
                .allowNativeAccess(false)
                .build()) {

            // 注入 params
            context.getBindings("js").putMember("params", params);

            // 执行脚本
            Value result = context.eval("js", wrappedScript);

            // 转换结果
            if (result.isNull()) {
                return new HashMap<>();
            }

            // 尝试转换为 Map
            String json = JSON.toJSONString(result);
            return JSON.parseObject(json, Map.class);
        }
    }

    private String wrapScript(String script) {
        // 检查是否包含 main 函数
        boolean hasMain = script.contains("function main") || script.contains("const main") || script.contains("let main") || script.contains("var main");

        if (hasMain) {
            // 标准写法: 调用 main({ params }) 并返回结果
            // 使用同步方式处理 Promise (GraalVM 会在上下文中处理 await)
            return script + "\n\n" +
                    "(function() {\n" +
                    "  if (typeof main === 'function') {\n" +
                    "    var result = main({ params: params });\n" +
                    "    // 如果是 Promise，等待其完成（GraalVM JS 中的 Promise 可以通过 context 处理）\n" +
                    "    return result;\n" +
                    "  }\n" +
                    "  return typeof ret !== 'undefined' ? ret : {};\n" +
                    "})();";
        } else {
            // 简化写法: 直接执行，返回 ret 对象
            return script + "\n\n" +
                    "(typeof ret !== 'undefined' ? ret : {});";
        }
    }

}

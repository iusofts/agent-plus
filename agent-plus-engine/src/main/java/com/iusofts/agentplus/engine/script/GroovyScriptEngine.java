package com.iusofts.agentplus.engine.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
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
 * Groovy 脚本引擎实现。
 *
 * @author Ivan
 */
public class GroovyScriptEngine implements ScriptEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptEngine.class);
    private static final long DEFAULT_TIMEOUT = 30000L;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final CompilerConfiguration config;

    public GroovyScriptEngine() {
        this.config = createSecureConfiguration();
    }

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
        Binding binding = new Binding();
        binding.setVariable("params", params);

        // 包装脚本，支持 ret 变量
        String wrappedScript = wrapScript(script);

        GroovyShell shell = new GroovyShell(binding, config);
        Object result = shell.evaluate(wrappedScript);

        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }

        // 尝试从 binding 获取 ret
        Object ret = binding.hasVariable("ret") ? binding.getVariable("ret") : null;
        if (ret instanceof Map) {
            return (Map<String, Object>) ret;
        }

        Map<String, Object> output = new HashMap<>();
        if (ret != null) {
            output.put("result", ret);
        }
        return output;
    }

    private String wrapScript(String script) {
        return script + "\n\n" +
                "(binding.hasVariable('ret') ? ret : null)";
    }

    private CompilerConfiguration createSecureConfiguration() {
        CompilerConfiguration config = new CompilerConfiguration();

        // 导入定制
        ImportCustomizer imports = new ImportCustomizer();
        imports.addImports(
                "java.util.ArrayList",
                "java.util.HashMap",
                "java.util.List",
                "java.util.Map"
        );

        config.addCompilationCustomizers(imports);
        return config;
    }

}

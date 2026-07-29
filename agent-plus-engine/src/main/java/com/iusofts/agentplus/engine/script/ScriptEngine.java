package com.iusofts.agentplus.engine.script;

import java.util.Map;

/**
 * 脚本引擎接口。
 *
 * @author Ivan
 */
public interface ScriptEngine {

    /**
     * 执行脚本。
     *
     * @param script  脚本内容
     * @param params  输入参数
     * @param timeout 超时时间(毫秒)
     * @return 执行结果
     */
    Map<String, Object> execute(String script, Map<String, Object> params, Long timeout) throws Exception;

}

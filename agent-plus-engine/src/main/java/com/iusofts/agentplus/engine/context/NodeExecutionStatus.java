package com.iusofts.agentplus.engine.context;

/**
 * 节点执行状态。
 *
 * @author Ivan
 */
public enum NodeExecutionStatus {
    /** 尚未调度 */
    PENDING,
    /** 正在执行 */
    RUNNING,
    /** 成功完成 */
    SUCCESS,
    /** 因条件分支未命中被跳过 */
    SKIPPED,
    /** 执行失败 */
    FAILED
}

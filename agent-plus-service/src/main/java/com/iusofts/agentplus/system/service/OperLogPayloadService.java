package com.iusofts.agentplus.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.system.dao.OperLogPayloadMapper;
import com.iusofts.agentplus.system.entity.OperLogPayload;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * <p>
 * 操作日志大字段载荷落库服务
 * </p>
 *
 * <p>主表 {@code sys_oper_log} 仅保留轻量字段(标题/方法/URL/IP/操作人/时间等),
 * 请求参数、返回参数、错误堆栈等大字段通过本服务写入附表
 * {@code sys_oper_log_payload},附表与主表按同样规则按天分表。</p>
 *
 * @author Ivan
 * @since 2026-08-07
 */
@DS("sys")
@Service
public class OperLogPayloadService extends ServiceImpl<OperLogPayloadMapper, OperLogPayload> {

    /**
     * 单条保存:若 operParam / jsonResult / errorMsg 全为空则不落附表。
     */
    public void saveIfNeeded(OperLogPayload payload) {
        if (payload == null) {
            return;
        }
        if (isEmpty(payload)) {
            return;
        }
        save(payload);
    }

    /**
     * 批量保存:过滤掉三个大字段全部为空的记录。
     */
    public void batchSaveIfNeeded(Collection<OperLogPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }
        saveBatch(payloads.stream().filter(this::notEmpty).toList());
    }

    private boolean notEmpty(OperLogPayload p) {
        return p != null && !isEmpty(p);
    }

    private boolean isEmpty(OperLogPayload p) {
        return (p.getOperParam() == null || p.getOperParam().isEmpty())
                && (p.getJsonResult() == null || p.getJsonResult().isEmpty())
                && (p.getErrorMsg() == null || p.getErrorMsg().isEmpty());
    }
}

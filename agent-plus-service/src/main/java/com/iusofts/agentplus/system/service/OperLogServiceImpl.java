package com.iusofts.agentplus.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.mybatis.Query2Wrapper;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.system.dao.OperLogMapper;
import com.iusofts.agentplus.system.dto.OperLogAddParam;
import com.iusofts.agentplus.system.dto.OperLogDto;
import com.iusofts.agentplus.system.dto.OperateLogQueryParam;
import com.iusofts.agentplus.system.entity.OperLog;
import com.iusofts.agentplus.system.entity.OperLogPayload;
import com.iusofts.agentplus.system.interfaces.IOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 操作日志记录 服务实现类
 * </p>
 *
 * <p>主表仅落轻量字段,大字段(请求参数/返回参数/错误堆栈)落到附表
 * {@code sys_oper_log_payload}。附表与主表按同样规则按天分表,
 * 写入时需保证主表与附表同事务。</p>
 *
 * @author Ivan
 * @since 2020-12-09
 */
@DS("sys")
@Slf4j
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements IOperLogService {

    private final OperLogPayloadService operLogPayloadService;

    public OperLogServiceImpl(OperLogPayloadService operLogPayloadService) {
        this.operLogPayloadService = operLogPayloadService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(OperLogAddParam param) {
        OperLog entity = ModelMapperUtil.strictMap(param, OperLog.class);
        super.save(entity);
        // save 后自增主键回填到 entity.id
        Integer operLogId = entity.getId();
        if (operLogId == null) {
            return;
        }
        OperLogPayload payload = new OperLogPayload();
        payload.setOperLogId(operLogId.longValue());
        payload.setOperParam(param.getOperParam());
        payload.setJsonResult(param.getJsonResult());
        payload.setErrorMsg(param.getErrorMsg());
        operLogPayloadService.saveIfNeeded(payload);
    }

    @Override
    public PageResult<OperLogDto> queryPage(OperateLogQueryParam param) {
        PageResult<OperLogDto> pageResult = new PageResult<>();
        Query2Wrapper<OperLog> wrapper = new Query2Wrapper<>();

        if (param.getId() != null) {
            wrapper.eq(OperLog.Fields.id, param.getId());
        }
        if (StringUtils.isNotBlank(param.getTitle())) {
            wrapper.eq(OperLog.Fields.title, param.getTitle());
        }
        if (StringUtils.isNotBlank(param.getOperName())) {
            wrapper.eq(OperLog.Fields.operName, param.getOperName());
        }
        if (StringUtils.isNotBlank(param.getOperUrl())) {
            wrapper.eq(OperLog.Fields.operUrl, param.getOperUrl());
        }
        if (StringUtils.isNotBlank(param.getOperIp())) {
            wrapper.eq(OperLog.Fields.operIp, param.getOperIp());
        }
        if (param.getOperatorType() != null) {
            wrapper.eq(OperLog.Fields.operatorType, param.getOperatorType());
        }
        if (param.getStatus() != null) {
            wrapper.eq(OperLog.Fields.status, param.getStatus());
        }
        if (param.getRequestTimeStart() != null) {
            wrapper.ge(OperLog.Fields.operTime, param.getRequestTimeStart());
        }
        if (param.getRequestTimeEnd() != null) {
            wrapper.le(OperLog.Fields.operTime, param.getRequestTimeEnd());
        }
        wrapper.orderByDesc("id");
        Page<OperLog> pageParam = new Page<>(param.getCurrentPage(), param.getPageSize());
        IPage<OperLog> page = super.page(pageParam, wrapper);
        List<OperLog> records = page.getRecords();
        List<OperLogDto> operateLogDtos = ModelMapperUtil.strictMapList(records, OperLogDto.class);
        // 列表不补回大字段,按需在详情接口读取
        pageResult.setDataList(operateLogDtos);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public OperLogDto getDetail(Integer id) {
        OperLog log = super.getById(id);
        if (log == null) {
            throw new SystemBusinessException("日志不存在");
        }
        OperLogDto dto = ModelMapperUtil.strictMap(log, OperLogDto.class);
        // 从附表补回大字段
        OperLogPayload payload = operLogPayloadService.lambdaQuery()
                .eq(OperLogPayload::getOperLogId, id.longValue())
                .one();
        if (payload != null) {
            dto.setOperParam(payload.getOperParam());
            dto.setJsonResult(payload.getJsonResult());
            dto.setErrorMsg(payload.getErrorMsg());
        }
        return dto;
    }

    /**
     * 兼容旧入参方式(若仍需要批量写入接口可保留)。
     */
    public void addBatch(List<OperLogAddParam> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        params.forEach(this::add);
    }

}

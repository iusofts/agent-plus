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
import com.iusofts.agentplus.system.interfaces.IOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 操作日志记录 服务实现类
 * </p>
 *
 * @author Ivan
 * @since 2020-12-09
 */
@DS("sys")
@Slf4j
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements IOperLogService {

    @Override
    public void add(OperLogAddParam param) {
        super.save(ModelMapperUtil.strictMap(param, OperLog.class));
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
        wrapper.select(OperLog.class, info -> !info.getColumn().equals(OperLog.Fields.jsonResult) && !info.getColumn().equals(OperLog.Fields.operParam));
        Page<OperLog> pageParam = new Page<>(param.getCurrentPage(), param.getPageSize());
        IPage<OperLog> page = super.page(pageParam, wrapper);
        List<OperLog> records = page.getRecords();
        List<OperLogDto> operateLogDtos = ModelMapperUtil.strictMapList(records, OperLogDto.class);
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
        return ModelMapperUtil.strictMap(log, OperLogDto.class);
    }

}

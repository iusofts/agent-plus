package com.iusofts.agentplus.system.interfaces;

import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.system.dto.OperLogAddParam;
import com.iusofts.agentplus.system.dto.OperLogDto;
import com.iusofts.agentplus.system.dto.OperateLogQueryParam;

/**
 * <p>
 * 操作日志记录 服务类
 * </p>
 *
 * @author Ivan
 * @since 2020-12-09
 */
public interface IOperLogService {

    void add(OperLogAddParam param);

    /**
     * 查询列表
     *
     * @param param
     * @return
     */
    PageResult<OperLogDto> queryPage(OperateLogQueryParam param);

    /**
     * 获取详情
     * @param id
     * @return
     */
    OperLogDto getDetail(Integer id);
    
}

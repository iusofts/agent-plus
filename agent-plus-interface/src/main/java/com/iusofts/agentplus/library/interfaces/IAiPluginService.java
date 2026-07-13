package com.iusofts.agentplus.library.interfaces;

import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginAddReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginDetailVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginEditReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginQueryPageReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginVo;

/**
 * ai插件服务接口
 *
 * @author Ivan
 * @since 2026-07-13
 */
public interface IAiPluginService {

    /**
     * 分页查询
     *
     * @param reqVo 查询条件
     * @return 分页结果
     */
    PageResult<AiPluginVo> queryPage(AiPluginQueryPageReqVo reqVo);

    /**
     * 查询详情
     *
     * @param reqVo ID请求
     * @return 插件详情
     */
    AiPluginDetailVo getById(IdReqVo reqVo);

    /**
     * 新增插件
     *
     * @param reqVo 新增请求
     */
    void add(AiPluginAddReqVo reqVo);

    /**
     * 编辑插件
     *
     * @param reqVo 编辑请求
     */
    void edit(AiPluginEditReqVo reqVo);

    /**
     * 删除插件
     *
     * @param reqVo ID请求
     */
    void remove(IdReqVo reqVo);

}

package com.iusofts.agentplus.library.interfaces;

import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolAddReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolDetailVo;
import com.iusofts.agentplus.library.vo.tool.AiToolEditReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolQueryPageReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolVo;

/**
 * ai工具服务接口
 *
 * @author Ivan
 * @since 2026-07-12
 */
public interface IAiToolService {

    /**
     * 分页查询
     *
     * @param reqVo 查询条件
     * @return 分页结果
     */
    PageResult<AiToolVo> queryPage(AiToolQueryPageReqVo reqVo);

    /**
     * 查询详情
     *
     * @param reqVo ID请求
     * @return 工具详情
     */
    AiToolDetailVo getById(IdReqVo reqVo);

    /**
     * 新增工具
     *
     * @param reqVo 新增请求
     */
    void add(AiToolAddReqVo reqVo);

    /**
     * 编辑工具
     *
     * @param reqVo 编辑请求
     */
    void edit(AiToolEditReqVo reqVo);

    /**
     * 删除工具
     *
     * @param reqVo ID请求
     */
    void remove(IdReqVo reqVo);

}

package com.iusofts.agentplus.basics.interfaces;

import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.basics.vo.IndustryAddReqVo;
import com.iusofts.agentplus.basics.vo.IndustryChangeStatusVo;
import com.iusofts.agentplus.basics.vo.IndustryEditVo;
import com.iusofts.agentplus.basics.vo.IndustryQueryPageReqVo;
import com.iusofts.agentplus.basics.vo.IndustryVo;
import com.iusofts.agentplus.common.vo.IdReqVo;

public interface IIndustryService {
    void add(IndustryAddReqVo reqVo);
    PageResult<IndustryVo> queryPage(IndustryQueryPageReqVo reqVo);
    void edit(IndustryEditVo reqVo);
    void changeStatus(IndustryChangeStatusVo reqVo);
    void deleteById(IdReqVo reqVo);
}

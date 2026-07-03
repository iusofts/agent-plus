package com.iusofts.basics.interfaces;

import com.iusofts.basic.page.PageResult;
import com.iusofts.basics.vo.IndustryAddReqVo;
import com.iusofts.basics.vo.IndustryChangeStatusVo;
import com.iusofts.basics.vo.IndustryEditVo;
import com.iusofts.basics.vo.IndustryQueryPageReqVo;
import com.iusofts.basics.vo.IndustryVo;
import com.iusofts.common.vo.IdReqVo;

public interface IIndustryService {
    void add(IndustryAddReqVo reqVo);
    PageResult<IndustryVo> queryPage(IndustryQueryPageReqVo reqVo);
    void edit(IndustryEditVo reqVo);
    void changeStatus(IndustryChangeStatusVo reqVo);
    void deleteById(IdReqVo reqVo);
}

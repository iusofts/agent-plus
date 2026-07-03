package com.iusofts.basics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.basic.page.PageResult;
import com.iusofts.basic.utils.ModelMapperUtil;
import com.iusofts.basics.entity.Industry;
import com.iusofts.basics.interfaces.IIndustryService;
import com.iusofts.basics.mapper.IndustryMapper;
import com.iusofts.basics.vo.IndustryAddReqVo;
import com.iusofts.basics.vo.IndustryChangeStatusVo;
import com.iusofts.basics.vo.IndustryEditVo;
import com.iusofts.basics.vo.IndustryQueryPageReqVo;
import com.iusofts.basics.vo.IndustryVo;
import com.iusofts.common.vo.IdReqVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndustryImpl extends ServiceImpl<IndustryMapper, Industry> implements IIndustryService {

    @Override
    public void add(IndustryAddReqVo reqVo) {
        Industry industry = ModelMapperUtil.strictMap(reqVo, Industry.class);
        industry.setCreateBy(reqVo.getOperatorId());
        super.save(industry);
    }

    @Override
    public PageResult<IndustryVo> queryPage(IndustryQueryPageReqVo reqVo) {
        PageResult<IndustryVo> pageResult = new PageResult<>();
        LambdaQueryWrapper<Industry> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(reqVo.getName())) {
            wrapper.like(Industry::getName, reqVo.getName());
        }
        if (reqVo.getStatus() != null) {
            wrapper.eq(Industry::getStatus, reqVo.getStatus());
        }
        wrapper.orderByAsc(Industry::getSort);
        Page<Industry> pageParam = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());
        IPage<Industry> page = this.page(pageParam, wrapper);
        List<IndustryVo> list = page.getRecords().stream().map(item -> {
            IndustryVo vo = ModelMapperUtil.strictMap(item, IndustryVo.class);
            return vo;
        }).toList();
        pageResult.setDataList(list);
        pageResult.setTotalCount(page.getTotal());
        return pageResult;
    }

    @Override
    public void edit(IndustryEditVo reqVo) {
        Industry industry = ModelMapperUtil.strictMap(reqVo, Industry.class);
        industry.setUpdateBy(reqVo.getOperatorId());
        super.updateById(industry);
    }

    @Override
    public void changeStatus(IndustryChangeStatusVo reqVo) {
        Industry industry = ModelMapperUtil.strictMap(reqVo, Industry.class);
        industry.setUpdateBy(reqVo.getOperatorId());
        super.updateById(industry);
    }

    @Override
    public void deleteById(IdReqVo reqVo) {
        Industry industry = ModelMapperUtil.strictMap(reqVo, Industry.class);
        industry.setUpdateBy(reqVo.getOperatorId());
        super.removeById(industry);
    }
}

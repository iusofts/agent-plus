package com.iusofts.agentplus.basic.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * dto转换工具
 *
 * @author Ivan
 * @date 2016年6月21日 下午3:01:43
 */
public class ModelMapperUtil {

    private static ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        //精准匹配
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setAmbiguityIgnored(true);;
    }

    /**
     * dto转换-通用精确匹配
     * (精确匹配是指只copy相同属性名的值)
     * 自定义匹配规则请参考：http://modelmapper.org/getting-started/ 的‘Explicit Mapping’部分
     *
     * @param source
     * @param destinationType
     * @return
     * @author Ivan
     * @date 2016年6月21日 下午2:58:39
     */
    public static <D> D strictMap(Object source, Class<D> destinationType) {
        return modelMapper.map(source, destinationType);
    }

    /**
     * List&lt;dto>转换
     *
     * @param source
     * @param componentType
     * @return
     * @author Ivan
     * @date 2016年6月21日 下午3:58:12
     */
    public static <D> List<D> strictMapList(Collection source, final Class<D> componentType) {
        List<D> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(source)) {
            for (Object obj : source) {
                list.add(modelMapper.map(obj, componentType));
            }
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * Page&lt;dto>转换
     *
     * @param page
     * @param componentType
     * @return
     * @author Ivan
     * @date 2016年8月15日 下午3:58:12
     */
    public static <D> Page<D> strictPage(Page<Object> page, final Class<D> componentType) {
        if (page == null && page.getTotal() < 1) {
            return new Page<>();
        }
        List<D> list = strictMapList(page.getRecords(), componentType);
        Page<D> result = new Page<>(page.getCurrent(), page.getTotal(), page.getTotal());
        result.setRecords(list);
        return result;
    }

    /**
     * Page&lt;dto>转换
     *
     * @param page
     * @param componentType
     * @return
     * @author Ivan
     * @date 2016年8月15日 下午3:58:12
     */
    public static <D> PageResult<D> strictPageResult(PageResult<?> page, final Class<D> componentType) {
        if (page == null && page.getTotalCount() < 1) {
            return new PageResult<>();
        }
        List<D> list = strictMapList(page.getDataList(), componentType);
        PageResult<D> result = new PageResult<>();
        result.setTotalCount(page.getTotalCount());
        result.setDataList(list);
        return result;
    }

}

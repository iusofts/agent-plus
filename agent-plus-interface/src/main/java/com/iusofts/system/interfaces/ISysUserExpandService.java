package com.iusofts.system.interfaces;

import com.iusofts.common.vo.ImportParam;
import com.iusofts.system.dto.SysUserDto;

import java.util.List;

/**
 * @author Ivan Shen
 */
public interface ISysUserExpandService {
    
    List<SysUserDto> getSysUserList(Integer shopId, String roleCode);
    
    void batchImportShopUser(ImportParam param);
    
}

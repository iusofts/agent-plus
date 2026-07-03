package com.iusofts.agentplus.system.interfaces;

import com.iusofts.agentplus.common.vo.ImportParam;
import com.iusofts.agentplus.system.dto.SysUserDto;

import java.util.List;

/**
 * @author Ivan Shen
 */
public interface ISysUserExpandService {
    
    List<SysUserDto> getSysUserList(Integer shopId, String roleCode);
    
    void batchImportShopUser(ImportParam param);
    
}

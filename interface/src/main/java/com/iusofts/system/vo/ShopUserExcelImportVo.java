package com.iusofts.system.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ShopUserExcelImportVo {

     @ExcelProperty("员工姓名")
    private String name;

     @ExcelProperty("账号")
    private String username;

     @ExcelProperty("手机号码")
    private String phone;
    
     @ExcelProperty("角色")
    private String roleName;

}

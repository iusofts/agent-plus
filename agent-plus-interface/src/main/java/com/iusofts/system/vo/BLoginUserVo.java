package com.iusofts.system.vo;

import com.iusofts.system.dto.SysUserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * <p>
 * 后台用户
 * </p>
 *
 * @author Ivan
 * @since 2019-08-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description =  "后台用户登陆信息")
public class BLoginUserVo {
    
    private SysUserDto user;
    private Set<String> roles;
    private Set<String> permissions;
    
}

package com.iusofts.agentplus.web.system.controller;

import com.iusofts.agentplus.basic.annotation.BLoginUser;
import com.iusofts.agentplus.basic.ip.IpUtils;
import com.iusofts.agentplus.system.interfaces.ILoginService;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.system.vo.ImageVerifyCodeVO;
import com.iusofts.agentplus.system.vo.LoginParam;
import com.iusofts.agentplus.system.vo.TokenVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import com.iusofts.agentplus.web.common.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 登录验证
 *
 * @author 
 */
@Tag(name = "登录验证")
@RestController
public class SysLoginController extends BApiController {
    @Autowired
    private ILoginService loginService;

    /**
     * 登录方法
     *
     * @param param 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public TokenVo login(@Validated @RequestBody LoginParam param, HttpServletRequest request) {
        param.setLoginIp(IpUtils.getIpAddr(request));
        return new TokenVo(loginService.login(param));
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public BLoginUserVo getInfo() {
        return SessionUtil.getBSessionUser();
    }

    @Operation(description = "获取用户菜单")
    @GetMapping("getMenuIds")
    public List<Long> getMenuIds(@BLoginUser BLoginUserVo loginUser) {
        Long userId = loginUser.getUser().getUserId();
        return loginService.getMenuIds(userId);
    }

    @Operation(description = "登出")
    @PostMapping("/logout")
    public void logout() {
        SessionUtil.invalidate();
    }

    @Operation(description = "客户端获取图片验证码")
    @RequestMapping(value = "/imageVerifyCode", method = RequestMethod.POST)
    public ImageVerifyCodeVO getImageVerifyCode() {
        return loginService.getImageVerifyCode();
    }

}

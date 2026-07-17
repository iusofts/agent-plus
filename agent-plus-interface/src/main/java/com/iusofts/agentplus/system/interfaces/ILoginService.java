/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/5/14
 * Description:ILoginService.java
 */
package com.iusofts.agentplus.system.interfaces;

import com.iusofts.agentplus.system.vo.ImageVerifyCodeVO;
import com.iusofts.agentplus.system.vo.LoginParam;

import java.util.List;

/**
 * @author Ivan Shen
 */
public interface ILoginService {

    String login(LoginParam param);

    ImageVerifyCodeVO getImageVerifyCode();

    boolean verifyImageCode(String imageId, String code);

    List<Long> getMenuIds(Long userId);

    /**
     * 刷新用户登录缓存
     * @param userId 用户ID
     * @param token 当前token
     */
    void refreshLoginUserCache(Long userId, String token);
}

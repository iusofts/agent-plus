/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/5/14
 * Description:ILoginService.java
 */
package com.iusofts.system.interfaces;

import com.iusofts.system.vo.ImageVerifyCodeVO;
import com.iusofts.system.vo.LoginParam;

import java.util.List;

/**
 * @author Ivan Shen
 */
public interface ILoginService {
    
    String login(LoginParam param);

    ImageVerifyCodeVO getImageVerifyCode();

    boolean verifyImageCode(String imageId, String code);
    
    List<Long> getMenuIds(Long userId);
}

package com.iusofts.agentplus.basic.web.captcha;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;

/**
 * 验证码生成工具 
 * @引用 EasyCaptcha
 * @author Ivan Shen
 */
@Slf4j
public class EasyCaptchaImgCodeUtil {

    /**
     * 获取数字字母验证码 默认5位
     * @return
     */
    public static ImgIdentifyingCodeVO getImgIdentifyingCode() {
        ImgIdentifyingCodeVO imgIdentifyingCode = new ImgIdentifyingCodeVO();
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        String verCode = specCaptcha.text().toLowerCase();
        log.info(verCode);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        specCaptcha.out(outputStream);
        imgIdentifyingCode.setImg(specCaptcha.toBase64());
        imgIdentifyingCode.setImgCode(verCode);
        return imgIdentifyingCode;
    }

    /**
     * 获取计算公式验证码 默认两位
     * @return
     */
    public static ImgIdentifyingCodeVO getImgEquationCode() {
        ImgIdentifyingCodeVO imgIdentifyingCode = new ImgIdentifyingCodeVO();
        ArithmeticCaptcha specCaptcha = new ArithmeticCaptcha(130, 48);
        String verCode = specCaptcha.getArithmeticString();
        log.info(verCode);
        imgIdentifyingCode.setImg(specCaptcha.toBase64());
        imgIdentifyingCode.setImgCode(specCaptcha.text());
        return imgIdentifyingCode;
    }
    
}

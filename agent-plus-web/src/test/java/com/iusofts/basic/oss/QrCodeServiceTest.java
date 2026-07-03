package com.iusofts.basic.oss;

import com.iusofts.basic.qrcode.QrCodeGenerator;
import common.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * Created by Ivan on 2019/11/21.
 */
public class QrCodeServiceTest extends BaseTest {

    @Autowired
    private HandleService handleService;

    @Test
    public void test() {
        ByteArrayInputStream inputStream = QrCodeGenerator.getQrCodeImage("13246789", 350, 350);
        String url = handleService.putOssObject("xxxx", "qrcode/" + UUID.randomUUID().toString() + ".png", inputStream);
        System.err.println(url);
    }

}
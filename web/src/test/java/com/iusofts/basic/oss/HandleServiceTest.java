package com.iusofts.basic.oss;

import common.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.UUID;

/**
 * Created by Ivan on 2019/11/21.
 */
public class HandleServiceTest extends BaseTest {

    @Autowired
    private HandleService handleService;

    @Test
    public void putOssObject() {
        String url = handleService.putOssObject("xxxx", "xxxxx/" + UUID.randomUUID().toString() + ".png", new File("C:\\Users\\xxxx\\Desktop\\xxxx\\素材\\fa_02.png"));
        System.err.println(url);
    }

    @Test
    public void putOssObject1() {
    }
}
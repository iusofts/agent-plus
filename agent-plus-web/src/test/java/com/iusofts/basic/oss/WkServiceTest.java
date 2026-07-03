package com.iusofts.basic.oss;

import com.iusofts.basic.wk.processor.WkProcessor;
import common.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * Created by Ivan on 2019/11/21.
 */
public class WkServiceTest extends BaseTest {

    @Autowired
    private HandleService handleService;

    @Autowired
    private WkProcessor wkProcessor;

    @Test
    public void test() {
        String res = wkProcessor.convertImg("http://www.baidu.com", null, 1400, null, null, null);
        System.err.println(res);
        File file = new File(res);
        String url = handleService.putOssObject("xxxxx", "tmp/" + file.getName(), file);
        System.err.println(url);
    }

    @Test
    public void testConvertPdf() {
        String pdf = wkProcessor.convertPdf("http://localhost:8080/xxxxx?xxId=1017188");
        System.out.println(pdf);
    }

}
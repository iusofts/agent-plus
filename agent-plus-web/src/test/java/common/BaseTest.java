package common;

import com.iusofts.agentplus.web.Application;
import com.iusofts.agentplus.web.config.DatabaseConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import({DatabaseConfig.class})
@ComponentScan(basePackages = {"com.iusofts"})
// @Rollback(false) 开启提交事务
public class BaseTest {

    @Test
    public void testStart() {
        System.out.println("test run success !!!");
    }

}
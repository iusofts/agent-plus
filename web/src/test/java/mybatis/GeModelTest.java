package mybatis;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.apache.ibatis.type.JdbcType;
import org.junit.Test;

/**
 * mybatis plus 3.5.x 代码生成器（适配3.5.0+版本）
 * 核心：废弃AutoGenerator，使用FastAutoGenerator简化配置
 */
public class GeModelTest {

    @Test
    public void getModel() {
        // 数据库连接配置（保留原配置，仅调整驱动类为8.x兼容版）
        String url = "jdbc:mysql://127.0.0.1:3306/agent-plus?zeroDateTimeBehavior=convertToNull&useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8";
        String username = "root";
        String password = "123456";
        String driver = "com.mysql.cj.jdbc.Driver"; // 3.5.x建议用8.x驱动，兼容低版本MySQL

        // 核心生成器：FastAutoGenerator（3.5.x主推）
        FastAutoGenerator.create(url, username, password)
                // 1. 全局配置（对应原GlobalConfig，移除了过时的ActiveRecord等配置）
                .globalConfig((scanner, builder) -> builder
                        .outputDir("C:\\Users\\Ivan\\Desktop\\agent-plus") // 生成目录（保留你的原路径）
                        .author("Ivan") // 作者（保留你的原配置）
                        .disableOpenDir() // 生成后不打开文件夹（可选，建议添加）
                        .dateType(DateType.TIME_PACK) // 日期类型策略（保留原配置）
                        .enableSpringdoc() // 启用Springdoc
                        .commentDate("yyyy-MM-dd") // 注释日期格式（可选优化）
                        .build()
                )
                // 2. 数据源配置（对应原DataSourceConfig，自定义字段类型转换）
                .dataSourceConfig((builder) -> builder
                        .dbQuery(new MySqlQuery()) // MySQL数据库查询器
                        .typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                                    // 兼容旧版本转换成Integer
                                    if (JdbcType.TINYINT == metaInfo.getJdbcType()) {
                                        return DbColumnType.INTEGER;
                                    }
                                    return typeRegistry.getColumnType(metaInfo);
                                })
                        .driverClassName(driver) // 数据库驱动
                        .build()
                )
                // 3. 包配置（对应原PackageConfig，保留原父包配置）
                .packageConfig((scanner, builder) -> builder
                        .parent("com.iusofts.agentplus.chat") // 父包名（保留你的原配置）
                        .entity("entity") // 实体类包名（默认，可自定义）
                        .mapper("mapper") // Mapper接口包名（默认）
                        .service("service") // Service接口包名（默认）
                        .serviceImpl("service.impl") // Service实现类包名（默认）
                        .controller("controller") // 控制器包名（默认）
                        .xml("mapper.xml") // MapperXML文件包名（默认）
                        .moduleName("") // 模块名（无则留空，原配置无）
                        .build()
                )
                // 4. 策略配置（对应原StrategyConfig，保留所有业务配置）
                .strategyConfig((scanner, builder) -> builder
                        .addInclude("ai_conversation","ai_message") // 指定生成的表
                        .addTablePrefix("t_") // 表前缀（生成实体时移除，保留原配置）
                         // 修正：表名、字段名转驼峰配置（3.5.15需通过builder指定，而非直接链式调用）
                        .entityBuilder()
                        .logicDeleteColumnName("delete_flag")
                        .fieldUseJavaDoc(false)
                        .naming(NamingStrategy.underline_to_camel) // 表名：下划线转驼峰（正确写法）
                        // 实体类策略
                        .entityBuilder()
                        .enableLombok() // 启用Lombok（保留原配置）
                        //.enableTableFieldAnnotation() // 生成字段注解（@TableField，可选优化）
                        // Controller策略
                        .controllerBuilder()
                        .enableRestStyle() // 生成RestController（@RestController，可选优化，替代原普通Controller）
                        .enableHyphenStyle() // URL中驼峰转连字符（如userInfo -> user-info，可选）
                        // Mapper策略
                        .mapperBuilder()
                        .enableBaseResultMap() // 生成BaseResultMap（保留原BaseResultMap）
                        .enableBaseColumnList() // 生成BaseColumnList（保留原BaseColumnList）
                        .build()
                )
                // 5. 模板引擎（3.5.x需要显式指定，推荐Freemarker，也可用Velocity）
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行生成
                .execute();
    }
}
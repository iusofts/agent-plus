package com.iusofts.web.config;

import com.iusofts.basic.validation.YzValidated;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

import static com.iusofts.basic.constants.CacheConstants.C_LOGIN_TOKEN_HEAD;
import static com.iusofts.basic.constants.CacheConstants.LOGIN_TOKEN_HEAD;

@Configuration
public class SpringDocConfig {

    // 从配置文件读取前缀
    @Value("${springdoc.server.prefix:}")
    private String serverPrefix;

    // ==================== 客户端API分组（面向前端/移动端） ====================
    @Bean
    public GroupedOpenApi clientApi(OperationCustomizer clientHeaderCustomizer) {
        // 匹配客户端接口路径（可根据实际情况调整，支持通配符）
        String[] paths = {"/api/**"};
        // 排除不需要的路径（可选）
        String[] excludedPaths = {"/api/internal/**"};

        return GroupedOpenApi.builder()
                // 分组名称（唯一标识）
                .group("api")
                // 匹配的接口路径
                .pathsToMatch(paths)
                // 排除的接口路径
                .pathsToExclude(excludedPaths)
                .addOperationCustomizer(clientHeaderCustomizer) // 绑定客户端Header
                .addOpenApiCustomizer(openApi -> {
                    openApi.info(clientApiInfo());
                    // 定义环境服务器URL
                    String serverUrl = serverPrefix;
                    Server server = new Server();
                    server.setUrl(serverUrl);
                    server.setDescription("服务");
                    openApi.servers(List.of(server));
                })
                .build();
    }

    // ==================== 后台API分组（面向运营/管理员） ====================
    @Bean
    public GroupedOpenApi adminApi(OperationCustomizer adminHeaderCustomizer) {
        // 匹配后台接口路径
        String[] paths = {"/bapi/**"};

        return GroupedOpenApi.builder()
                .group("bapi")
                .pathsToMatch(paths)
                .addOperationCustomizer(adminHeaderCustomizer) // 绑定客户端Header
                .addOpenApiCustomizer(openApi -> {
                    openApi.info(adminApiInfo());
                    // 定义环境服务器URL
                    String serverUrl = serverPrefix;
                    Server server = new Server();
                    server.setUrl(serverUrl);
                    server.setDescription("服务");
                    openApi.servers(List.of(server));
                })
                .build();
    }


    // ========== 1. 客户端API专属Header定制器 ==========
    @Bean
    public OperationCustomizer clientHeaderCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            Parameter tokenHeader = new HeaderParameter()
                    .name(C_LOGIN_TOKEN_HEAD)
                    .description("客户端用户登录令牌")
                    .required(false)
                    .schema(new StringSchema().example("9165c1a2-5f1f-4a7d-a6f8-f78e1a9f93e3"));
            operation.addParametersItem(tokenHeader);
            return operation;
        };
    }

    // ========== 2. 后台API专属Header定制器 ==========
    @Bean
    public OperationCustomizer adminHeaderCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            Parameter adminTokenHeader = new HeaderParameter()
                    .name(LOGIN_TOKEN_HEAD)
                    .description("后台管理登录令牌")
                    .required(true) // 后台接口强制要求传
                    .schema(new StringSchema().example("8b7e4d9c-3a8f-4e7b-9d8c-8e7d6f5a4b3c"));
            operation.addParametersItem(adminTokenHeader);
            return operation;
        };
    }
    
    public Info clientApiInfo() {
        return new Info()
                .title("客户端接口文档")
                .version("1.0")
                .description("调试时Header参数需要填写");
    }

    public Info adminApiInfo() {
        return new Info()
                .title("后台接口文档")
                .version("1.0")
                .description("调试时Header参数需要填写");
    }



    @Component
    public class ParamSpringDocCustomizer implements ParameterCustomizer {
        @Override
        public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
            if (methodParameter.hasParameterAnnotation(YzValidated.class) 
                    || methodParameter.hasParameterAnnotation(RequestBody.class) 
                    || methodParameter.hasParameterAnnotation(PathVariable.class) 
                    || methodParameter.hasParameterAnnotation(RequestParam.class)) {
                return parameterModel;
            }
            return null;
        }
    }


}

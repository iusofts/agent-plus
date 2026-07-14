package com.iusofts.agentplus.web.monitor.controller;

import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.web.common.controller.BApiController;
import com.iusofts.agentplus.web.common.domain.Server;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;

/**
 * 服务器监控
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/bapi/monitor/server")
public class ServerController extends BApiController
{
    @Permission("monitor:server:list")
    @OperationLogExclude(type = RES)
    @Operation(summary = "获取服务器监控信息")
    @GetMapping()
    public Server getInfo() throws Exception
    {
        Server server = new Server();
        server.copyTo();
        return server;
    }
}

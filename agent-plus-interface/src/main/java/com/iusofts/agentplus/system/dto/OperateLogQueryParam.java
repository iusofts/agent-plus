package com.iusofts.agentplus.system.dto;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志查询参数
 *
 * @author Ivan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "操作日志查询参数")
public class OperateLogQueryParam extends PageQuery {

    @Schema(description = "日志主键")
    private Integer id;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "操作类别（0其它 1后台用户 2手机端用户）")
    private Integer operatorType;

    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    @Schema(description = "操作人ip")
    private String operIp;

    @Schema(description = "请求开始时间")
    private LocalDateTime requestTimeStart;

    @Schema(description = "请求结束时间")
    private LocalDateTime requestTimeEnd;

}

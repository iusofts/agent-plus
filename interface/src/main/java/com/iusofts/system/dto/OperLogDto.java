package com.iusofts.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志记录
 * </p>
 *
 * @author Ivan
 * @since 2020-12-09
 */
@Data
@Schema(description = "操作日志记录")
public class OperLogDto {

    @Schema(description = "日志主键")
    private Integer id;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "业务类型（0其它 1新增 2修改 3删除）")
    private Integer businessType;

    @Schema(description = "方法名称")
    private String method;

    @Schema(description = "请求方式")
    private String requestMethod;

    @Schema(description = "操作类别（0其它 1后台用户 2手机端用户）")
    private Integer operatorType;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "主机地址")
    private String operIp;

    @Schema(description = "用户身份令牌")
    private String token;

    @Schema(description = "操作地点")
    private String operLocation;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回参数")
    private String jsonResult;

    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "操作时间")
    private LocalDateTime operTime;

    @Schema(description = "执行时间(单位毫秒)")
    private Integer executeTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}

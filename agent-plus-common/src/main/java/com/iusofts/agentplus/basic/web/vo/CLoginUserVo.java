package com.iusofts.agentplus.basic.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description="客户端登录用户vo")
public class CLoginUserVo {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "经验值")
    private Integer exp;
    
    @Schema(description = "升级所需经验值")
    private Integer needExp;

    @Schema(description = "已经最高等级")
    private boolean maxLevel;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "会员类型 （1.个人会员 2.企业会员）")
    private Integer vipType;

    @Schema(description = "所属组织id")
    private Integer orgId;

    @Schema(description = "所属组织名称")
    private String orgName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "有效期")
    private LocalDateTime expirationDate;

    @Schema(description = "应用版本")
    private String appVersion;

    @Schema(description = "平台 1.win64 2.mac x64 3.mac arm64")
    private Integer platform;

    @Schema(description = "操作系统")
    private String osName;

    @Schema(description = "是否初始化")
    private boolean init;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最后登录时间")
    private LocalDateTime loginDate;

    @Schema(description = "行业id")
    private Long industryId;

    @Schema(description = "行业名称")
    private String industryName;

    @Schema(description = "版本")
    private String edition;

}

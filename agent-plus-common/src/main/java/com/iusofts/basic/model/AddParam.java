package com.iusofts.basic.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 胡前培
 * @version 1.0.0
 * @ClassName AddParam
 * @Description TODO  新增公共参数
 * @createTime 2021/12/13 14:33
 */

@Data
public class AddParam {

    @Schema(description = "创建人id")
    private Integer createrId;

    @Schema(description = "创建人姓名")
    private String createrName;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新人id", hidden = true)
    private Integer updaterId;

    @Schema(description = "更新人名称", hidden = true)
    private String updaterName;


}

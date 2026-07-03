/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/5/15
 * Description:IdReqVo.java
 */
package com.iusofts.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Ivan Shen
 */
@Data
public class IdsReqVo {

    @Schema(description = "唯一标识ID")
    private List<Long> ids;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人")
    private Long operatorName;

    @Schema(description = "所属组织ID")
    private Integer orgId;

    public IdsReqVo() {
    }

    public IdsReqVo(List<Long> ids, Long operatorId) {
        this.ids = ids;
        this.operatorId = operatorId;
    }

    public IdsReqVo(List<Long> ids, Integer orgId) {
        this.ids = ids;
        this.orgId = orgId;
    }

    public IdsReqVo(List<Long> ids, Long operatorId, Integer orgId) {
        this.ids = ids;
        this.operatorId = operatorId;
        this.orgId = orgId;
    }
}

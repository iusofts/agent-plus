/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/5/15
 * Description:IdReqVo.java
 */
package com.iusofts.agentplus.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Ivan Shen
 */
@Data
public class IdReqVo {

    @Schema(description = "唯一标识ID")
    private Long id;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

    @Schema(description = "操作人", hidden = true)
    private Long operatorName;

    @Schema(description = "所属组织ID", hidden = true)
    private Integer orgId;

    public IdReqVo() {
    }

    public IdReqVo(Long id, Long operatorId) {
        this.id = id;
        this.operatorId = operatorId;
    }

    public IdReqVo(Long id, Integer orgId) {
        this.id = id;
        this.orgId = orgId;
    }

    public IdReqVo(Long id, Long operatorId, Integer orgId) {
        this.id = id;
        this.operatorId = operatorId;
        this.orgId = orgId;
    }
    
    public static IdReqVo buildSysParamVo(Long id) {
        return new IdReqVo(id, 0L, null);
    }
    
}

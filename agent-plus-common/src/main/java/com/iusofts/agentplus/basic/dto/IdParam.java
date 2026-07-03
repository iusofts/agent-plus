package com.iusofts.agentplus.basic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * @author Ivan Shen
 */
@Data
public class IdParam {

    @NotNull(message = "ID不能为空")
    private Integer id;
}

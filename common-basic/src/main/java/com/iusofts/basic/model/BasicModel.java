package com.iusofts.basic.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.iusofts.basic.company.CompanyIdentityContextHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BasicModel<A extends BasicModel<A>> extends Model<A> {

    @Schema(description = "公司id")
    private Integer companyId;

    public BasicModel() {
        this.companyId = CompanyIdentityContextHolder.getCompanyId();
    }

}

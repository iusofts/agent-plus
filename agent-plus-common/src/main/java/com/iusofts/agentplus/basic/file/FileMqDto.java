package com.iusofts.agentplus.basic.file;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件Mq对象
 *
 * @author xuekchen
 */
@Data
@Schema(description = "文件Mq对象")
public class FileMqDto implements Serializable {

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件地址")
    private String url;

}

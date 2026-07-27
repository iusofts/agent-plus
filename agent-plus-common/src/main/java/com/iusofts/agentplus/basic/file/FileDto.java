package com.iusofts.agentplus.basic.file;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件对象
 *
 * @author Ivan
 */
@Data
@Schema(description = "文件对象")
public class FileDto {

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件访问地址")
    private String url;

    @Schema(description = "文件类型，例如Image、File、Video等")
    private String type;

    @Schema(description = "文件大小，单位：字节")
    private Long size;

}
package com.iusofts.basic.file;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件对象
 *
 * @author xuekchen
 */
@Data
@Schema(description = "文件对象")
public class FileDto {

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件地址")
    private String url;

}

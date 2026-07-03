package com.iusofts.basic.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 图片数据
 *
 * @author Ivan Shen
 */
@Data
public class ImageAndLabelDto extends ImageDto {

    @Schema(description = "图片标签")
    private List<ImageLabel> labelList;

}

package com.iusofts.agentplus.basic.file;

import com.iusofts.agentplus.basic.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * 图片工具
 *
 * @author Ivan Shen
 */
public class ImageDtoUtil {

    private static final Logger log = LoggerFactory.getLogger(ImageDtoUtil.class);

    public static ImageDto toObj(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return JsonUtils.json2obj(json, ImageDto.class);
            } catch (Exception e) {
                log.warn("图片JSON转换失败", e);
            }
        }
        return null;
    }

    public static List<ImageDto> toObjList(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                List<ImageDto> imageDtos = JsonUtils.json2list(json, ImageDto.class);
                imageDtos.sort(Comparator.comparing(ImageDto::getIsFirst).reversed());
                return imageDtos;
            } catch (Exception e) {
                log.warn("图片JSON转换失败", e);
            }
        }
        return null;
    }

    public static ImageDto getFirstImage(List<ImageDto> imageDtos) {
        if (CollectionUtils.isNotEmpty(imageDtos)) {
            imageDtos.sort(Comparator.comparing(ImageDto::getIsFirst).reversed());
            return imageDtos.get(0);
        }
        return null;
    }

    public static String getFirstImageUrl(List<ImageDto> imageDtos) {
        ImageDto firstImage = getFirstImage(imageDtos);
        if (firstImage != null) {
            return firstImage.getUrl();
        }
        return null;
    }

    public static String toJson(ImageDto imageDto) {
        if (imageDto != null) {
            return JsonUtils.obj2json(imageDto);
        }
        return null;
    }

    public static String toJson(List<?> imageDtos) {
        if (CollectionUtils.isNotEmpty(imageDtos)) {
            return JsonUtils.obj2json(imageDtos);
        }
        return null;
    }

}

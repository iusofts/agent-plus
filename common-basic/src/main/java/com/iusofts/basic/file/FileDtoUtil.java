package com.iusofts.basic.file;

import com.iusofts.basic.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 文件工具
 *
 * @author Ivan Shen
 */
public class FileDtoUtil {

    private static final Logger log = LoggerFactory.getLogger(FileDtoUtil.class);

    public static FileDto toObj(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return JsonUtils.json2obj(json, FileDto.class);
            } catch (Exception e) {
                log.warn("文件JSON转换失败", e);
            }
        }
        return null;
    }

    public static List<FileDto> toObjList(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                List<FileDto> fileDtos = JsonUtils.json2list(json, FileDto.class);
                return fileDtos;
            } catch (Exception e) {
                log.warn("文件JSON转换失败", e);
            }
        }
        return null;
    }

    public static String toJson(FileDto fileDto) {
        if (fileDto != null) {
            return JsonUtils.obj2json(fileDto);
        }
        return null;
    }

    public static String toJson(List<?> fileDtos) {
        if (CollectionUtils.isNotEmpty(fileDtos)) {
            return JsonUtils.obj2json(fileDtos);
        }
        return null;
    }

}

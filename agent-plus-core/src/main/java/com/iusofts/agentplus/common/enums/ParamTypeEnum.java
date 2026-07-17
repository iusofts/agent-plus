package com.iusofts.agentplus.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 参数类型枚举.
 *
 * @author Ivan
 */
@Getter
@RequiredArgsConstructor
public enum ParamTypeEnum {

    STRING("String"),
    FILE("File"),
    FILE_IMAGE("Image"),
    FILE_DOC("Doc"),
    FILE_CODE("Code"),
    FILE_PPT("PPT"),
    FILE_TXT("TXT"),
    FILE_EXCEL("Excel"),
    FILE_AUDIO("Audio"),
    FILE_ZIP("Zip"),
    FILE_VIDEO("Video"),
    INTEGER("Integer"),
    NUMBER("Number"),
    OBJECT("Object"),
    ARRAY("Array"),
    BOOLEAN("Boolean");

    private final String value;

    public static ParamTypeEnum fromValue(String value) {
        for (ParamTypeEnum typeEnum : values()) {
            if (typeEnum.getValue().equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static List<Map<String, Object>> getTypeTree() {
        return List.of(
                typeNode("String", "String", null),
                typeNode("File", "File", List.of(
                        typeNode("File", "File", null),
                        typeNode("Image", "Image", null),
                        typeNode("Doc", "Doc", null),
                        typeNode("Code", "Code", null),
                        typeNode("PPT", "PPT", null),
                        typeNode("TXT", "TXT", null),
                        typeNode("Excel", "Excel", null),
                        typeNode("Audio", "Audio", null),
                        typeNode("Zip", "Zip", null),
                        typeNode("Video", "Video", null)
                )),
                typeNode("Integer", "Integer", null),
                typeNode("Number", "Number", null),
                typeNode("Object", "Object", null),
                typeNode("Array", "Array", null),
                typeNode("Boolean", "Boolean", null)
        );
    }

    private static Map<String, Object> typeNode(String label, String value, List<Map<String, Object>> children) {
        if (children == null || children.isEmpty()) {
            return Map.of("label", label, "value", value, "children", List.of());
        }
        return Map.of("label", label, "value", value, "children", children);
    }
}

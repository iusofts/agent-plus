package com.iusofts.basic.fasterxml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号脱敏序列化器：将11位手机号中间四位替换为****
 */
public class PhoneDesensitizationSerializer extends JsonSerializer<String> {

    // 手机号正则表达式（匹配11位数字手机号）
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");

    @Override
    public void serialize(String phone, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (phone == null) {
            // 手机号为空时直接返回null
            jsonGenerator.writeNull();
            return;
        }
        // 匹配手机号格式并替换中间四位
        Matcher matcher = PHONE_PATTERN.matcher(phone);
        if (matcher.matches()) {
            // 格式正确：保留前3位和后4位，中间替换为****
            String maskedPhone = matcher.replaceAll("$1****$2");
            jsonGenerator.writeString(maskedPhone);
        } else {
            // 格式不正确：返回原字符串（也可根据需求抛异常/返回空）
            jsonGenerator.writeString(phone);
        }
    }
}
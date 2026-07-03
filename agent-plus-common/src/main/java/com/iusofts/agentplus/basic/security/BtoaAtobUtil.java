package com.iusofts.agentplus.basic.security;

import java.util.regex.Matcher;

import java.util.regex.Pattern;


public class BtoaAtobUtil {
    //btoa加密方法
    public static String base64hash = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static void main(String args[]) {
        String rs = bota("/constructionDetail?deliveryId=100");
        System.out.println(rs);
        rs = atob("L2NvbnN0cnVjdGlvbkRldGFpbD90b2tlbj0yYzg5YjA2ZC00YTk5LTQ2OGUtOGUwMC1lMzQwNDQ4NTU4YjQmZGVsaXZlcnlJZD0xMDA=");
        System.out.println(rs);
    }

    /*
            str:要匹配的数据
            reg：正则匹配的规则
         */
    public static boolean isMatcher(String str, String reg) {
        //编译成一个正则表达式模式
        Pattern pattern = Pattern.compile(reg);
        //匹配模式
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static String bota(String str) {
        if (str == null || isMatcher(str, "([^\\u0000-\\u00ff])")) {
            throw new Error("INVALID_CHARACTER_ERR");
        }
        int i = 0, prev = 0, ascii, mod = 0;
        StringBuilder result = new StringBuilder();
        while (i < str.length()) {
            ascii = str.charAt(i);
            mod = i % 3;
            switch (mod) {
                //第一个6位只需要让8位二进制右移两位
                case 0:
                    result.append(base64hash.charAt(ascii >> 2));
                    break;
                //第二个6位=第一个8位的后两位+第二个八位的前四位
                case 1:
                    result.append(base64hash.charAt((prev & 3) << 4 | (ascii >> 4)));
                    break;
                //第三个6位=第二个8位的后四位+第三个8位的前两位
                //第四个6位 = 第三个8位的后6位
                case 2:
                    result.append(base64hash.charAt((prev & 0x0f) << 2 | (ascii >> 6)));
                    result.append(base64hash.charAt(ascii & 0x3f));
                    break;
            }
            prev = ascii;
            i++;
        }
        //循环结束后看mod, 为0 证明需补3个6位，第一个为最后一个8位的最后两位后面补4个0。另外两个6位对应的是异常的“=”；
        // mod为1，证明还需补两个6位，一个是最后一个8位的后4位补两个0，另一个对应异常的“=”
        if (mod == 0) {
            result.append(base64hash.charAt((prev & 3) << 4));
            result.append("==");
        } else if (mod == 1) {
            result.append(base64hash.charAt((prev & 0x0f) << 2));
            result.append("=");
        }

        return result.toString();

    }


    /**
     * // atob method
     * // 逆转encode的思路即可
     *
     * @param inStr
     * @return
     */
    public static String atob(String inStr) {
        if (inStr == null) {
            return null;
        }
        inStr = inStr.replaceAll("\\s|=", "");
        StringBuilder result = new StringBuilder();

        int cur;
        int prev = -1;
        int mod;
        int i = 0;

        while (i < inStr.length()) {
            cur = base64hash.indexOf(inStr.charAt(i));
            mod = i % 4;
            switch (mod) {
                case 0:
                    break;
                case 1:
                    result.append(String.valueOf((char) (prev << 2 | cur >> 4)));
                    break;
                case 2:
                    result.append(String.valueOf((char) ((prev & 0x0f) << 4 | cur >> 2)));
                    break;
                case 3:
                    result.append(String.valueOf((char) ((prev & 3) << 6 | cur)));
                    break;
            }
            prev = cur;
            i++;
        }
        return result.toString();
    }
}
package com.iusofts.agentplus.basic.wk.processor;

import com.iusofts.agentplus.basic.wk.WkProperties;
import com.iusofts.agentplus.basic.web.vo.CookieParam;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.wk.interceptor.HtmlToPdfInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class WkProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WkProperties wkProperties;

    private final String fileSeparator = "/";

    private final String PDFSUFFIX = ".pdf";
    private final String IMGSUFFIX = ".jpg";

    /**
     * html转pdf
     *
     * @param srcPath html路径，网络路径 www.baidu.com
     * @return 转换成功返回true
     */
    public String convert(String srcPath, String command, String dir, String suffix) {
        if (!dir.endsWith(fileSeparator)) {
            dir = dir + fileSeparator;
        }
        String destPath = dir + UUID.randomUUID() + suffix;
        File file = new File(destPath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if (!parent.exists()) {
            parent.mkdirs();
        }
        StringBuilder cmd = new StringBuilder();
        cmd.append(wkProperties.getToimgtool());
        cmd.append(command);
        cmd.append(" ");
        cmd.append("--javascript-delay 3600");// 前台加载js延迟参数
        cmd.append(" ");
        cmd.append(srcPath);
        cmd.append(" ");
        cmd.append(destPath);

        try {
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(
                    proc.getErrorStream());
            HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(
                    proc.getInputStream());
            error.start();
            output.start();
            proc.waitFor();
            logger.info("HTML2PDF成功，参数---html路径：{},pdf保存路径 ：{}", new Object[]{srcPath, destPath});
        } catch (Exception e) {
            logger.error("HTML2PDF失败，srcPath地址：{},错误信息：{}", new Object[]{srcPath, e.getMessage()});
            throw new SystemBusinessException("wkhtmltopdf出现异常");
        }
        return destPath;
    }

    public String convertImg(String url, Integer second, Integer width, Integer height, String cookieName, String cookieValue) {
        String dir = wkProperties.getImgDir();
        if (!dir.endsWith(fileSeparator)) {
            dir = dir + fileSeparator;
        }
        String destPath = dir + UUID.randomUUID() + IMGSUFFIX;
        File file = new File(destPath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if (!parent.exists()) {
            parent.mkdirs();
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(wkProperties.getToimgtool());
        cmd.append(" ");
        cmd.append("--disable-smart-width ");
        if (width != null) {
            cmd.append(" ");
            cmd.append("--width " + width);
        }
        if (height != null) {
            cmd.append(" ");
            cmd.append("--height " + height);
        }
        if (second != null) {
            cmd.append(" ");
            cmd.append("--javascript-delay " + second);// 前台加载js延迟参数
        }
        if (StringUtils.isNotBlank(cookieName) && StringUtils.isNotBlank(cookieValue)) {
            cmd.append(" ");
            cmd.append("--cookie " + cookieName + " " + cookieValue);// 前台加载js延迟参数
        }
        cmd.append(" ");
        cmd.append(url);
        cmd.append(" ");
        cmd.append(destPath);

        try {
            logger.info("wkhtmltoImg 命令行：{}", cmd.toString());
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(
                    proc.getErrorStream());
            HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(
                    proc.getInputStream());
            error.start();
            output.start();
            proc.waitFor();
            logger.info("wkhtmltoImg成功，参数---html路径：{},img保存路径 ：{}", new Object[]{url, destPath});
        } catch (Exception e) {
            logger.error("wkhtmltoImg失败，url地址：{},错误信息：{}", new Object[]{url, e.getMessage()});
            throw new SystemBusinessException("wkhtmltoImg出现异常");
        }
        return destPath;
    }

    public String convertPdf(String url) {
        return convertPdf(url, null);
    }

    public String convertPdf(String url, List<CookieParam> cookieParams) {
        String dir = wkProperties.getPdfDir();
        if (!dir.endsWith(fileSeparator)) {
            dir = dir + fileSeparator;
        }
        String destPath = dir + UUID.randomUUID() + PDFSUFFIX;
        File file = new File(destPath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if (!parent.exists()) {
            parent.mkdirs();
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(wkProperties.getTopdftool());
        if (CollectionUtils.isNotEmpty(cookieParams)) {//拼接cookie参数
            for (CookieParam cookieParam : cookieParams) {
                cmd.append(" ");
                cmd.append("--cookie " + cookieParam.getName() + " " + cookieParam.getValue());// 参数
            }
        }

        cmd.append(" ");
        cmd.append("--page-size A4");// 参数
        cmd.append(" ");
        cmd.append("--javascript-delay 3600");// 前台加载js延迟参数
        cmd.append(" ");
        cmd.append(url);
        cmd.append(" ");
        cmd.append(destPath);

        try {
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(
                    proc.getErrorStream());
            HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(
                    proc.getInputStream());
            error.start();
            output.start();
            proc.waitFor();
            logger.info("HTML2PDF成功，参数---html路径：{},pdf保存路径 ：{}", new Object[]{url, destPath});
        } catch (Exception e) {
            logger.error("HTML2PDF失败，url地址：{},错误信息：{}", new Object[]{url, e.getMessage()});
            throw new SystemBusinessException("wkhtmltopdf出现异常");
        }
        return destPath;
    }

    public String convertTransversePdf(String url) {
        return convertTransversePdf(url, null);
    }

    public String convertTransversePdf(String url,List<CookieParam> cookieParams) {
        String dir = wkProperties.getPdfDir();
        if (!dir.endsWith(fileSeparator)) {
            dir = dir + fileSeparator;
        }
        String destPath = dir + UUID.randomUUID() + PDFSUFFIX;
        File file = new File(destPath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if (!parent.exists()) {
            parent.mkdirs();
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(wkProperties.getTopdftool());
        if (CollectionUtils.isNotEmpty(cookieParams)) {//拼接cookie参数
            for (CookieParam cookieParam : cookieParams) {
                cmd.append(" ");
                cmd.append("--cookie " + cookieParam.getName() + " " + cookieParam.getValue());// 参数
            }
        }

        cmd.append(" ");
        cmd.append("--page-size A4");// 参数
        cmd.append(" ");
        cmd.append("--javascript-delay 3600");// 前台加载js延迟参数
        cmd.append(" ");
        cmd.append("-O landscape");
        cmd.append(" ");
        cmd.append(url);
        cmd.append(" ");
        cmd.append(destPath);

        try {
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(
                    proc.getErrorStream());
            HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(
                    proc.getInputStream());
            error.start();
            output.start();
            proc.waitFor();
            logger.info("HTML2PDF成功，参数---html路径：{},pdf保存路径 ：{}", new Object[]{url, destPath});
        } catch (Exception e) {
            logger.error("HTML2PDF失败，url地址：{},错误信息：{}", new Object[]{url, e.getMessage()});
            throw new SystemBusinessException("wkhtmltopdf出现异常");
        }
        return destPath;
    }
}

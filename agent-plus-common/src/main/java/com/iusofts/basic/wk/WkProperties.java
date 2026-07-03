package com.iusofts.basic.wk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wk")
public class WkProperties {

    //wkhtmltopdf
    private String topdftool;

    //pdf生成路径
    private String pdfDir;

    //wkhtmltoimg
    private String toimgtool;

    //img生成路径
    private String imgDir;

    public String getTopdftool() {
        return topdftool;
    }

    public void setTopdftool(String topdftool) {
        this.topdftool = topdftool;
    }

    public String getPdfDir() {
        return pdfDir;
    }

    public void setPdfDir(String pdfDir) {
        this.pdfDir = pdfDir;
    }

    public String getToimgtool() {
        return toimgtool;
    }

    public void setToimgtool(String toimgtool) {
        this.toimgtool = toimgtool;
    }

    public String getImgDir() {
        return imgDir;
    }

    public void setImgDir(String imgDir) {
        this.imgDir = imgDir;
    }


}

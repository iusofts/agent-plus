package com.iusofts.basic.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类
 *
 * @author 
 * @date 2019/12/22
 */
public class FileProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessUtils.class);

    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 下载文件
     *
     * @param url
     * @param fileName
     */
    public static void downloadFromUrl(String url, String fileName) {
        try {
            URL fileUrl = new URL(url);
            File file = new File(fileName);
            FileUtils.copyURLToFile(fileUrl, file);
        } catch (Exception e) {
            logger.error("文件下载失败：" + url, e);
        }
    }

    /**
     * 压缩文件
     *
     * @param sourceFilePath 源文件路径
     * @param zipFilename    压缩文件名
     */
    public static void compressToZip(String sourceFilePath, String zipFilePath, String zipFilename) {
        File sourceFile = new File(sourceFilePath);
        File zipPath = new File(zipFilePath);
        if (!zipPath.exists()) {
            zipPath.mkdirs();
        }
        File zipFile = new File(zipPath + File.separator + zipFilename + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            writeZip(sourceFile, "", zos);
        } catch (Exception e) {
            logger.error("压缩文件失败!", e);
        }
    }

    /**
     * 遍历所有文件，压缩
     *
     * @param file       源文件目录
     * @param parentPath 压缩文件目录
     * @param zos        文件流
     */
    public static void writeZip(File file, String parentPath, ZipOutputStream zos) {
        if (file.isDirectory()) {
            parentPath += file.getName() + File.separator;
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    writeZip(f, parentPath, zos);
                }
            }
        } else {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                ZipEntry zipEntry = new ZipEntry(parentPath + file.getName());
                zos.putNextEntry(zipEntry);
                int len;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, len);
                    zos.flush();
                }
            } catch (Exception e) {
                logger.error("文件压缩失败!", e);
            }
        }
    }

    /**
     * 删除文件夹
     *
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    if (!deleteDir(new File(dir, child))) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

}

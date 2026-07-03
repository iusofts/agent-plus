package com.iusofts.basic.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * ftp读取文件
 */
@Slf4j
public class FtpUtil {

    /**
     * 获取FTP连接
     *
     * @param url      ftp ip地址
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return
     * @throws Exception
     */
    public static FTPClient getFtpClient(String url, int port, String username, String password) throws Exception {
        FTPClient ftp = null;
        int reply;
        ftp = new FTPClient();
        // 设置timeout时间
        ftp.setConnectTimeout(30000);
        ftp.connect(url, port);
        ftp.login(username, password);
        //ftp.setFileType(FTP.ASCII_FILE_TYPE);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            return null;
        }

        //设置编码格式
        if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8", "ON"))) {
            ftp.setControlEncoding("UTF-8");
        } else {
            ftp.setControlEncoding("GBK");
        }

        ftp.enterLocalPassiveMode();

        return ftp;
    }

    /**
     * 获取文件夹下某个文件信息流
     *
     * @param ftpClient
     * @param path      文件路径
     * @param fileName  文件名
     * @return
     */
    public static InputStream getFTPFileInputStream(FTPClient ftpClient, String path, String fileName) {
        InputStream in = null;
        try {
            if (ftpClient == null) {
                return in;
            }
            ftpClient.changeWorkingDirectory(path);
            FTPFile[] files = ftpClient.listFiles();

            log.info("files：" + files.length);

            if (files.length > 0) {
                //解决中文路径问题
                in = ftpClient.retrieveFileStream(new String(fileName.getBytes("utf-8"), FTP.DEFAULT_CONTROL_ENCODING));
                if (in == null) {
                    in = ftpClient.retrieveFileStream(new String(fileName.getBytes("gbk"), FTP.DEFAULT_CONTROL_ENCODING));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FTP 读取数据异常！");
        } finally {
            //关闭连接
            if (ftpClient != null) {
                disConnection(ftpClient);
            }
            return in;
        }
    }

    /**
     * 获取Ftp 文件内容
     *
     * @param url      ftp ip地址
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @param path     路径
     * @param fileName 文件名
     * @param encoding 编码
     * @return
     * @throws Exception
     */
    public static String getFtpFile(String url, int port, String username, String password, String path, String fileName, String encoding) throws Exception {
        FTPClient ftpClient = getFtpClient(url, port, username, password);
        if (ftpClient == null) {
            System.out.println("连接失败！");
            return null;
        }
        BufferedReader br = null;
        InputStream in = null;
        in = getFTPFileInputStream(ftpClient, path, fileName);
        StringBuffer result = new StringBuffer();
        if (in == null) {
            System.out.println("读取文件失败！");
            return null;
        }
        try {
            if (StringUtils.isEmpty(encoding)) {
                encoding = "UTF-8";
            }
            br = new BufferedReader(new InputStreamReader(in, encoding));
            String data = null;
            while ((data = br.readLine()) != null) {
                result.append(data + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭ftp连接
            disConnection(ftpClient);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    //关闭io流
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result.toString();
    }

    /**
     * 获取文件夹下的所有文件列表
     *
     * @param ftpClient
     * @param path      路径
     * @return
     */
    public static FTPFile[] getFTPDirectoryFiles(FTPClient ftpClient, String path) {
        FTPFile[] files = null;
        try {
            ftpClient.changeWorkingDirectory(path);
            files = ftpClient.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FTP读取数据异常！");
        }
        //关闭连接
        //disConnection(ftpClient);
        return files;
    }

    /**
     * 关闭FTP服务连接
     *
     * @param ftpClient
     */
    public static void disConnection(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 判断是否是.txt 和 .csv 文件
     *
     * @param fileName 文件名称
     * @return
     */
    public static boolean getFileStatus(String fileName) {
        boolean resutl = false;
        if (StringUtils.isNotEmpty(fileName)) {
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                String fileFormat = fileName.substring(index);

                if (".txt".equalsIgnoreCase(fileFormat) || ".csv".equalsIgnoreCase(fileFormat)) {
                    resutl = true;
                }
            }
        }
        return resutl;
    }

    /**
     * 上传文件
     *
     * @param ftpClient
     * @param path        文件路径
     * @param inputStream 文件流
     * @return
     */
    public static void uploadFile(FTPClient ftpClient, String path, InputStream inputStream) {
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean uploaded = ftpClient.storeFile(path, inputStream);
            inputStream.close();

            if (uploaded) {
                System.out.println("File uploaded successfully.");
            } else {
                System.out.println("File upload failed.");
            }
        } catch (IOException ex) {
            System.err.println("Error uploading file: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        String url = "118.31.116.33";
        int port = 21;
        String username = "ftp03";
        String password = "zH@Lo2yJ";

        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient(url, port, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String path = "/";
        //获取目录下文件列表
        System.out.println("获取到的文件是：");
        FTPFile[] ftpFiles = getFTPDirectoryFiles(ftpClient, path);
        for (FTPFile f : ftpFiles) {
            if (getFileStatus(f.getName())) {
                System.out.println(f.getName());
            }
        }
        System.out.println();
        System.out.println("文件内容是:");
        String fileName = "2222.txt";
        String encoding = "UTF-8";

        //获取某个ftp文件内容
        String fileData = null;
        try {
            fileData = getFtpFile(url, port, username, password, path, fileName, encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(fileData);
    }
}
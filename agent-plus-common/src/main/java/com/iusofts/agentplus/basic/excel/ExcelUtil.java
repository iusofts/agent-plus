package com.iusofts.agentplus.basic.excel;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入导出工具类（基于 Alibaba EasyExcel 实现）
 */
public class ExcelUtil {

    /**
     * 复杂导出Excel（支持自定义是否创建表头）
     *
     * @param list           导出数据列表
     * @param title          表头名称（EasyExcel中可作为sheet副标题，此处暂未使用）
     * @param sheetName      sheet表名
     * @param pojoClass      数据实体类（需添加EasyExcel注解@ExcelProperty）
     * @param fileName       导出文件名（含.xlsx后缀）
     * @param isCreateHeader 是否创建表头
     * @param response       Http响应对象（用于输出文件）
     */
    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass,
                                   String fileName, boolean isCreateHeader, HttpServletResponse response) {
        try {
            // 1. 设置响应头（解决文件名中文乱码、指定文件类型）
            setExcelResponseHeader(fileName, response);

            // 2. 构建Excel写入器
            EasyExcel.write(response.getOutputStream(), pojoClass)
                    .sheet(sheetName)
                    .needHead(isCreateHeader) // 是否创建表头
                    .doWrite(list); // 写入数据
        } catch (IOException e) {
            throw new RuntimeException("Excel导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 复杂导出Excel（默认创建表头）
     * 重载方法，简化调用
     */
    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass,
                                   String fileName, HttpServletResponse response) {
        exportExcel(list, title, sheetName, pojoClass, fileName, true, response);
    }

    /**
     * Map集合导出Excel（无需实体类，表头为Map的key，值为Map的value）
     *
     * @param list     Map数据列表（key=表头名称，value=单元格值）
     * @param fileName 导出文件名（含.xlsx后缀）
     * @param response Http响应对象
     */
    public static void exportExcel(List<Map<String, Object>> list, String fileName, HttpServletResponse response) {
        defaultExport(list, fileName, response);
    }

    /**
     * 设置Excel下载的响应头（通用方法）
     */
    private static void setExcelResponseHeader(String fileName, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 处理文件名中文乱码
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /**
     * Map列表默认导出逻辑
     */
    private static void defaultExport(List<Map<String, Object>> list, String fileName, HttpServletResponse response) {
        try {
            setExcelResponseHeader(fileName, response);
            // EasyExcel支持直接写入Map列表，表头自动取Map的key
            EasyExcel.write(response.getOutputStream())
                    .sheet("sheet1") // 默认sheet名称
                    .doWrite(list);
        } catch (IOException e) {
            throw new RuntimeException("Map数据Excel导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 导出Excel为字节流（支持自定义是否创建表头）
     * 适用场景：无需直接下载，需灵活处理Excel字节流（如保存到本地、上传云存储等）
     *
     * @param list           导出数据列表
     * @param sheetName      sheet表名
     * @param pojoClass      数据实体类（需添加EasyExcel注解@ExcelProperty）
     * @param isCreateHeader 是否创建表头
     * @return ByteArrayOutputStream Excel字节流对象
     */
    public static ByteArrayOutputStream exportExcelToByteArray(List<?> list, String sheetName, Class<?> pojoClass, boolean isCreateHeader) {
        // 创建字节输出流，用于存储Excel字节数据
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // 构建Excel写入器，写入字节输出流
            EasyExcel.write(out, pojoClass)
                    .sheet(sheetName)
                    .needHead(isCreateHeader)
                    .doWrite(list);
            // 刷新流，确保数据全部写入
            out.flush();
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Excel导出为字节流失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径导入Excel（解析为指定实体类）
     *
     * @param filePath   本地/服务器文件路径（如：D:/test.xlsx）
     * @param titleRows  标题行数量（标题行不计入数据，一般为0）
     * @param headerRows 表头行数（表头对应实体类的@ExcelProperty）
     * @param pojoClass  目标实体类
     * @return 解析后的实体类列表
     */
    public static <T> List<T> importExcel(String filePath, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
        if (StringUtils.isBlank(filePath)) {
            return Collections.emptyList();
        }
        // 修正：titleRows默认值防止空指针，headRowNumber = 标题行 + 表头行（从0开始计数）
        int skipRows = (titleRows == null ? 0 : titleRows) + (headerRows == null ? 1 : headerRows);
        try {
            // 构建读取器，跳过标题行+表头行，解析数据
            return EasyExcel.read(filePath)
                    .head(pojoClass) // 指定实体类作为表头映射
                    .sheet() // 读取第一个sheet
                    .headRowNumber(skipRows) // 核心修正：用总跳过行数替代错误方法
                    .doReadSync(); // 同步读取数据
        } catch (Exception e) {
            throw new RuntimeException("Excel文件导入失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据URL导入Excel（解析为指定实体类）
     *
     * @param url        远程Excel文件URL（如：https://xxx.com/test.xlsx）
     * @param titleRows  标题行数量
     * @param headerRows 表头行数量
     * @param pojoClass  目标实体类
     * @return 解析后的实体类列表
     */
    public static <T> List<T> importUrlExcel(String url, Integer titleRows, Integer headerRows, Class<T> pojoClass) throws Exception {
        if (StringUtils.isBlank(url)) {
            return Collections.emptyList();
        }
        // 通过URL获取输入流
        URL newUrl = new URL(url);
        try (InputStream inputStream = newUrl.openStream()) {
            return importInputStreamExcel(inputStream, titleRows, headerRows, pojoClass);
        }
    }

    /**
     * 根据URL导入Excel（解析为指定实体类）
     *
     * @param url        远程Excel文件URL（如：https://xxx.com/test.xlsx）
     * @param pojoClass  目标实体类
     * @return 解析后的实体类列表
     */
    public static <T> List<T> importUrlExcel(String url, Class<T> pojoClass) throws Exception {
        return importUrlExcel(url, null, null, pojoClass);
    }

    /**
     * 根据输入流导入Excel（解析为指定实体类）
     *
     * @param inputStream Excel文件输入流
     * @param titleRows   标题行数量
     * @param headerRows  表头行数量
     * @param pojoClass   目标实体类
     * @return 解析后的实体类列表
     */
    public static <T> List<T> importInputStreamExcel(InputStream inputStream, Integer titleRows, Integer headerRows, Class<T> pojoClass) throws Exception {
        if (inputStream == null) {
            return Collections.emptyList();
        }
        // 修正：titleRows默认值防止空指针，headRowNumber = 标题行 + 表头行（从0开始计数）
        int skipRows = (titleRows == null ? 0 : titleRows) + (headerRows == null ? 1 : headerRows);
        try {
            return EasyExcel.read(inputStream)
                    .head(pojoClass)
                    .sheet()
                    .headRowNumber(skipRows) // 核心修正：用总跳过行数替代错误方法
                    .doReadSync();
        } finally {
            // 关闭输入流，避免资源泄漏
            inputStream.close();
        }
    }

    // 测试示例（需自行定义MemberDto实体类）
    public static void main(String[] args) throws Exception {
        /*
        // 导入示例：假设Excel第1行是标题（titleRows=1），第2行是表头（headerRows=1），则skipRows=2
        List<MemberDto> list = ExcelUtil.importUrlExcel(
            "https://img.redsun360.com/fileList/20211216171925-2850002c-8978-b51c.xlsx", 
            1, 1, MemberDto.class
        );
        System.out.println(list);
        
        // 导出示例（伪代码）
        // List<MemberDto> exportList = new ArrayList<>();
        // HttpServletResponse response = ...; // Web环境中获取response
        // ExcelUtil.exportExcel(exportList, "会员列表", "会员数据", MemberDto.class, "会员列表.xlsx", response);
        */
    }
}
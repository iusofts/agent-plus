package com.iusofts.agentplus.plugin.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParserConfig;
import org.apache.tika.sax.BodyContentHandler;

/**
 * 单独验证 KnowledgeIngestionService 第 104 行的解析逻辑（DocumentContentExtractor.extract）。
 *
 * <p>不启动 Spring，直接 new 出解析器跑，用于排查“解析结果不理想”的问题。</p>
 *
 * <p>运行方式（二选一）：</p>
 * <pre>
 * # 1) 解析远程 URL（OSS 地址）
 * mvn -pl agent-plus-plugin -am test -Dtest=DocumentContentExtractorTest#extractFromUrl -Ddoc.url="https://xxx/a.docx"
 *
 * # 2) 解析本地文件
 * mvn -pl agent-plus-plugin -am test -Dtest=DocumentContentExtractorTest#extractFromLocalFile -Ddoc.file="E:/tmp/a.docx"
 *
 * # 可选：追加 -Dchunk.size=512 -Dchunk.overlap=50 同时打印分块结果
 * </pre>
 *
 * @author Ivan
 */
class DocumentContentExtractorTest {

    /** 解析远程 URL —— 与线上完全一致的路径（下载 + Tika 解析）。 */
    @Test
    @EnabledIfSystemProperty(named = "doc.url", matches = ".+")
    void extractFromUrl() throws Exception {
        //String url = System.getProperty("doc.url");
        String url = "https://iusofts.oss-cn-hangzhou.aliyuncs.com/knowledge/20260710150734-3561b00c-c915-889b.docx";
        DocumentContentExtractor extractor = new DocumentContentExtractor();

        long t0 = System.currentTimeMillis();
        String text = extractor.extract(url);
        long cost = System.currentTimeMillis() - t0;

        printResult("URL=" + url, text, cost);
        maybePrintChunks(text);
    }

    /**
     * 解析本地文件 —— 复用与 {@link DocumentContentExtractor} 完全相同的 Tika 配置，
     * 只把“下载”换成“读本地文件”，方便在没有 OSS URL 时快速验证解析质量。
     */
    @Test
    @EnabledIfSystemProperty(named = "doc.file", matches = ".+")
    void extractFromLocalFile() throws Exception {
        Path path = Path.of(System.getProperty("doc.file"));

        Parser parser = new AutoDetectParser();
        OfficeParserConfig officeConfig = new OfficeParserConfig();
        officeConfig.setUseSAXDocxExtractor(true);
        officeConfig.setUseSAXPptxExtractor(true);
        officeConfig.setIncludeHeadersAndFooters(false);
        officeConfig.setIncludeShapeBasedContent(false);
        ParseContext parseContext = new ParseContext();
        parseContext.set(OfficeParserConfig.class, officeConfig);

        long t0 = System.currentTimeMillis();
        String text;
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            parser.parse(in, handler, metadata, parseContext);
            text = handler.toString() == null ? "" : handler.toString().trim();
        }
        long cost = System.currentTimeMillis() - t0;

        printResult("FILE=" + path, text, cost);
        maybePrintChunks(text);
    }

    private void printResult(String source, String text, long cost) {
        System.out.println("========== 解析结果 ==========");
        System.out.println("来源: " + source);
        System.out.println("耗时: " + cost + "ms");
        System.out.println("长度: " + (text == null ? 0 : text.length()));
        System.out.println("---------- 正文开始 ----------");
        System.out.println(text);
        System.out.println("---------- 正文结束 ----------");
    }

    private void maybePrintChunks(String text) {
        String sizeProp = System.getProperty("chunk.size");
        if (sizeProp == null) {
            return;
        }
        int size = Integer.parseInt(sizeProp);
        int overlap = Integer.parseInt(System.getProperty("chunk.overlap", "0"));
        List<String> chunks = new TextChunker().split(text, size, overlap);
        System.out.println("========== 分块结果 (size=" + size + ", overlap=" + overlap
                + ", count=" + chunks.size() + ") ==========");
        for (int i = 0; i < chunks.size(); i++) {
            String c = chunks.get(i);
            System.out.println("--- chunk[" + i + "] len=" + c.length() + " ---");
            System.out.println(c);
        }
    }
}

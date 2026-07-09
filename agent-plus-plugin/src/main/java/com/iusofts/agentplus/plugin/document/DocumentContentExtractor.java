package com.iusofts.agentplus.plugin.document;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/**
 * 文档内容抽取器:从 OSS url 下载文件并用 Apache Tika 解析为纯文本。
 *
 * <p>Tika 自动识别格式(txt/md/pdf/docx/xlsx/pptx/html 等),无需按扩展名分支。</p>
 *
 * @author Ivan
 */
@Component
public class DocumentContentExtractor {

    private static final Logger log = LoggerFactory.getLogger(DocumentContentExtractor.class);

    /** BodyContentHandler 写入上限,-1 表示不限制(默认 100KB)。 */
    private static final int NO_WRITE_LIMIT = -1;

    private final OkHttpClient httpClient;
    private final Parser parser;
    private final ParseContext parseContext;

    public DocumentContentExtractor() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(60))
                .callTimeout(Duration.ofSeconds(120))
                .build();
        this.parser = new AutoDetectParser();

        // OOXML(docx/xlsx/pptx)提取配置:
        // - 使用 SAX 流式提取器,规避 DOM 提取器把页眉/页脚、图形等内嵌 XML 片段
        //   原样吐到正文的问题(现象:首尾块混入 XML)。
        // - 关闭图形/形状文本抽取,避免 VML/DrawingML fallback 里的标记泄漏为正文。
        OfficeParserConfig officeConfig = new OfficeParserConfig();
        officeConfig.setUseSAXDocxExtractor(true);
        officeConfig.setUseSAXPptxExtractor(true);
        officeConfig.setIncludeHeadersAndFooters(false);
        officeConfig.setIncludeShapeBasedContent(false);

        this.parseContext = new ParseContext();
        this.parseContext.set(OfficeParserConfig.class, officeConfig);
    }

    /**
     * 下载并解析文档为纯文本。
     *
     * @param docUrl 文档 URL(OSS 地址)
     * @return 解析出的纯文本(已 trim)
     * @throws IOException   下载失败
     * @throws TikaException 解析失败
     */
    public String extract(String docUrl) throws IOException, TikaException {
        if (!StringUtils.hasText(docUrl)) {
            throw new IllegalArgumentException("文档 URL 为空");
        }
        Request request = new Request.Builder().url(docUrl).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("下载文档失败,HTTP " + response.code() + ": " + docUrl);
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("下载文档失败,响应体为空: " + docUrl);
            }
            try (InputStream in = new BufferedInputStream(body.byteStream())) {
                BodyContentHandler handler = new BodyContentHandler(NO_WRITE_LIMIT);
                Metadata metadata = new Metadata();
                parser.parse(in, handler, metadata, parseContext);
                String text = handler.toString();
                String trimmed = text == null ? "" : text.trim();
                log.info("文档解析完成: url={}, 内容长度={}", docUrl, trimmed.length());
                return trimmed;
            } catch (org.xml.sax.SAXException e) {
                throw new TikaException("文档解析失败: " + docUrl, e);
            }
        }
    }
}

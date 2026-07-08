package com.iusofts.agentplus.plugin.document;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
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

    /** Tika 默认对字符串输出有 100KB 限制,这里放开(-1 表示不限制)。 */
    private static final int NO_WRITE_LIMIT = -1;

    private final OkHttpClient httpClient;
    private final Tika tika;

    public DocumentContentExtractor() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(60))
                .callTimeout(Duration.ofSeconds(120))
                .build();
        this.tika = new Tika();
        this.tika.setMaxStringLength(NO_WRITE_LIMIT);
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
                Metadata metadata = new Metadata();
                String text = tika.parseToString(in, metadata);
                String trimmed = text == null ? "" : text.trim();
                log.info("文档解析完成: url={}, 内容长度={}", docUrl, trimmed.length());
                return trimmed;
            }
        }
    }
}

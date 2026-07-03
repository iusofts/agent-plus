package com.iusofts.agentplus.basic.freemaker;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Freemaker模板管理
 *
 * @author Ivan Shen
 */
@Component
public class FreemakerTemplateManage implements TemplateManage {

	private static final Logger LOGGER = LoggerFactory.getLogger(FreemakerTemplateManage.class);

	@Resource(name = "freeMarkerCfg")
	private Configuration freeMarkerCfg;

	@Override
	@Deprecated
	public boolean create(String templateName, String content) {
		// 手动维护
		return false;
	}

	@Override
	@Deprecated
	public boolean remove(String templateName) {
		// 手动维护
		return false;
	}

	@Override
	@Deprecated
	public boolean update(String templateName, String content) {
		// 手动维护
		return false;
	}

	@Override
	@Deprecated
	public String get(String templateName) {
		// 手动维护
		return null;
	}

	@Override
	@Deprecated
	public List<String> getAll() {
		// 手动维护
		return null;
	}

	@Override
	public String parse(String templateName, Object data) {
		// 根据模板获取查询
		StringWriter writer = new StringWriter();

		try {
			freeMarkerCfg.getTemplate(templateName + ".ftl").process(data, writer);
		} catch (TemplateException e) {
			LOGGER.error("queryTemplateBuild index TemplateException", e);
		} catch (IOException e) {
			LOGGER.error("queryTemplateBuild index IOException", e);
		}

		LOGGER.debug(writer.toString());

		return writer.toString();
	}
}

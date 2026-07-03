package com.iusofts.basic.freemaker;

import java.util.List;

/**
 * 模板管理
 *
 * @author Ivan Shen
 */
public interface TemplateManage {

	/**
	 * 创建模板
	 * 
	 * @param templateName
	 *            模板名称
	 * @param content
	 *            模板内容
	 * @return
	 */
	boolean create(String templateName, String content);

	/**
	 * 删除模板
	 * 
	 * @param templateName
	 *            模板名称
	 * @return
	 */
	boolean remove(String templateName);

	/**
	 * 更新模板
	 *
	 * @param templateName
	 *            模板名称
	 * @param content
	 *            模板内容
	 * @return
	 */
	boolean update(String templateName, String content);

	/**
	 * 获取模板
	 * 
	 * @param templateName
	 *            模板名称
	 * @return 模板内容
	 */
	String get(String templateName);

	/**
	 * 获取所有模板名称
	 * 
	 * @return
	 */
	List<String> getAll();

	/**
	 * 解析模板
	 * 
	 * @param templateName
	 *            模板名称
	 * @param data
	 *            数据
	 * @return
	 */
	String parse(String templateName, Object data);

}

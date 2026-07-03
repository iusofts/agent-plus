package com.iusofts.system.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * mysql Mapper 接口
 * </p>
 */
@DS("sys")
public interface MySQLMapper {

    @DS("sys")
    @Select("select 1")
    int activeSys();

    @DS("yz")
    @Select("select 1")
    int activeYz();

    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return 存在返回1，否则返回0
     */
    @Select("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = #{tableName}")
    int checkTableExists(@Param("tableName") String tableName);

    /**
     * 创建操作日志表
     * @param tableName 表名
     */
    @Insert("CREATE TABLE `${tableName}` (\n" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志主键',\n" +
            "  `title` varchar(50) COLLATE utf8mb4_bin DEFAULT '' COMMENT '模块标题',\n" +
            "  `business_type` int(2) NOT NULL DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',\n" +
            "  `method` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '方法名称',\n" +
            "  `request_method` varchar(10) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求方式',\n" +
            "  `operator_type` int(1) NOT NULL DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2客户端用户）',\n" +
            "  `oper_name` varchar(50) COLLATE utf8mb4_bin DEFAULT '' COMMENT '操作人员',\n" +
            "  `dept_name` varchar(50) COLLATE utf8mb4_bin DEFAULT '' COMMENT '部门名称',\n" +
            "  `oper_url` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '请求URL',\n" +
            "  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',\n" +
            "  `token` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用户身份令牌',\n" +
            "  `oper_ip` varchar(50) COLLATE utf8mb4_bin DEFAULT '' COMMENT '主机地址',\n" +
            "  `oper_location` varchar(255) COLLATE utf8mb4_bin DEFAULT '' COMMENT '操作地点',\n" +
            "  `oper_param` longtext COLLATE utf8mb4_bin COMMENT '请求参数',\n" +
            "  `json_result` longtext COLLATE utf8mb4_bin COMMENT '返回参数',\n" +
            "  `status` int(1) DEFAULT '0' COMMENT '操作状态（0正常 1异常）',\n" +
            "  `error_msg` longtext COLLATE utf8mb4_bin COMMENT '错误消息',\n" +
            "  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',\n" +
            "  `execute_time` int(11) DEFAULT NULL COMMENT '执行时间(单位毫秒)',\n" +
            "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
            "  `delete_flag` int(11) NOT NULL DEFAULT '0' COMMENT '软删除标记（0：未删除；1：已删除）',\n" +
            "  PRIMARY KEY (`id`) USING BTREE,\n" +
            "  KEY `idx_user_id` (`user_id`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='操作日志记录'")
    void createOperLogTable(@Param("tableName") String tableName);

    /**
     * 删除操作日志表
     * @param tableName 表名
     */
    @Delete("DROP TABLE IF EXISTS `${tableName}`")
    void dropOperLogTable(@Param("tableName") String tableName);

    /**
     * 获取过期的操作日志表列表
     * @param tableNamePattern 表名模式
     * @param cutoffDate 截止日期
     * @return 过期表名列表
     */
    @Select("<script>" +
            "SELECT table_name " +
            "FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() " +
            "AND table_name LIKE '${tableNamePattern}' " +
            "AND table_name &lt; '${cutoffDate}' " +
            "ORDER BY table_name ASC" +
            "</script>")
    List<String> getExpiredLogTables(@Param("tableNamePattern") String tableNamePattern, @Param("cutoffDate") String cutoffDate);
}

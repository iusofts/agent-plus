/*
 Navicat MySQL Dump SQL

 Source Server         : agent-plus
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 121.40.203.82:3306
 Source Schema         : agent-plus-sys

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 03/07/2026 15:47:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2025-02-18 21:35:13', 'admin', '2026-07-01 14:39:46', '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow', 0);
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2025-02-18 21:35:13', '', NULL, '初始化密码 123456', 0);
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2025-02-18 21:35:13', '', NULL, '深色主题theme-dark，浅色主题theme-light', 0);
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2025-02-18 21:35:13', '', NULL, '是否开启验证码功能（true开启，false关闭）', 0);
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2025-02-18 21:35:13', '', NULL, '是否开启注册用户功能（true开启，false关闭）', 0);
INSERT INTO `sys_config` VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2025-02-18 21:35:13', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）', 0);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int(4) NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 109 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '公司', 0, '', '', '', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-06-30 17:57:34', 0);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '总部', 1, '', '', '', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-06-30 17:57:29', 0);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 99, '', '', '', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-06-30 17:57:23', 0);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 1, '', '', '', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-06-30 17:57:04', 0);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 2, '', '15888888888', '', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-06-30 23:18:58', 0);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 99, '', '', '', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-06-30 17:57:15', 0);
INSERT INTO `sys_dept` VALUES (108, 107, '0,100,101,107', '1组', 0, NULL, NULL, NULL, '0', 'admin', '2026-06-30 17:59:30', '', NULL, 1);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(4) NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '性别男', 0);
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '性别女', 0);
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '性别未知', 0);
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '显示菜单', 0);
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '隐藏菜单', 0);
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '正常状态', 0);
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '停用状态', 0);
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '正常状态', 0);
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '停用状态', 0);
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '默认分组', 0);
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '系统分组', 0);
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '系统默认是', 0);
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '系统默认否', 0);
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '通知', 0);
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '公告', 0);
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '正常状态', 0);
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '关闭状态', 0);
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '其他操作', 0);
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '新增操作', 0);
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '修改操作', 0);
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '删除操作', 0);
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '授权操作', 0);
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '导出操作', 0);
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '导入操作', 0);
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '强退操作', 0);
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '生成操作', 0);
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '清空操作', 0);
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-07-01 14:33:40', '正常状态', 0);
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '停用状态', 0);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '用户性别列表', 0);
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '菜单状态列表', 0);
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '系统开关列表', 0);
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '任务状态列表', 0);
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-02-18 21:35:13', 'admin', '2026-07-01 14:04:36', '任务分组列表', 0);
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '系统是否列表', 0);
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '通知类型列表', 0);
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '通知状态列表', 0);
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '操作类型列表', 0);
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-02-18 21:35:13', '', NULL, '登录状态列表', 0);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(4) NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int(1) NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int(1) NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '备注',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1006 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 999, 'system', NULL, NULL, '', 1, 0, 'M', '0', '0', NULL, 'Memo', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (5, 'AI智能体', 0, 0, 'ai', NULL, NULL, '', 1, 0, 'M', '0', '0', NULL, 'MostlyCloudy', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (100, '员工管理', 1, 2, 'user', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'Memo', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 3, 'role', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'Memo', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 4, 'menu', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'Memo', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 5, 'dept', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'Memo', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (104, '字典管理', 1, 6, 'dict', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'Memo', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (500, '智能体管理', 5, 1, 'list', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'MostlyCloudy', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (501, '行业管理', 5, 2, 'industry', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'CollectionTag', 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (1001, '员工新增', 100, 0, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:user:add', NULL, 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (1002, '员工修改', 100, 0, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:user:edit', NULL, 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (1003, '员工删除', 100, 0, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:user:remove', NULL, 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (1004, '重置密码', 100, 0, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', NULL, 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);
INSERT INTO `sys_menu` VALUES (1005, '门店员工导入', 100, 0, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:user:importShopUser', NULL, 'admin', '2026-06-04 11:23:01', '', NULL, '', 0);

-- ----------------------------
-- Table structure for sys_oper_log_2026_07_03
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log_2026_07_03`;
CREATE TABLE `sys_oper_log_2026_07_03`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int(2) NOT NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int(1) NOT NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2客户端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求URL',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户身份令牌',
  `oper_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '请求参数',
  `json_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '返回参数',
  `status` int(1) NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `execute_time` int(11) NULL DEFAULT NULL COMMENT '执行时间(单位毫秒)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log_2026_07_03
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(4) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '1' COMMENT '数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）',
  `permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '菜单权限集合',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '角色状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, '1', NULL, '0', 'admin', '2025-02-18 21:35:13', '', NULL, '超级管理员', 0);
INSERT INTO `sys_role` VALUES (2, '普通角色', 'NORMAL_USER', 2, '2', NULL, '0', 'admin', '2025-02-18 21:35:13', 'admin', '2025-02-20 23:44:56', '普通角色', 0);
INSERT INTO `sys_role` VALUES (3, '门店员工', 'STORE_STAFF', 3, '1', NULL, '0', 'admin', '2025-07-11 00:59:01', 'admin', '2026-01-17 23:21:46', NULL, 0);
INSERT INTO `sys_role` VALUES (4, '总部员工', 'HQ_STAFF', 1, '1', NULL, '0', 'admin', '2025-07-11 01:00:23', 'admin', '2026-01-17 23:21:09', NULL, 0);
INSERT INTO `sys_role` VALUES (5, '销售顾问', 'SALES_CONSULT', 2, '1', NULL, '0', 'admin', '2026-01-18 23:05:16', '', NULL, NULL, 0);
INSERT INTO `sys_role` VALUES (6, '信息员', 'INFORMATION_CLERK', 4, '1', '[\"aiflow:chatflow:list\",\"aiflow:workflow:design\",\"home:index:list\",\"aiflow:workflow:list\",\"ai:chat:list\",\"library:knowledge:list\",\"aiflow:workflow:query\",\"aiflow:workflow:edit\"]', '0', 'admin', '2026-01-18 23:06:00', 'admin', '2026-06-30 17:42:10', '222', 0);
INSERT INTO `sys_role` VALUES (7, '测试角色', 'TEST', 1, '2', '[\"system:user:resetPwd\",\"system:user:export\",\"library:prompt:list\",\"system:user:remove\",\"home:index:list\",\"system:user:index\",\"system:user:import\",\"library:knowledge:list\",\"system:user:query\",\"system:user:add\"]', '0', 'admin', '2026-01-19 22:00:42', 'admin', '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role` VALUES (8, '管理员', 'ADMIN', 0, '1', '', '0', 'admin', '2026-04-07 14:26:27', 'shenchen', '2026-06-30 17:42:13', NULL, 0);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 385 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 3, 2, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (2, 3, 200, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (3, 3, 201, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (4, 3, 202, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (5, 3, 203, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (6, 3, 204, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (7, 3, 2001, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (8, 3, 2002, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (9, 3, 2011, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (10, 3, 2012, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (11, 3, 2021, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (12, 3, 2022, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (13, 3, 2031, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (14, 3, 2041, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (15, 3, 2042, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (16, 4, 1, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (17, 4, 2, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (18, 4, 100, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (19, 4, 101, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (20, 4, 102, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (21, 4, 103, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (22, 4, 104, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (23, 4, 200, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (24, 4, 201, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (25, 4, 202, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (26, 4, 203, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (27, 4, 204, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (28, 4, 1001, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (29, 4, 1002, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (30, 4, 1003, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (31, 4, 1004, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (32, 4, 2001, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (33, 4, 2002, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (34, 4, 2011, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (35, 4, 2012, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (36, 4, 2021, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (37, 4, 2022, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (38, 4, 2031, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (39, 4, 2041, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (40, 4, 2042, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (41, 5, 2, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (42, 5, 200, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (43, 5, 201, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (44, 5, 202, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (45, 5, 203, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (46, 5, 2001, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (47, 5, 2002, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (48, 5, 2011, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (49, 5, 2012, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (50, 5, 2021, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (51, 5, 2022, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (52, 5, 2031, '2026-01-25 23:57:21', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (166, 8, 6, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (167, 8, 600, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (168, 8, 5, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (169, 8, 500, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (170, 8, 2, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (171, 8, 200, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (172, 8, 201, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (173, 8, 202, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (174, 8, 203, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (175, 8, 204, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (176, 8, 3, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (177, 8, 300, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (178, 8, 301, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (179, 8, 302, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (180, 8, 303, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (181, 8, 4, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (182, 8, 400, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (183, 8, 1, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (184, 8, 100, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (185, 8, 1001, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (186, 8, 1002, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (187, 8, 1003, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (188, 8, 1004, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (189, 8, 1005, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (190, 8, 101, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (191, 8, 102, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (192, 8, 103, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (193, 8, 104, '2026-06-01 23:59:12', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (342, 6, 1001, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (343, 6, 1001001, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (344, 6, 1101001, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (345, 6, 1201, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (346, 6, 1201001, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (347, 6, 1201001001, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (348, 6, 1201001002, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (349, 6, 1201002, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (350, 6, 1201003, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (351, 6, 1301002, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (352, 6, 1101, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (353, 6, 1301, '2026-06-30 17:08:03', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (372, 7, 1001, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (373, 7, 1001001, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (374, 7, 1101, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (375, 7, 1101001, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (376, 7, 1101002, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (377, 7, 1401001001, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (378, 7, 1401001002, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (379, 7, 1401001004, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (380, 7, 1401001005, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (381, 7, 1401001006, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (382, 7, 1401001007, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (383, 7, 1401001, '2026-07-02 00:44:31', NULL, 0);
INSERT INTO `sys_role_menu` VALUES (384, 7, 1401, '2026-07-02 00:44:31', NULL, 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户账号',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '员工姓名',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '用户邮箱',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `data_scope` int(11) NULL DEFAULT 1 COMMENT '数据范围（1：默认 2：自定数据权限)',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1015 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '员工信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '管理员', 'ry@163.com', '15888888888', '1', '', '21232f297a57a5a743894a0e4a801fc3', '0', 1, '127.0.0.1', '2026-07-02 21:40:54', 'admin', '2025-02-18 21:35:13', '', '2026-07-02 21:40:53', '管理员', 0);
INSERT INTO `sys_user` VALUES (2, 103, 'ry', '测试', '', '15666666666', '0', '', 'e10adc3949ba59abbe56e057f20f883e', '0', 1, '127.0.0.1', '2026-06-29 15:51:38', 'admin', '2025-02-18 21:35:13', 'admin', '2026-07-01 16:59:08', '测试员', 1);
INSERT INTO `sys_user` VALUES (14, 103, 'test', '测试', '', '13888888888', NULL, '', 'e10adc3949ba59abbe56e057f20f883e', '0', 1, '127.0.0.1', '2026-07-02 00:44:39', 'admin', '2026-07-01 22:42:38', '', '2026-07-02 00:44:38', NULL, 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-01-26 00:01:43', NULL, 0);
INSERT INTO `sys_user_role` VALUES (30, 14, 7, '2026-07-01 22:42:39', NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;

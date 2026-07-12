/*
 工具模块表设计
 Date: 12/07/2026
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_tool
-- ----------------------------
DROP TABLE IF EXISTS `ai_tool`;
CREATE TABLE `ai_tool` (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工具名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工具唯一编码',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '工具类型 1:内置工具 2:http工具',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工具描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标地址',
  `params_schema` json NULL DEFAULT NULL COMMENT '参数定义(JSON Schema格式)',
  `response_schema` json NULL DEFAULT NULL COMMENT '响应定义(JSON Schema格式)',
  `http_config` json NULL DEFAULT NULL COMMENT 'HTTP配置(JSON格式)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code_delete`(`code`, `delete_flag`) USING BTREE,
  INDEX `idx_ai_tool_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai工具表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 内置工具初始数据
-- ----------------------------
INSERT INTO `id_generator` (`type`, `uid`, `name`, `step_min`, `step_max`) VALUES (11, 1000, 'tool', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;

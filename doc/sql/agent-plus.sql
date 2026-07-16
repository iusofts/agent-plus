/*
 Navicat MySQL Dump SQL

 Source Server         : agent-plus
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 121.40.203.82:3306
 Source Schema         : agent-plus

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 15/07/2026 16:06:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_agent
-- ----------------------------
DROP TABLE IF EXISTS `ai_agent`;
CREATE TABLE `ai_agent`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '智能体名称',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '类型 1:自主规划 2:对话流',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '功能介绍',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '智能体图标',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '设定描述',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '使用模型ID',
  `workflow_ids` json NULL COMMENT '绑定工作流ID列表(JSON数组存储)',
  `tool_ids` json NULL COMMENT '绑定工具ID列表(JSON数组存储)',
  `opening_statement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '开场白文案',
  `opening_questions` json NULL COMMENT '开场白预置问题(JSON数组存储)',
  `knowledge_base_ids` json NULL COMMENT '绑定知识库ID列表(JSON数组存储)',
  `retrieval_top_k` int(11) NULL DEFAULT 3 COMMENT '知识库召回条数',
  `temperature` decimal(3, 2) NOT NULL DEFAULT 0.70 COMMENT '生成随机性(temperature)',
  `context_rounds` int(11) NOT NULL DEFAULT 5 COMMENT '携带上下文轮数',
  `max_reply_length` int(11) NULL DEFAULT 2000 COMMENT '最大回复长度',
  `max_inference_length` int(11) NULL DEFAULT 4000 COMMENT '最大推理回答长度',
  `enable_question_suggestion` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用用户问题建议 0:否 1:是',
  `custom_suggestion_prompt` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自定义建议提示词 0:否 1:是',
  `suggestion_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '自定义建议提示词',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_agent_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai智能体' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_agent
-- ----------------------------
INSERT INTO `ai_agent` VALUES (1109, '商场导购员', 1, '一个商场AI导购ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss', 'https://iusofts.oss-cn-hangzhou.aliyuncs.com/agent/20260714160210-300e0ce4-fe8f-32bc.jpg', '你是一个商场导购，需要根据客户需求指引去对应楼层。要亲切、活泼。\n\n如果询问商场营业状态，先调用时间插件确认时间。\n\n每次回复不要超过30个字。', 1, '[]', '[700000002, 700000001]', '有什么我能帮你的吗？', '[\"吃饭在几楼？\", \"厕所在哪？\"]', '[1202]', 3, 0.70, 5, 2000, 4000, 0, 1, 'asdasdasdasd', 0, '2026-07-09 01:35:46', 0, '2026-07-14 16:02:19', 0, 1, 1);
INSERT INTO `ai_agent` VALUES (100000013, '小朋友', 1, '一个会写代码的小学生', NULL, '你是一个会写Python代码的小学生', 1, '[]', '[]', '', '[]', '[]', 3, 0.70, 5, 2000, 4000, 0, 0, '', 0, '2026-07-10 17:18:45', 0, '2026-07-14 14:36:53', 0, 1, 1);

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation`  (
  `id` bigint(20) NOT NULL COMMENT '会话id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体id',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '使用模型ID',
  `current_rounds` int(11) NOT NULL DEFAULT 1 COMMENT '当前轮次',
  `last_chat_time` datetime NULL DEFAULT NULL COMMENT '最后聊天时间',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_conversation_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话会话' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for ai_flow
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow`;
CREATE TABLE `ai_flow`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '类型 1:工作流Workflow 2:对话流Chatflow',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '未命名流程' COMMENT '流程名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程唯一编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '流程描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '图标地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `publish_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发布状态 0:未发布 1:已发布',
  `latest_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'v1.0.0' COMMENT '当前最新版本号(v1.0.0格式)',
  `online_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '线上发布版本号，空=未发布',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `uk_code_delete`(`code`, `delete_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI流程主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_flow
-- ----------------------------
INSERT INTO `ai_flow` VALUES (200000004, 1, '测试商场工作流01', 'TEST_SM_01', '用于处理商场业务的流程，包括但不限于询问楼层、找商户、找洗手间等。', '', 1, 0, 'v1.0.0', '', 1, '2026-07-15 15:20:01', 0, '2026-07-15 15:20:01', 0);
INSERT INTO `ai_flow` VALUES (200000005, 2, '测试商场工作流01', 'TEST_SM_CHAT_01', '用于处理商场业务的流程，包括但不限于询问楼层、找商户、找洗手间等。', '', 1, 0, 'v1.0.0', '', 1, '2026-07-15 15:20:29', 1, '2026-07-15 15:20:35', 0);

-- ----------------------------
-- Table structure for ai_flow_runtime
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_runtime`;
CREATE TABLE `ai_flow_runtime`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行实例ID',
  `flow_id` bigint(20) NOT NULL COMMENT '流程ID',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行使用的语义化版本v1.0.0',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '全局追踪ID',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '运行状态 0等待 1运行中 2成功 3失败 4终止',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `input_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '本次执行入参JSON',
  `output_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '全局输出结果',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `cost_ms` bigint(20) NULL DEFAULT 0 COMMENT '耗时毫秒',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '触发人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新操作人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_flow_run`(`flow_id`, `run_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程运行实例' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_flow_runtime
-- ----------------------------

-- ----------------------------
-- Table structure for ai_flow_runtime_node
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_runtime_node`;
CREATE TABLE `ai_flow_runtime_node`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `runtime_id` bigint(20) NOT NULL COMMENT '关联运行实例ID',
  `node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'VueFlow节点唯一id',
  `node_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型(Start/LLM/Knowledge/Condition/End)',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0未执行 1执行中 2成功 3失败 4跳过',
  `node_input` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点入参JSON',
  `node_output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点输出JSON',
  `error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '异常堆栈信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '节点开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '节点结束时间',
  `cost_ms` bigint(20) NULL DEFAULT 0 COMMENT '节点耗时(毫秒)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_runtime_id`(`runtime_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '运行节点明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_flow_runtime_node
-- ----------------------------

-- ----------------------------
-- Table structure for ai_flow_version
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_version`;
CREATE TABLE `ai_flow_version`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_id` bigint(20) NOT NULL COMMENT '关联ai_flow主键',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语义化版本 v1.0.0',
  `version_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '版本别名/标题',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '修改备注',
  `flow_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'VueFlow画布完整数据JSON(nodes/edges/viewport)',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '流程全局配置(入参、超时、重试、权限等)',
  `publishing_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发布状态 0草稿 1已发布 2待审核',
  `publishing_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `publishing_by` bigint(20) NULL DEFAULT NULL COMMENT '发布人ID',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '保存操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '版本生成时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_version`(`flow_id`, `version_no`, `delete_flag`) USING BTREE,
  INDEX `idx_flow_id`(`flow_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI流程版本画布表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_flow_version
-- ----------------------------

-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库描述',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库图标',
  `collection_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '向量库集合名称',
  `embedding_model_id` bigint(20) NULL DEFAULT NULL COMMENT '嵌入模型ID',
  `chunk_size` int(11) NOT NULL DEFAULT 512 COMMENT '分块大小',
  `chunk_overlap` int(11) NOT NULL DEFAULT 100 COMMENT '分块重叠大小',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_knowledge_base_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_knowledge_base
-- ----------------------------
INSERT INTO `ai_knowledge_base` VALUES (1202, 'XX商场知识库', '', 'Shop', 'kb_1202', 2, 1024, 50, 1, 1, '2026-07-09 17:24:09', 1, '2026-07-14 17:27:15', 0, 1);

-- ----------------------------
-- Table structure for ai_knowledge_chunk
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_chunk`;
CREATE TABLE `ai_knowledge_chunk`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `document_id` bigint(20) NOT NULL COMMENT '文档ID',
  `vector_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量库中的ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分块内容',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '分块序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '分块状态 0:停用 1:启用',
  `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '元数据JSON',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_knowledge_chunk_kb_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_ai_knowledge_chunk_doc_id`(`document_id`) USING BTREE,
  INDEX `idx_ai_knowledge_chunk_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档分块' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_knowledge_chunk
-- ----------------------------
INSERT INTO `ai_knowledge_chunk` VALUES (1368, 1202, 1366, '1367-0', 'XX购物中心楼层店铺及设施介绍文档\n一、商场基础概况\nXX购物中心是集时尚购物、特色餐饮、亲子休闲、潮流娱乐、生活便民于一体的综合性商业综合体，涵盖地下2层、地上6层商业区域，汇聚国内外优质品牌店铺，配套完善的便民设施。商场整体动线清晰、功能分区明确，全方位满足顾客购物、休闲、聚餐、娱乐等多元消费需求。\n营业时间：周一至周日 10:07-22:08\n基础便民服务：各楼层配备休息座椅、饮水设备；负一层、一层设有总服务台，可提供会员办理、寻人广播、物品寄存、母婴用品、轮椅/婴儿车租借、咨询引导等服务；全楼层覆盖免费WiFi。\n二、各楼层店铺及卫生间设施详情\n（一）负二层（B2）：地下停车场+设备配套层\n核心业态：地下停车场、商场设备机房、员工通道、便民储物区，无对外营业店铺，主要为顾客提供停车配套服务，规划充足停车位，支持新能源汽车充电。\n卫生间位置：楼层西侧扶梯旁，设有男卫生间、女卫生间、无障碍卫生间，配备洗手台、干手器、防滑设施，24小时开放。\n（二）负一层（B1）：生活超市+潮流小吃+便民生活\n核心业态：精品生活超市、网红小吃、特色快餐、日用百货、零食茶饮、生活便民小店，主打亲民消费、日常刚需及快捷餐饮，适配日常采购、短途休憩、简餐就餐需求。\n主力入驻店铺：大型生鲜生活超市、蜜雪冰城、正新鸡排、绝味鸭脖、晨光文具、精品零食店、手机配件店、干洗便民店等。\n卫生间位置：楼层中部中庭后方，扶梯两侧分别设置男女卫生间，同时配备无障碍卫生间、母婴卫生间，设有婴儿护理台、恒温洗手池，适配亲子、特殊人群使用。\n（三）一层（1F）：轻奢时尚+美妆珠宝+精品零售\n核心业态：高端美妆、黄金珠宝、轻奢配饰、国际快时尚、钟表眼镜、精品箱包，为商场核心潮流时尚楼层，主打高端精致消费，适配穿搭、美妆、礼品选购需求。\n主力入驻店铺：雅诗兰黛、兰蔻、完美日记、周大福、周生生、老凤祥、优衣库、UR、名创优品、高端钟表店、轻奢箱包店、品牌香水集合店等。\n卫生间位置：楼层北侧收银区后方，设有独立男、女卫生间，南侧靠近服务台位置设置无障碍卫生间，干净整洁、配套设施齐全。\n（四）二层（2F）：都市女装+配饰穿搭+休闲美学\n核心业态：时尚女装、淑女风服饰、通勤职业装、女装配饰、丝巾鞋帽、美甲美睫、穿搭集合店，覆盖少女、职场女性、轻奢休闲等全风格女装穿搭。', 0, 1, NULL, 1, '2026-07-10 16:07:57', 1, '2026-07-14 17:27:16', 0, 1);
INSERT INTO `ai_knowledge_chunk` VALUES (1370, 1202, 1366, '1369-1', '勤职业装、女装配饰、丝巾鞋帽、美甲美睫、穿搭集合店，覆盖少女、职场女性、轻奢休闲等全风格女装穿搭。\n主力入驻店铺：太平鸟、乐町、ONLY、VERO MODA、伊芙丽、诗凡黎、轻奢女装集合店、品牌鞋帽配饰店、高端美甲美睫工作室等。\n卫生间位置：楼层东侧扶梯转角处，配置男、女卫生间及无障碍卫生间，周边设有休闲休息区，方便顾客休憩等候。\n（五）三层（3F）：男装运动+户外休闲+潮流穿搭\n核心业态：商务男装、休闲男装、运动服饰、户外装备、潮流潮牌、男士配饰、健身穿搭，兼顾商务正装、日常休闲、运动潮流等多元男士穿搭需求。\n主力入驻店铺：七匹狼、劲霸、杰克琼斯、李宁、安踏、阿迪达斯、耐克、特步、户外冲锋衣集合店、男士皮具配饰店等。\n卫生间位置：楼层西侧电梯口旁，设有独立男、女卫生间，配备防滑地面、紧急呼叫按钮，同时设置专属男士洗漱整理区域。\n（六）四层（4F）：亲子儿童+母婴休闲+童趣娱乐\n核心业态：儿童服饰、母婴用品、玩具教具、儿童乐园、亲子早教、童装鞋帽、婴幼儿护理，是一站式亲子体验楼层，适配家庭亲子消费场景。\n主力入驻店铺：巴拉巴拉、安奈儿、小猪班纳、母婴生活馆、益智玩具店、室内儿童乐园、亲子手工馆、婴幼儿游泳馆、儿童鞋帽集合店等。\n卫生间位置：楼层中庭西侧，专属设置儿童卫生间、母婴卫生间、无障碍卫生间，配备儿童专用洗手台、小马桶、婴儿抚触台、温奶器，全方位适配亲子家庭需求。\n（七）五层（5F）：特色餐饮+主题美食+休闲茶饮\n核心业态：各地特色正餐、网红主题餐厅、火锅烤肉、中西简餐、奶茶咖啡、甜品烘焙，涵盖大众美食、特色正餐、休闲饮品，满足聚餐、约会、休闲就餐需求。\n主力入驻店铺：海底捞、太二酸菜鱼、烤肉自助餐厅、中西式简餐店、瑞幸咖啡、星巴克、甜品蛋糕店、特色干锅、家常菜主题餐厅等。\n卫生间位置：楼层南北两侧各设置一组男女卫生间，北侧配套无障碍卫生间，紧邻餐饮区，动线便捷，高峰时段可分流使用。\n（八）六层（6F）：影院娱乐+休闲体验+主题休闲\n核心业态：巨幕影院、休闲娱乐、桌游电玩、私人影院、解压体验馆、轻奢休闲清吧，主打沉浸式娱乐、休闲放松，适配年轻人聚会、观影、休闲娱乐场景。\n主力入驻店铺：XX国际影城、电玩城、桌游馆、沉浸式体验馆、休闲清吧、网红打卡休闲区、文创集合店等。\n卫生间位置：影院检票口外侧东侧，设有男、女卫生间及无障碍卫生间，配套休息长椅，观影前后可便捷使用。\n三、设施通用说明\n1.', 1, 1, NULL, 1, '2026-07-10 16:07:57', 0, '2026-07-14 17:27:16', 0, 1);
INSERT INTO `ai_knowledge_chunk` VALUES (1372, 1202, 1366, '1371-2', '外侧东侧，设有男、女卫生间及无障碍卫生间，配套休息长椅，观影前后可便捷使用。\n三、设施通用说明\n1.  全楼层卫生间均免费开放，定时清洁消杀，配备洗手液、纸巾、干手器等基础用品，保障卫生整洁；\n2.  无障碍卫生间、母婴卫生间、儿童卫生间均为专属便民设施，优先供特殊人群、亲子家庭使用；\n3.  各楼层扶梯、电梯、卫生间均设有清晰导视标识，顾客可跟随楼层指引、地面标识快速定位；\n4.  如需设施协助、店铺咨询，可前往各楼层服务点位或拨打商场服务热线。\n|（注：部分内容可能由 AI 生成）', 2, 1, NULL, 1, '2026-07-10 16:07:58', 0, '2026-07-14 17:27:16', 0, 1);

-- ----------------------------
-- Table structure for ai_knowledge_doc_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_doc_log`;
CREATE TABLE `ai_knowledge_doc_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `knowledge_base_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库名称',
  `doc_id` bigint(20) NOT NULL COMMENT '文档ID',
  `doc_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档名称',
  `operation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型(ADD/UPDATE/DELETE)',
  `chunk_count` int(11) NULL DEFAULT 0 COMMENT '分块数量',
  `total_char_count` int(11) NULL DEFAULT 0 COMMENT '总字符数',
  `total_embedding_tokens` int(11) NULL DEFAULT 0 COMMENT 'embedding总消耗token',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `start_time` datetime(3) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_knowledge_base_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_doc_id`(`doc_id`) USING BTREE,
  INDEX `idx_time_sign`(`time_sign`) USING BTREE,
  INDEX `idx_time_hour`(`time_sign`, `hour_sign`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档处理日志' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for ai_knowledge_document
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_document`;
CREATE TABLE `ai_knowledge_document`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档名称',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text' COMMENT '文档类型 txt/pdf/docx/md/url',
  `doc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档URL/路径',
  `summary` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文档内容摘要',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '文档状态 0:待处理 1:处理中 2:可用 3:失败 4:已禁用 5:已归档',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理失败原因',
  `chunk_count` int(11) NOT NULL DEFAULT 0 COMMENT '分块数量',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_knowledge_document_kb_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_ai_knowledge_document_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_knowledge_document
-- ----------------------------
INSERT INTO `ai_knowledge_document` VALUES (1366, 1202, 'XX购物中心楼层店铺及设施介绍文档.docx', 'docx', 'https://iusofts.oss-cn-hangzhou.aliyuncs.com/knowledge/20260710160754-74779d32-840c-3a7c.docx', NULL, 2, '', 3, 1, '2026-07-10 16:07:56', 1, '2026-07-14 17:27:16', 0, 1);

-- ----------------------------
-- Table structure for ai_knowledge_retrieval_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_retrieval_log`;
CREATE TABLE `ai_knowledge_retrieval_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路追踪ID(关联llm调用)',
  `call_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用来源(AGENT/CHAT/FLOW/API)',
  `source_id` bigint(20) NULL DEFAULT NULL COMMENT '来源ID(智能体ID/会话ID/流程ID)',
  `source_node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源节点ID(工作流节点ID)',
  `knowledge_base_id` bigint(20) NOT NULL COMMENT '知识库ID',
  `knowledge_base_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库名称',
  `query` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '检索查询内容',
  `query_char_count` int(11) NULL DEFAULT 0 COMMENT '查询字符数',
  `query_embedding_tokens` int(11) NULL DEFAULT 0 COMMENT '查询向量化消耗token',
  `top_k` int(11) NULL DEFAULT NULL COMMENT '召回条数',
  `retrieved_chunks` json NULL COMMENT '召回文档块列表',
  `retrieved_count` int(11) NULL DEFAULT 0 COMMENT '实际召回数量',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `start_time` datetime(3) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_call_source`(`call_source`) USING BTREE,
  INDEX `idx_knowledge_base_id`(`knowledge_base_id`) USING BTREE,
  INDEX `idx_time_sign`(`time_sign`) USING BTREE,
  INDEX `idx_org_id`(`org_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_time_hour`(`time_sign`, `hour_sign`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库检索日志' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for ai_llm_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_llm_call_log`;
CREATE TABLE `ai_llm_call_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  `call_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用来源(AGENT/CHAT/FLOW/API)',
  `source_id` bigint(20) NULL DEFAULT NULL COMMENT '来源ID(智能体ID/会话ID/流程ID)',
  `source_node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源节点ID(工作流节点ID)',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '模型ID',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `model_provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型提供商(QWEN/DOUBAO/OPENAI/CUSTOM)',
  `temperature` decimal(4, 2) NULL DEFAULT NULL COMMENT '生成温度',
  `max_tokens` int(11) NULL DEFAULT NULL COMMENT '最大生成长度',
  `input_messages` json NULL COMMENT '输入消息列表',
  `input_char_count` int(11) NULL DEFAULT 0 COMMENT '输入字符数',
  `input_tokens` int(11) NULL DEFAULT 0 COMMENT '输入消耗token数',
  `output_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输出内容',
  `output_char_count` int(11) NULL DEFAULT 0 COMMENT '输出字符数',
  `output_tokens` int(11) NULL DEFAULT 0 COMMENT '输出消耗token数',
  `total_tokens` int(11) NULL DEFAULT 0 COMMENT '总消耗token数',
  `call_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '调用状态(0:失败 1:成功)',
  `error_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误码',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `start_time` datetime(3) NULL DEFAULT NULL COMMENT '调用开始时间',
  `end_time` datetime(3) NULL DEFAULT NULL COMMENT '调用结束时间',
  `duration` int(11) NULL DEFAULT 0 COMMENT '调用时长(毫秒)',
  `time_sign` date NOT NULL COMMENT '日期(用于分区)',
  `hour_sign` tinyint(4) NULL DEFAULT NULL COMMENT '小时(0-23,用于按小时聚合)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_call_source`(`call_source`) USING BTREE,
  INDEX `idx_source_id`(`source_id`) USING BTREE,
  INDEX `idx_model_id`(`model_id`) USING BTREE,
  INDEX `idx_time_sign`(`time_sign`) USING BTREE,
  INDEX `idx_org_id`(`org_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_call_status`(`call_status`) USING BTREE,
  INDEX `idx_time_hour`(`time_sign`, `hour_sign`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI大模型调用日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message`  (
  `id` bigint(20) NOT NULL COMMENT '消息id',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话id',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色(user/assistant)',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容',
  `struct_res` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '结构返回值',
  `agent_id` bigint(20) NULL DEFAULT NULL COMMENT '智能体ID',
  `input_tokens` int(11) NULL DEFAULT 0 COMMENT '输入消耗token数',
  `output_tokens` int(11) NULL DEFAULT 0 COMMENT '输出消耗token数',
  `total_tokens` int(11) NULL DEFAULT 0 COMMENT '总消耗token数',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_ai_message_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_model
-- ----------------------------
DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `model_type` int(11) NOT NULL DEFAULT 1 COMMENT '模型类型 1:LLM 2:Embedding',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'qwen' COMMENT '提供商 qwen:千问 doubao:豆包 openai:OpenAI',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型显示名称',
  `api_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API密钥',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'API基础URL',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '模型配置JSON',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认模型 0:否 1:是',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_model_provider`(`provider`) USING BTREE,
  INDEX `idx_ai_model_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模型配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_model
-- ----------------------------
INSERT INTO `ai_model` VALUES (1, 1, 'qwen', 'qwen-plus', '', 'sk-xxx', 'https://dashscope.aliyuncs.com/api/v1', NULL, 1, 1, 0, '2026-07-08 11:19:40', 1, '2026-07-09 13:41:06', 0, 1);
INSERT INTO `ai_model` VALUES (2, 2, 'qwen', 'text-embedding-v4', '通用文本向量-v4', 'sk-xxxx', 'https://dashscope.aliyuncs.com/api/v1', NULL, 1, 1, 0, '2026-07-08 11:19:40', 1, '2026-07-09 16:17:22', 0, 1);
INSERT INTO `ai_model` VALUES (3, 1, 'doubao', 'ep-20240606181916-xxxxx', '豆包-pro', 'ssssssssss', 'https://ark.cn-beijing.volces.com/api/v3', NULL, 1, 0, 0, '2026-07-08 11:19:40', 1, '2026-07-09 11:07:49', 0, 1);

-- ----------------------------
-- Table structure for ai_plugin
-- ----------------------------
DROP TABLE IF EXISTS `ai_plugin`;
CREATE TABLE `ai_plugin`  (
  `id` bigint(20) NOT NULL COMMENT '主键编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '插件名称',
  `plugin_type` tinyint(4) NOT NULL COMMENT '插件类型 1:内置工具 2:服务接口 3:MCP',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '插件描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '插件图标地址',
  `plugin_config` json NULL COMMENT '插件专属配置，按类型区分结构',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记 0正常 1已删除',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plugin_type`(`plugin_type`, `delete_flag`, `status`) USING BTREE,
  INDEX `idx_plugin_org_id`(`org_id`, `delete_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI插件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_plugin
-- ----------------------------
INSERT INTO `ai_plugin` VALUES (800000000, '内置插件', 1, '系统内置工具插件，聚合平台自带的内置工具', NULL, NULL, 0, 1, 0, '2026-07-13 15:03:48', 0, NULL, 0, 1);
INSERT INTO `ai_plugin` VALUES (800000002, '测试服务1', 2, '先测试试11111111111', 'Handbag', '{\"url\": \"http://localhost/\", \"headers\": {\"User-Agent\": \"AgentPlus/1.0\"}}', 1, 1, 0, '2026-07-13 16:17:50', 0, '2026-07-13 17:08:22', 0, 1);

-- ----------------------------
-- Table structure for ai_tool
-- ----------------------------
DROP TABLE IF EXISTS `ai_tool`;
CREATE TABLE `ai_tool`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '工具名称',
  `plugin_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '插件ID',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '工具类型 1:内置工具 2:服务接口',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工具描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标地址',
  `params_schema` json NULL COMMENT '参数定义(JSON Schema格式)',
  `response_schema` json NULL COMMENT '响应定义(JSON Schema格式)',
  `http_config` json NULL COMMENT 'HTTP配置(JSON格式)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_tool_org_id`(`org_id`) USING BTREE,
  INDEX `idx_plugin_id_delete`(`plugin_id`, `delete_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai工具表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_tool
-- ----------------------------
INSERT INTO `ai_tool` VALUES (700000001, '计算器', 800000000, 1, '执行基本数学运算：加减乘除', NULL, '[{\"name\": \"a\", \"type\": \"Number\", \"enabled\": true, \"children\": null, \"itemType\": null, \"required\": true, \"description\": \"第一个操作数\", \"defaultValue\": null, \"injectMethod\": null}, {\"name\": \"b\", \"type\": \"Number\", \"enabled\": true, \"children\": null, \"itemType\": null, \"required\": true, \"description\": \"第二个操作数\", \"defaultValue\": null, \"injectMethod\": null}, {\"name\": \"operation\", \"type\": \"String\", \"enabled\": true, \"children\": null, \"itemType\": null, \"required\": true, \"description\": \"运算类型：add(+)、subtract(-)、multiply(*)、divide(/)\", \"defaultValue\": null, \"injectMethod\": null}, {\"name\": \"scale\", \"type\": \"Integer\", \"enabled\": true, \"children\": null, \"itemType\": null, \"required\": false, \"description\": \"除法精度（小数位数）\", \"defaultValue\": \"10\", \"injectMethod\": null}]', '[{\"name\": \"a\", \"type\": \"Number\", \"enabled\": true, \"children\": null, \"description\": \"第一个操作数\"}, {\"name\": \"b\", \"type\": \"Number\", \"enabled\": true, \"children\": null, \"description\": \"第二个操作数\"}, {\"name\": \"operation\", \"type\": \"String\", \"enabled\": true, \"children\": null, \"description\": \"运算类型\"}, {\"name\": \"result\", \"type\": \"Number\", \"enabled\": true, \"children\": null, \"description\": \"计算结果\"}]', NULL, 1, 0, '2026-07-13 15:06:03', 0, NULL, 0, 1);
INSERT INTO `ai_tool` VALUES (700000002, '当前时间', 800000000, 1, '获取服务器真实当前系统时间，可自定义时间格式化模板', NULL, '[{\"name\": \"pattern\", \"type\": \"String\", \"enabled\": true, \"children\": null, \"itemType\": null, \"required\": false, \"description\": \"时间格式，如 yyyy-MM-dd HH:mm:ss\", \"defaultValue\": \"yyyy-MM-dd HH:mm:ss\", \"injectMethod\": null}]', '[{\"name\": \"time\", \"type\": \"String\", \"enabled\": true, \"children\": null, \"description\": \"格式化后的标准时间字符串\"}, {\"name\": \"timestamp\", \"type\": \"Number\", \"enabled\": true, \"children\": null, \"description\": \"当前毫秒时间戳\"}, {\"name\": \"pattern\", \"type\": \"String\", \"enabled\": true, \"children\": null, \"description\": \"本次使用的格式化模板\"}]', NULL, 1, 0, '2026-07-13 15:06:03', 0, '2026-07-14 10:42:48', 0, 1);
INSERT INTO `ai_tool` VALUES (700000003, '查询用户信息', 800000002, 2, '用于根据手机号码查询用户的信息接口', 'Avatar', '[{\"name\": \"mobile\", \"type\": \"String\", \"enabled\": true, \"children\": [], \"itemType\": null, \"required\": false, \"description\": \"手机号\", \"defaultValue\": \"\", \"injectMethod\": \"Body\"}]', '[{\"name\": \"name\", \"type\": \"String\", \"enabled\": true, \"children\": [], \"description\": \"姓名\"}]', '{\"uri\": \"getUserInfoByMobile\", \"method\": \"POST\"}', 1, 0, '2026-07-13 17:17:00', 0, NULL, 0, 1);

-- ----------------------------
-- Table structure for id_generator
-- ----------------------------
DROP TABLE IF EXISTS `id_generator`;
CREATE TABLE `id_generator`  (
  `type` int(11) NOT NULL AUTO_INCREMENT COMMENT '类型',
  `uid` int(11) NOT NULL COMMENT '自增ID',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '名称',
  `step_min` int(11) NOT NULL DEFAULT 1 COMMENT '最小步长',
  `step_max` int(11) NOT NULL DEFAULT 1 COMMENT '最大步长',
  PRIMARY KEY (`type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of id_generator
-- ----------------------------
INSERT INTO `id_generator` VALUES (1, 100000097, 'chat', 1, 1);
INSERT INTO `id_generator` VALUES (2, 200000005, 'flow', 1, 1);
INSERT INTO `id_generator` VALUES (3, 300000000, 'knowledge_base', 1, 1);
INSERT INTO `id_generator` VALUES (4, 400000001, 'knowledge_document', 1, 1);
INSERT INTO `id_generator` VALUES (5, 500000006, 'knowledge_chunk', 1, 1);
INSERT INTO `id_generator` VALUES (6, 600000000, 'ai_model', 1, 1);
INSERT INTO `id_generator` VALUES (11, 700000003, 'tool', 1, 1);
INSERT INTO `id_generator` VALUES (12, 800000002, 'plugin', 1, 1);

-- ----------------------------
-- Table structure for t_industry
-- ----------------------------
DROP TABLE IF EXISTS `t_industry`;
CREATE TABLE `t_industry`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '行业名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态（1.启用 2.停用）',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '行业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_industry
-- ----------------------------
INSERT INTO `t_industry` VALUES (1, '测试', 0, 1, 1, '2026-06-04 11:28:49', NULL, '2026-06-04 11:28:49', 0);

SET FOREIGN_KEY_CHECKS = 1;

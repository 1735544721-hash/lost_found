/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : lost_found_db

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 01/07/2025 16:17:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for claim_application
-- ----------------------------
DROP TABLE IF EXISTS `claim_application`;
CREATE TABLE `claim_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `item_id` bigint NOT NULL COMMENT '物品ID',
  `item_type` tinyint NOT NULL COMMENT '物品类型(0招领信息,1失物信息)',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '申请说明',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待审核,1已通过,2已拒绝,3已取消)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item`(`item_id` ASC, `item_type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '认领申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of claim_application
-- ----------------------------
INSERT INTO `claim_application` VALUES (1, 1, 1, 3, '俺滴学生证！！！！！！！！！！', 3, '2025-06-23 17:34:22', '2025-06-23 17:51:34', NULL, '2025-06-23 17:39:49', '用户主动取消申请');
INSERT INTO `claim_application` VALUES (2, 1, 1, 3, '在我这！！！！！！！！', 1, '2025-06-23 17:40:01', '2025-06-23 17:51:37', 2, '2025-06-23 17:43:54', '谢谢泥');
INSERT INTO `claim_application` VALUES (3, 5, 1, 3, '111111111111111', 0, '2025-06-25 11:39:25', '2025-06-25 11:39:25', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for found_item
-- ----------------------------
DROP TABLE IF EXISTS `found_item`;
CREATE TABLE `found_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '招领ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `found_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拾取地点',
  `found_time` datetime NULL DEFAULT NULL COMMENT '拾取时间',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `images` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片(多张用逗号分隔)',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待认领,1已认领,2已交接,3已关闭,4已过期)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_status_create_time`(`status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_category_status`(`category_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '招领信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of found_item
-- ----------------------------
INSERT INTO `found_item` VALUES (1, '捡到一部手机', '在教学楼捡到一部iPhone手机，黑色外壳', 2, '教学楼三楼', '2023-05-17 09:15:00', '李四', '13900139000', NULL, 2, 0, '2025-06-23 16:59:00', '2025-06-23 17:17:14');
INSERT INTO `found_item` VALUES (2, '捡到一串钥匙', '在操场捡到一串钥匙，约5把', 4, '操场跑道', '2023-05-18 16:20:00', '李四', '13900139001', NULL, 2, 0, '2025-06-23 16:59:00', '2025-06-23 16:59:00');
INSERT INTO `found_item` VALUES (3, '捡到一本660', '里面写了2页左右，封面没有姓名', 5, '图书馆楼下共享单车', '2025-06-19 00:00:00', 'jx', '13123456789', '/img/1750670457832.png', 3, 0, '2025-06-23 17:20:59', '2025-06-23 17:20:59');
INSERT INTO `found_item` VALUES (4, '测试测试', '测试测试测试测试测试', 2, '测试', '2025-06-26 00:00:00', 'user1', '13800138001', '/img/1751073473254.jpg', 2, 0, '2025-06-28 09:18:10', '2025-06-28 09:18:10');

-- ----------------------------
-- Table structure for item_category
-- ----------------------------
DROP TABLE IF EXISTS `item_category`;
CREATE TABLE `item_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort` int NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_category
-- ----------------------------
INSERT INTO `item_category` VALUES (1, '证件类', 1, '2025-06-23 16:59:00', '2025-06-23 16:59:00');
INSERT INTO `item_category` VALUES (2, '电子产品', 2, '2025-06-23 16:59:00', '2025-06-23 16:59:00');
INSERT INTO `item_category` VALUES (3, '现金/卡类', 3, '2025-06-23 16:59:00', '2025-06-23 16:59:00');
INSERT INTO `item_category` VALUES (4, '生活用品', 4, '2025-06-23 16:59:00', '2025-06-23 16:59:00');
INSERT INTO `item_category` VALUES (5, '书籍资料', 5, '2025-06-23 16:59:00', '2025-06-23 16:59:00');
INSERT INTO `item_category` VALUES (6, '衣物饰品', 6, '2025-06-23 16:59:00', '2025-06-23 16:59:00');
INSERT INTO `item_category` VALUES (7, '其他', 99, '2025-06-23 16:59:00', '2025-06-23 16:59:00');

-- ----------------------------
-- Table structure for lost_item
-- ----------------------------
DROP TABLE IF EXISTS `lost_item`;
CREATE TABLE `lost_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '失物ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `lost_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '丢失地点',
  `lost_time` datetime NULL DEFAULT NULL COMMENT '丢失时间',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `images` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片(多张用逗号分隔)',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待认领,1已认领,2已交接,3已关闭,4已过期)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_status_create_time`(`status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_category_status`(`category_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '失物信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lost_item
-- ----------------------------
INSERT INTO `lost_item` VALUES (1, '丢失学生证', '在图书馆丢失了一张学生证，姓名张三', 1, '图书馆二楼', '2023-05-15 14:30:00', '张三', '13800138001', NULL, 2, 1, '2025-06-23 16:59:00', '2025-06-23 17:14:49');
INSERT INTO `lost_item` VALUES (2, '丢失黑色钱包', '内有身份证、银行卡等重要证件', 3, '食堂门口', '2023-05-16 12:00:00', '张三', '13800138001', '/img/1750829121743.jpg,/img/1750829130466.jpg', 2, 0, '2025-06-23 16:59:00', '2025-06-25 13:25:31');
INSERT INTO `lost_item` VALUES (3, '学生证丢了', '有我照片，学号为2006051039', 1, '图书馆2楼', '2025-06-17 00:00:00', 'test', '13123456789', '/img/1750671050359.jpeg', 3, 0, '2025-06-23 17:18:23', '2025-06-23 17:30:51');
INSERT INTO `lost_item` VALUES (4, '照片丢了', '照片如下，一个小房子', 4, '南食堂', '2025-06-20 00:00:00', 'test', '13123456789', '/img/1750671103600.jpeg', 3, 0, '2025-06-23 17:32:14', '2025-06-23 17:32:14');
INSERT INTO `lost_item` VALUES (5, 'test0000', 'test0000111', 1, 'test0000', '2025-06-17 00:00:00', 'user1', '13800138001', '', 2, 2, '2025-06-25 11:38:16', '2025-06-25 11:40:14');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '类型(0系统消息,1申请消息,2审核消息)',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读(0未读,1已读)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 2, '新的认领申请', '用户 jx 申请认领您发布的物品：test0000，请及时处理。', 1, 3, 1, '2025-06-25 11:39:25');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `role_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色编码(ADMIN/USER)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态(0待审核,1正常,2审核失败,3已禁用,4已锁定,5已过期)',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$S7fPW1PZXknUl1cZJlcOK.xcSrMV1w68kcB7JB4sknKJpti/HOrnC', '系统管理员', NULL, NULL, NULL, 'ADMIN', '2025-06-23 16:59:00', '2025-06-23 17:08:03', 1, '');
INSERT INTO `user` VALUES (2, 'user1', '$2a$10$S7fPW1PZXknUl1cZJlcOK.xcSrMV1w68kcB7JB4sknKJpti/HOrnC', '张三', '13800138001', 'zhangsan@example.com', NULL, 'USER', '2025-06-23 16:59:00', '2025-06-23 17:08:04', 1, '');
INSERT INTO `user` VALUES (3, 'test', '$2a$10$S7fPW1PZXknUl1cZJlcOK.xcSrMV1w68kcB7JB4sknKJpti/HOrnC', 'jx', '13123456789', '1796145602@qq.com', NULL, 'USER', '2025-06-23 17:07:52', '2025-06-23 17:07:52', 1, NULL);
INSERT INTO `user` VALUES (5, 'test1111', '$2a$10$j8iABY5QTClh8VveiXWQjeGenMUaftitg9NKmBy1GfXI.di/rJJv2', 'jx11', '13123456778', '133456789@qq.com', NULL, 'USER', '2025-06-23 23:49:54', '2025-06-23 23:49:54', 0, NULL);
INSERT INTO `user` VALUES (6, 'test000', '$2a$10$yAMRe7rkHTQSOXgNhreyB.pIc5i4b9ma7.2v64jwgVUGDR6Qbj9Q.', '1515', '13123456789', '154165@163.com', NULL, 'USER', '2025-07-01 16:01:10', '2025-07-01 16:01:10', 0, NULL);

SET FOREIGN_KEY_CHECKS = 1;

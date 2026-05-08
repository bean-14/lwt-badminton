/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : lwt

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 08/05/2026 10:47:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for booking
-- ----------------------------
DROP TABLE IF EXISTS `booking`;
CREATE TABLE `booking`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(0) NOT NULL COMMENT 'Student user ID',
  `coach_id` bigint(0) NOT NULL COMMENT 'Coach user ID',
  `venue_id` bigint(0) NOT NULL COMMENT 'Venue ID',
  `schedule_date` date NOT NULL COMMENT 'Booking date',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT 'pending / confirmed / cancelled / leave',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `confirm_time` datetime(0) NULL DEFAULT NULL COMMENT 'Confirmation time',
  `leave_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Leave reason (only for leave status)',
  `leave_time` datetime(0) NULL DEFAULT NULL COMMENT 'Leave request time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_date`(`student_id`, `schedule_date`) USING BTREE,
  INDEX `idx_coach_date`(`coach_id`, `schedule_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Booking record' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of booking
-- ----------------------------
INSERT INTO `booking` VALUES (1, 9, 8, 1, '2026-05-07', 'pending', '2026-05-07 11:23:59', NULL, NULL, NULL);
INSERT INTO `booking` VALUES (2, 10, 8, 2, '2026-05-07', 'pending', '2026-05-07 11:23:59', NULL, NULL, NULL);
INSERT INTO `booking` VALUES (3, 9, 8, 1, '2026-05-07', 'confirmed', '2026-05-07 09:00:00', '2026-05-07 10:30:00', NULL, NULL);
INSERT INTO `booking` VALUES (4, 10, 7, 4, '2026-05-06', 'confirmed', '2026-05-06 16:00:00', '2026-05-06 17:30:00', NULL, NULL);
INSERT INTO `booking` VALUES (5, 9, 8, 1, '2026-05-01', 'cancelled', '2026-05-01 10:00:00', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for coach_schedule
-- ----------------------------
DROP TABLE IF EXISTS `coach_schedule`;
CREATE TABLE `coach_schedule`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `coach_id` bigint(0) NOT NULL COMMENT 'Coach user ID',
  `venue_id` bigint(0) NOT NULL COMMENT 'Venue ID',
  `schedule_date` date NOT NULL COMMENT 'Schedule date',
  `start_time` time(0) NOT NULL COMMENT 'Start time',
  `end_time` time(0) NOT NULL COMMENT 'End time',
  `status` int(0) NULL DEFAULT 1 COMMENT '1=available, 0=disabled',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coach_date`(`coach_id`, `schedule_date`) USING BTREE,
  INDEX `idx_venue_date`(`venue_id`, `schedule_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Coach available schedule' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coach_schedule
-- ----------------------------
INSERT INTO `coach_schedule` VALUES (1, 8, 1, '2026-05-07', '14:00:00', '15:30:00', 1, '2026-05-07 11:23:41');
INSERT INTO `coach_schedule` VALUES (2, 8, 1, '2026-05-07', '16:00:00', '17:30:00', 1, '2026-05-07 11:23:41');
INSERT INTO `coach_schedule` VALUES (3, 8, 2, '2026-05-07', '19:00:00', '20:30:00', 1, '2026-05-07 11:23:41');
INSERT INTO `coach_schedule` VALUES (4, 8, 3, '2026-05-08', '09:00:00', '10:30:00', 1, '2026-05-07 11:23:41');
INSERT INTO `coach_schedule` VALUES (5, 8, 1, '2026-05-08', '14:00:00', '15:30:00', 1, '2026-05-07 11:23:41');
INSERT INTO `coach_schedule` VALUES (6, 7, 4, '2026-05-08', '10:00:00', '11:30:00', 1, '2026-05-07 11:23:41');
INSERT INTO `coach_schedule` VALUES (7, 7, 5, '2026-05-08', '15:00:00', '16:30:00', 1, '2026-05-07 11:23:41');
INSERT INTO `coach_schedule` VALUES (8, 7, 2, '2026-05-09', '18:00:00', '19:30:00', 1, '2026-05-07 11:23:41');

-- ----------------------------
-- Table structure for recharge_record
-- ----------------------------
DROP TABLE IF EXISTS `recharge_record`;
CREATE TABLE `recharge_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT 'User ID',
  `hours` int(0) NOT NULL COMMENT 'Hours recharged',
  `amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT 'Amount (manual record)',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Remark',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Recharge record' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Login username',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MD5 encrypted password',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Display name',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Phone number',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'student / coach / admin',
  `remaining_hours` int(0) NULL DEFAULT 0 COMMENT 'Remaining course hours',
  `status` int(0) NULL DEFAULT 1 COMMENT '1=active, 0=disabled',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `deleted` int(0) NULL DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'User table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (6, 'admin', '0192023a7bbd73250516f069df18b500', 'Administrator', '13800000000', 'admin', 0, 1, '2026-05-01 22:00:06', '2026-05-01 22:00:06', 0);
INSERT INTO `sys_user` VALUES (7, 'coach1', '0521f0f22ec9cd2d9ad48d070fddb419', 'Coach Wang', '13800000001', 'coach', 0, 1, '2026-05-01 22:00:06', '2026-05-01 22:00:06', 0);
INSERT INTO `sys_user` VALUES (8, 'coach2', '0521f0f22ec9cd2d9ad48d070fddb419', 'Coach Li', '13800000002', 'coach', 0, 1, '2026-05-01 22:00:06', '2026-05-01 22:00:06', 0);
INSERT INTO `sys_user` VALUES (9, 'student1', 'ad6a280417a0f533d8b670c61667e1a0', 'Student Zhang', '13800000003', 'student', 10, 1, '2026-05-01 22:00:06', '2026-05-01 22:00:06', 0);
INSERT INTO `sys_user` VALUES (10, 'student2', 'ad6a280417a0f533d8b670c61667e1a0', 'Student Chen', '13800000004', 'student', 5, 1, '2026-05-01 22:00:06', '2026-05-01 22:00:06', 0);

-- ----------------------------
-- Table structure for venue
-- ----------------------------
DROP TABLE IF EXISTS `venue`;
CREATE TABLE `venue`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Venue name, e.g. Court 1',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Location description',
  `status` int(0) NULL DEFAULT 1 COMMENT '1=available, 0=unavailable',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Venue table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of venue
-- ----------------------------
INSERT INTO `venue` VALUES (1, 'Court 1', 'Building A, Floor 1', 1, '2026-05-01 21:31:27');
INSERT INTO `venue` VALUES (2, 'Court 2', 'Building A, Floor 1', 1, '2026-05-01 21:31:27');
INSERT INTO `venue` VALUES (3, 'Court 3', 'Building A, Floor 2', 1, '2026-05-01 21:31:27');
INSERT INTO `venue` VALUES (4, 'Court 4', 'Building B, Floor 1', 1, '2026-05-01 21:31:27');
INSERT INTO `venue` VALUES (5, 'Court 5', 'Building B, Floor 2', 1, '2026-05-01 21:31:27');

SET FOREIGN_KEY_CHECKS = 1;

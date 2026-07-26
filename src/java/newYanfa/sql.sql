-- 库房基础信息表
CREATE TABLE `warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '库房名称',
  `location` varchar(128) DEFAULT NULL COMMENT '库房位置',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 物料入库库存表（唯一入库表，不分明细）
CREATE TABLE `inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `warehouse_id` bigint NOT NULL COMMENT '库房ID',
  `contact_number` varchar(64) NOT NULL COMMENT '联络单号',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) NOT NULL COMMENT '物料名称',
  `material_remark` varchar(255) DEFAULT NULL COMMENT '物料备注',
  `return_date` date DEFAULT NULL COMMENT '归还日期',
  `responsible_foreman` varchar(64) DEFAULT NULL COMMENT '责任工段长',
  `responsible_rd` varchar(64) DEFAULT NULL COMMENT '责任研发',
  `location_code` varchar(64) DEFAULT NULL COMMENT '货位号',
  `bring_out_status` tinyint NOT NULL DEFAULT 1 COMMENT '带出状态 0-不带出 1-带出',
  `in_qty` int NOT NULL DEFAULT 0 COMMENT '入库数量',
  `on_hand_qty` int NOT NULL DEFAULT 0 COMMENT '在库数量',
  `available_qty` int NOT NULL DEFAULT 0 COMMENT '可领用数量',
  `out_qty` int NOT NULL DEFAULT 0 COMMENT '已出库数量',
  `returned_qty` int NOT NULL DEFAULT 0 COMMENT '已退库数量',
  `pending_bring_out_qty` int NOT NULL DEFAULT 0 COMMENT '待带出数量',
  `not_bring_out_qty` int NOT NULL DEFAULT 0 COMMENT '不带出数量',
  `brought_out_qty` int NOT NULL DEFAULT 0 COMMENT '已带出数量',
  `status` varchar(20) NOT NULL DEFAULT 'FIRST_IN' COMMENT '单号状态 FIRST_IN-初次入库 TAKEN-已领用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contact_material` (`contact_number`,`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 领料单主表
CREATE TABLE `order_main` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `applicant` varchar(64) NOT NULL COMMENT '领料员',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态 PENDING-待发货 APPROVED-已发货 REJECTED-已拒绝 CANCELLED-已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 领料单明细表
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单主表ID',
  `inventory_id` bigint NOT NULL COMMENT '对应库存记录ID（批次）',
  `warehouse_id` bigint NOT NULL COMMENT '库房ID',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) NOT NULL COMMENT '物料名称',
  `requested_qty` int NOT NULL COMMENT '申请数量',
  `actual_qty` int DEFAULT NULL COMMENT '实发数量（发货时填写）',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '明细状态',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 人员权限表（关联库房与角色）
CREATE TABLE `user_warehouse_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL COMMENT '用户标识',
  `warehouse_id` bigint NOT NULL COMMENT '库房ID',
  `role` varchar(20) NOT NULL COMMENT '角色 ADMIN-库房管理员 APPLICANT-领料员',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_warehouse` (`user_id`,`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
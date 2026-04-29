-- 产品表
CREATE TABLE `product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '产品名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 接待人表
CREATE TABLE `receptionist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `employee_id` varchar(50) NOT NULL COMMENT '工号',
  `name` varchar(50) NOT NULL,
  `department` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 接待人-产品权限关联表
CREATE TABLE `receptionist_product_permission` (
  `receptionist_id` int(11) NOT NULL,
  `product` varchar(50) NOT NULL COMMENT '产品名称',
  PRIMARY KEY (`receptionist_id`,`product`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 外部门员工表（用于刷卡查询）
CREATE TABLE `internal_staff` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `employee_id` varchar(50) NOT NULL COMMENT '工号',
  `name` varchar(50) NOT NULL,
  `department` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 接待会话表
CREATE TABLE `reception_session` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `receptionist_id` int(11) NOT NULL COMMENT '接待人ID',
  `product` varchar(50) NOT NULL COMMENT '接待产品',
  `reception_time` datetime NOT NULL COMMENT '接待时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 访客接待明细表
CREATE TABLE `visitor_reception` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `session_id` int(11) NOT NULL,
  `visitor_name` varchar(50) NOT NULL,
  `visitor_type` tinyint(1) NOT NULL COMMENT '0:外部门员工,1:外公司人员',
  `employee_id` varchar(50) DEFAULT NULL COMMENT '外部门员工工号',
  `department` varchar(100) DEFAULT NULL,
  `company` varchar(100) DEFAULT NULL COMMENT '外公司名称',
  `id_number` varchar(50) DEFAULT NULL COMMENT '身份证号',
  `checkout_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0:未签离,1:已签离',
  `checkout_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
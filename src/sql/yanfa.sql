-- 库房基础信息表
CREATE TABLE warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '库房名称',
    location VARCHAR(200) COMMENT '位置',
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '库房信息';

-- 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL COMMENT 'ROLE_ADMIN 库房管理员, ROLE_PICKER 领料员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '用户';

-- 用户与库房权限关联（库房管理员管理特定库房）
CREATE TABLE user_warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_warehouse (user_id, warehouse_id)
) COMMENT '用户库房权限';

-- 入库单（联络单号唯一，对应一次入库操作）
CREATE TABLE inbound_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_number VARCHAR(50) NOT NULL UNIQUE COMMENT '联络单号',
    warehouse_id BIGINT NOT NULL COMMENT '入库库房ID',
    operator_id BIGINT NOT NULL COMMENT '操作人（库房管理员）',
    inbound_date DATE NOT NULL COMMENT '入库日期',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '入库单';

-- 入库明细（同时作为库存批次，管理每条物料的完整生命周期）
CREATE TABLE inbound_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inbound_order_id BIGINT NOT NULL COMMENT '入库单ID',
    warehouse_id BIGINT NOT NULL,
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    material_remark VARCHAR(500) COMMENT '物料备注',
    quantity INT NOT NULL COMMENT '初始入库数量',
    current_quantity INT NOT NULL COMMENT '当前剩余数量（在库/可借）',
    return_date DATE COMMENT '归还日期（借出用）',
    responsible_foreman VARCHAR(50) COMMENT '责任人工段长',
    responsible_rd VARCHAR(50) COMMENT '责任研发',
    location_code VARCHAR(50) COMMENT '货位号',
    bring_out_status VARCHAR(20) DEFAULT '不带出' COMMENT '带出状态：待带出/不带出/已带出',
    status VARCHAR(20) DEFAULT '已在库' COMMENT '物料状态：已在库/已出库/已退库',
    pickable TINYINT DEFAULT 1 COMMENT '1-可领用 0-不可领用（退还后置0，仅可借）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_warehouse_code (warehouse_id, material_code)
) COMMENT '入库明细（库存批次）';

-- 出库/入库履历表（记录每一次物料变动）
CREATE TABLE outbound_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    inbound_item_id BIGINT NOT NULL COMMENT '关联的库存批次ID',
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL COMMENT '变动数量（正数出库，负数入库/退库）',
    operation_type VARCHAR(20) NOT NULL COMMENT 'PICK-领用出库, BORROW-借出, RETURN-退库, BORROW_RETURN-借出归还',
    related_order_id BIGINT COMMENT '关联订单ID',
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '物料变动履历';

-- 订单主表（领用单/借料单）
CREATE TABLE order_main (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    order_type VARCHAR(20) NOT NULL COMMENT 'PICK-领用, BORROW-借料',
    warehouse_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL COMMENT '申请人员工ID（领料员）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING-待提交, SUBMITTED-已提交, COMPLETED-已完成, CANCELLED-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '订单主表';

-- 订单明细（购物车条目）
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    inbound_item_id BIGINT NOT NULL COMMENT '库存批次ID',
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    quantity INT NOT NULL COMMENT '申请数量',
    unit VARCHAR(20),
    remark VARCHAR(200)
) COMMENT '订单明细';

-- 退库记录（功能3专用，同时对应履历）
CREATE TABLE return_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    inbound_item_id BIGINT NOT NULL,
    contact_number VARCHAR(50) NOT NULL COMMENT '原始入库联络单号',
    material_code VARCHAR(50) NOT NULL,
    borrower VARCHAR(50) NOT NULL COMMENT '领用人',
    borrowed_quantity INT NOT NULL COMMENT '领用数量',
    material_name VARCHAR(100) NOT NULL,
    return_quantity INT NOT NULL COMMENT '本次退库数量',
    operator_id BIGINT NOT NULL COMMENT '操作管理员',
    return_date DATE NOT NULL,
    remark VARCHAR(500),
    outbound_record_id BIGINT COMMENT '关联的原始领用出库记录ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '退库记录';


-- 入库明细表（整合了原入库单信息，每行一条物料记录）
CREATE TABLE inbound_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_number VARCHAR(50) NOT NULL COMMENT '联络单号（同批次多条物料相同）',
    warehouse_id BIGINT NOT NULL COMMENT '入库库房ID',
    operator_id BIGINT NOT NULL COMMENT '操作人（库房管理员）',
    inbound_date DATE NOT NULL COMMENT '入库日期',
    remark VARCHAR(500) COMMENT '入库备注',
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    material_remark VARCHAR(500) COMMENT '物料备注',
    quantity INT NOT NULL COMMENT '初始入库数量',
    current_quantity INT NOT NULL COMMENT '当前剩余数量',
    return_date DATE COMMENT '归还日期（借出用）',
    responsible_foreman VARCHAR(50) COMMENT '责任人工段长',
    responsible_rd VARCHAR(50) COMMENT '责任研发',
    location_code VARCHAR(50) COMMENT '货位号',
    bring_out_status VARCHAR(20) DEFAULT '不带出' COMMENT '带出状态：待带出/不带出/已带出',
    status VARCHAR(20) DEFAULT '已在库' COMMENT '物料状态：已在库/已出库/已退库',
    pickable TINYINT DEFAULT 1 COMMENT '1-可领用 0-不可领用（退还后置0）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_warehouse_contact (warehouse_id, contact_number),
    INDEX idx_material_code (material_code)
) COMMENT '入库物料明细（单表，含入库单信息）';
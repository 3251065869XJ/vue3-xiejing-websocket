-- 1. 库存表增加借出相关字段
ALTER TABLE inventory 
ADD COLUMN borrowable_qty INT NOT NULL DEFAULT 0 COMMENT '可借数量',
ADD COLUMN borrowed_qty INT NOT NULL DEFAULT 0 COMMENT '已借出数量';

-- 2. 借料单主表
CREATE TABLE borrow_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '借料单号',
  applicant VARCHAR(64) NOT NULL COMMENT '借料人',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING-待审批 APPROVED-已借出 REJECTED-已拒绝 CANCELLED-已取消 RETURNED-已归还',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. 借料单明细表
CREATE TABLE borrow_order_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  borrow_order_id BIGINT NOT NULL COMMENT '借料单ID',
  inventory_id BIGINT NOT NULL COMMENT '库存批次ID',
  warehouse_id BIGINT NOT NULL,
  material_code VARCHAR(64) NOT NULL,
  material_name VARCHAR(128) NOT NULL,
  requested_qty INT NOT NULL COMMENT '申请数量',
  actual_qty INT DEFAULT NULL COMMENT '实借数量',
  returned_qty INT NOT NULL DEFAULT 0 COMMENT '已归还数量',
  status VARCHAR(20) DEFAULT 'PENDING',
  KEY idx_borrow_order_id (borrow_order_id)
);

-- 4. 库存交易日志表（记录每一次数量变动）
CREATE TABLE inventory_transaction_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inventory_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  material_code VARCHAR(64) NOT NULL,
  change_type VARCHAR(30) NOT NULL COMMENT '变动类型：INBOUND, OUT_ORDER, RETURN, BORROW_OUT, BORROW_RETURN, CANCEL_ORDER, REJECT_ORDER, SHIP_DIFF',
  change_qty INT NOT NULL COMMENT '变动数量（正表示增加，负表示减少）',
  before_on_hand INT, after_on_hand INT,
  before_available INT, after_available INT,
  before_borrowable INT, after_borrowable INT,
  before_borrowed INT, after_borrowed INT,
  reference_no VARCHAR(64) COMMENT '关联单号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
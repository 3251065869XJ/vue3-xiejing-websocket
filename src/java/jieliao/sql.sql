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

====================
SELECT
    r.reservation_no,
    r.employee_id,
    r.employee_name,
    r.depot_id,
    de.depot_name,
    de.duty_dept_name,
    r.receive_time,
    r.shipment_time,
    r.shipment_remark,
    r.return_reason,
    r.l4_organization_name,
    r.l4_org_code,
    r.organization_name,
    r.org_code,
    r.create_time,
    r.last_update_by,
    r.last_update_time,
    r.remark,
    r.status,
    r.return_date,
    r.use_address,
    rd.po_num,
    rd.goods_id,
    rd.goods_name,
    rd.quantity,
    in_rec.out_status,
    ret_rec.out_order,
    IFNULL(ret_rec.return_qty, 0) AS return_qty,
    CASE
        WHEN IFNULL(ret_rec.return_qty, 0) < rd.quantity THEN 1
        WHEN IFNULL(ret_rec.return_qty, 0) = rd.quantity THEN 2
        ELSE 0
    END AS is_return
FROM w_rdm_reservation AS r
INNER JOIN w_rdm_reservation_detail AS rd
    ON rd.reservation_no = r.reservation_no
LEFT JOIN w_rdm_depot AS de
    ON de.depot_id = r.depot_id

-- change_type = 1：入库记录，取 out_status
LEFT JOIN
(
    SELECT
        po_num,
        goods_id,
        MAX(out_status) AS out_status
    FROM w_rdm_material_stock_record
    WHERE change_type = 1
      AND depot_id IN (252, 254)
    GROUP BY po_num, goods_id
) AS in_rec
    ON in_rec.po_num = rd.po_num
   AND in_rec.goods_id = rd.goods_id

-- change_type = 3：退库记录，聚合成一行
LEFT JOIN
(
    SELECT
        reservation_no,
        po_num,
        goods_id,
        GROUP_CONCAT(DISTINCT out_order ORDER BY out_order SEPARATOR ',') AS out_order,
        SUM(quantity) AS return_qty
    FROM w_rdm_material_stock_record
    WHERE change_type = 3
      AND depot_id IN (252, 254)
    GROUP BY reservation_no, po_num, goods_id
) AS ret_rec
    ON ret_rec.reservation_no = rd.reservation_no
   AND ret_rec.po_num = rd.po_num
   AND ret_rec.goods_id = rd.goods_id

WHERE r.create_time >= '2026-01-01'  -- 建议根据实际过滤条件添加
ORDER BY r.create_time DESC
LIMIT 1000;  -- 建议分页

==============
索引建议
假设你的表是 InnoDB，以下是建议添加的索引。

1. 明细表 w_rdm_reservation_detail
sql
ALTER TABLE w_rdm_reservation_detail
  ADD UNIQUE KEY uk_detail_res_po_goods (reservation_no, po_num, goods_id);
作用：

保证 reservation_no + po_num + goods_id 唯一。

同时可以加速主表 reservation_no 关联明细表。

如果该表已经有业务主键或唯一键，就不要再重复添加。

2. 库存流水表 w_rdm_material_stock_record
入库子查询需要按 po_num + goods_id 聚合，并且查 change_type = 1。

sql
ALTER TABLE w_rdm_material_stock_record
  ADD KEY idx_stock_in (change_type, depot_id, po_num, goods_id, out_status);
退库子查询需要按 reservation_no + po_num + goods_id 聚合，并且查 change_type = 3，同时要取 out_order 和 quantity。

sql
ALTER TABLE w_rdm_material_stock_record
  ADD KEY idx_stock_return (change_type, depot_id, reservation_no, po_num, goods_id, out_order, quantity);
说明：

change_type 放最左边，因为是等值过滤。

depot_id 用于 IN 过滤。

后面跟上聚合字段，让 MySQL 尽量走覆盖索引，减少回表。

如果库存流水表的数量字段不叫 quantity，请替换成实际字段名。

3. 主表 w_rdm_reservation
如果你会按创建时间范围查询，建议加：

sql
ALTER TABLE w_rdm_reservation
  ADD KEY idx_reservation_create_time (create_time);
如果你还经常按库房和时间过滤：

sql
ALTER TABLE w_rdm_reservation
  ADD KEY idx_reservation_depot_create_time (depot_id, create_time);
4. 库房表 w_rdm_depot
一般 depot_id 是主键，不需要额外加索引。如果不是主键，建议：

sql
ALTER TABLE w_rdm_depot
  ADD PRIMARY KEY (depot_id);
重要注意事项
如果一次查询返回几十万条数据，1 秒内不现实。
建议务必加上时间范围、库房范围、状态等过滤条件，并使用 LIMIT 分页。否则光网络传输和结果集解析就可能超过 1 秒。

GROUP_CONCAT 长度限制
MySQL 5.7 默认 group_concat_max_len 是 1024。如果退库单号很多或很长，可能会被截断。可以在会话中设置：

sql
SET SESSION group_concat_max_len = 10240;
确认字段名
我假设库存流水表中有 reservation_no 字段和 quantity 数量字段。
如果实际字段名不同，比如退库数量字段叫 return_quantity，需要替换。

入库 out_status 如果有多条记录
同一个 po_num + goods_id 的 change_type = 1 入库记录理论上应该只有一条。如果有多条，我上面使用了 MAX(out_status)。
如果你有更明确的最新记录规则，比如按主键或创建时间取最新一条，可以再优化。

执行计划验证
索引加完后，用 EXPLAIN 看执行计划：

sql
EXPLAIN
SELECT ... -- 优化后的 SQL
重点看 w_rdm_material_stock_record 两个子查询是否走索引，最好出现 Using index。

如果你能提供具体的过滤条件，比如时间范围、库房范围、状态等，我可以再帮你进一步收缩 SQL。
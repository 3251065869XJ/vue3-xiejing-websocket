@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {
    @Autowired
    private OrderMainMapper orderMainMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private InventoryService inventoryService; // 用于查询
    
    /**
     * 领料员提交领料单
     * 核心逻辑：
     * 1. 根据库房和物料编码，查询所有可用库存批次（available_qty>0），按创建时间排序
     * 2. 按先进先出原则扣减每个批次的available_qty，直到满足申请数量
     * 3. 生成订单主表和明细表，明细关联具体批次
     * 4. 若库存不足，整体回滚
     */
    public OrderMain submitOrder(String applicant, OrderSubmitDTO dto) {
        // 生成订单号
        String orderNo = "ORD" + System.currentTimeMillis();
        OrderMain order = new OrderMain();
        order.setOrderNo(orderNo);
        order.setApplicant(applicant);
        order.setStatus("PENDING");
        orderMainMapper.insert(order);
        
        for (OrderItemDTO item : dto.getItems()) {
            int remaining = item.getRequestedQty();
            // 查询该物料在指定库房的所有可用批次（available_qty > 0），按创建时间升序（先进先出）
            List<Inventory> availableBatches = inventoryMapper.selectList(
                    new LambdaQueryWrapper<Inventory>()
                            .eq(Inventory::getWarehouseId, item.getWarehouseId())
                            .eq(Inventory::getMaterialCode, item.getMaterialCode())
                            .gt(Inventory::getAvailableQty, 0)
                            .orderByAsc(Inventory::getCreateTime)
            );
            if (availableBatches.isEmpty()) {
                throw new BusinessException("物料 " + item.getMaterialCode() + " 在库房无可用库存");
            }
            // 遍历批次，扣减
            for (Inventory batch : availableBatches) {
                if (remaining <= 0) break;
                int available = batch.getAvailableQty();
                int deduct = Math.min(available, remaining);
                // 扣减库存
                batch.setAvailableQty(available - deduct);
                // 注意：此时不更新onHandQty，真正出库在发货时处理
                inventoryMapper.updateById(batch);
                // 创建订单明细，记录扣减的批次和数量
                OrderDetail detail = new OrderDetail();
                detail.setOrderId(order.getId());
                detail.setInventoryId(batch.getId());
                detail.setWarehouseId(batch.getWarehouseId());
                detail.setMaterialCode(batch.getMaterialCode());
                detail.setMaterialName(batch.getMaterialName());
                detail.setRequestedQty(deduct);
                detail.setStatus("PENDING");
                orderDetailMapper.insert(detail);
                remaining -= deduct;
            }
            if (remaining > 0) {
                throw new BusinessException("物料 " + item.getMaterialCode() + " 库存不足，无法下单");
            }
        }
        return order;
    }
    
    /**
     * 取消订单（领料员操作）
     * 将扣减的可领用数量全部回加
     */
    public void cancelOrder(Long orderId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待发货订单可取消");
        }
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(orderId);
        for (OrderDetail detail : details) {
            // 回加库存可用数量
            Inventory batch = inventoryMapper.selectById(detail.getInventoryId());
            if (batch != null) {
                batch.setAvailableQty(batch.getAvailableQty() + detail.getRequestedQty());
                inventoryMapper.updateById(batch);
            }
        }
        order.setStatus("CANCELLED");
        orderMainMapper.updateById(order);
        // 明细状态同步
        for (OrderDetail d : details) {
            d.setStatus("CANCELLED");
            orderDetailMapper.updateById(d);
        }
    }
    
    /**
     * 库房管理员拒绝发货
     * 逻辑同取消，将扣减的可用数量加回
     */
    public void rejectOrder(Long orderId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待发货订单可拒绝");
        }
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(orderId);
        for (OrderDetail detail : details) {
            Inventory batch = inventoryMapper.selectById(detail.getInventoryId());
            if (batch != null) {
                batch.setAvailableQty(batch.getAvailableQty() + detail.getRequestedQty());
                inventoryMapper.updateById(batch);
            }
        }
        order.setStatus("REJECTED");
        orderMainMapper.updateById(order);
        for (OrderDetail d : details) {
            d.setStatus("REJECTED");
            orderDetailMapper.updateById(d);
        }
    }
    
    /**
     * 发货（支持修改实发数量）
     * 1. 对每个明细，比较实发数量与申请数量
     * 2. 若实发 < 申请，差额回加到库存可用数量
     * 3. 更新库存：onHandQty减少实发数量，outQty增加实发数量
     * 4. 更新明细实发数量和状态
     */
    public void shipOrder(ShipmentDTO shipment) {
        OrderMain order = orderMainMapper.selectById(shipment.getOrderId());
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待发货订单可发货");
        }
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(shipment.getOrderId());
        // 将发货数据转为Map便于查找
        Map<Long, Integer> actualMap = shipment.getItems().stream()
                .collect(Collectors.toMap(ShipmentItemDTO::getDetailId, ShipmentItemDTO::getActualQty));
        
        for (OrderDetail detail : details) {
            Integer requested = detail.getRequestedQty();
            Integer actual = actualMap.get(detail.getId());
            if (actual == null) {
                throw new BusinessException("缺少明细 " + detail.getId() + " 的实发数量");
            }
            if (actual > requested) {
                throw new BusinessException("实发数量不能大于申请数量");
            }
            // 差额处理
            int diff = requested - actual;
            Inventory batch = inventoryMapper.selectById(detail.getInventoryId());
            if (batch == null) {
                throw new BusinessException("库存批次不存在");
            }
            if (diff > 0) {
                // 少发，回加可用数量
                batch.setAvailableQty(batch.getAvailableQty() + diff);
            }
            // 更新在库数量（出库减少）
            batch.setOnHandQty(batch.getOnHandQty() - actual);
            batch.setOutQty(batch.getOutQty() + actual);
            // 若该批次在库数量降为0，状态可更新（业务自行定义）
            inventoryMapper.updateById(batch);
            
            // 更新明细
            detail.setActualQty(actual);
            detail.setStatus("SHIPPED");
            orderDetailMapper.updateById(detail);
        }
        order.setStatus("APPROVED");
        orderMainMapper.updateById(order);
    }
}
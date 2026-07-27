@Service
@Transactional(rollbackFor = Exception.class)
public class BorrowOrderService {
    @Autowired
    private BorrowOrderMapper borrowOrderMapper;
    @Autowired
    private BorrowOrderDetailMapper borrowDetailMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private InventoryLogHelper logHelper;

    /**
     * 借料申请：下单时扣减 borrowable_qty（冻结），生成订单与明细
     * 逻辑与普通领料类似，但查询条件为 borrowable_qty > 0
     */
    public BorrowOrder submitBorrow(String applicant, BorrowOrderSubmitDTO dto) {
        String orderNo = "BOR" + System.currentTimeMillis();
        BorrowOrder order = new BorrowOrder();
        order.setOrderNo(orderNo);
        order.setApplicant(applicant);
        order.setStatus("PENDING");
        borrowOrderMapper.insert(order);

        for (BorrowItemDTO item : dto.getItems()) {
            int remaining = item.getRequestedQty();
            List<Inventory> batches = inventoryMapper.selectList(
                    new LambdaQueryWrapper<Inventory>()
                            .eq(Inventory::getWarehouseId, item.getWarehouseId())
                            .eq(Inventory::getMaterialCode, item.getMaterialCode())
                            .gt(Inventory::getBorrowableQty, 0)
                            .orderByAsc(Inventory::getCreateTime));
            if (batches.isEmpty()) {
                throw new BusinessException("物料 " + item.getMaterialCode() + " 无可借库存");
            }
            for (Inventory batch : batches) {
                if (remaining <= 0) break;
                int borrowable = batch.getBorrowableQty();
                int deduct = Math.min(borrowable, remaining);
                // 扣减可借数量（冻结）
                batch.setBorrowableQty(borrowable - deduct);
                inventoryMapper.updateById(batch);
                logHelper.log(batch, "BORROW_FREEZE", -deduct, orderNo);
                // 生成明细
                BorrowOrderDetail detail = new BorrowOrderDetail();
                detail.setBorrowOrderId(order.getId());
                detail.setInventoryId(batch.getId());
                detail.setWarehouseId(batch.getWarehouseId());
                detail.setMaterialCode(batch.getMaterialCode());
                detail.setMaterialName(batch.getMaterialName());
                detail.setRequestedQty(deduct);
                detail.setStatus("PENDING");
                borrowDetailMapper.insert(detail);
                remaining -= deduct;
            }
            if (remaining > 0) throw new BusinessException("物料 " + item.getMaterialCode() + " 可借数量不足");
        }
        return order;
    }

    /**
     * 取消借料单：解冻可借数量
     */
    public void cancelBorrow(Long orderId) {
        BorrowOrder order = borrowOrderMapper.selectById(orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待审批的借料单可取消");
        }
        List<BorrowOrderDetail> details = borrowDetailMapper.selectByBorrowOrderId(orderId);
        for (BorrowOrderDetail detail : details) {
            Inventory batch = inventoryMapper.selectById(detail.getInventoryId());
            if (batch != null) {
                batch.setBorrowableQty(batch.getBorrowableQty() + detail.getRequestedQty());
                inventoryMapper.updateById(batch);
                logHelper.log(batch, "BORROW_UNFREEZE", detail.getRequestedQty(), order.getOrderNo());
            }
            detail.setStatus("CANCELLED");
            borrowDetailMapper.updateById(detail);
        }
        order.setStatus("CANCELLED");
        borrowOrderMapper.updateById(order);
    }

    /**
     * 拒绝借料单：同取消，解冻
     */
    public void rejectBorrow(Long orderId) {
        cancelBorrow(orderId); // 逻辑相同
        BorrowOrder order = borrowOrderMapper.selectById(orderId);
        order.setStatus("REJECTED");
        borrowOrderMapper.updateById(order);
    }

    /**
     * 借料发货：根据实借数量减少在库、增加已借出，处理少发解冻差额
     */
    public void shipBorrow(BorrowShipmentDTO shipment) {
        BorrowOrder order = borrowOrderMapper.selectById(shipment.getBorrowOrderId());
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待审批借料单可发货");
        }
        List<BorrowOrderDetail> details = borrowDetailMapper.selectByBorrowOrderId(shipment.getBorrowOrderId());
        Map<Long, Integer> actualMap = shipment.getItems().stream()
                .collect(Collectors.toMap(BorrowShipmentItemDTO::getDetailId, BorrowShipmentItemDTO::getActualQty));

        for (BorrowOrderDetail detail : details) {
            Integer requested = detail.getRequestedQty();
            Integer actual = actualMap.get(detail.getId());
            if (actual == null) throw new BusinessException("缺少明细 " + detail.getId() + " 实借数量");
            if (actual > requested) throw new BusinessException("实借数量不能大于申请数量");
            int diff = requested - actual;
            Inventory batch = inventoryMapper.selectById(detail.getInventoryId());
            if (batch == null) throw new BusinessException("库存批次不存在");
            // 处理少发：差额解冻，加回 borrowable
            if (diff > 0) {
                batch.setBorrowableQty(batch.getBorrowableQty() + diff);
                logHelper.log(batch, "BORROW_DIFF_UNFREEZE", diff, order.getOrderNo());
            }
            // 库存变动：在库减少，已借出增加
            batch.setOnHandQty(batch.getOnHandQty() - actual);
            batch.setBorrowedQty(batch.getBorrowedQty() + actual);
            inventoryMapper.updateById(batch);
            logHelper.log(batch, "BORROW_OUT", -actual, order.getOrderNo());

            detail.setActualQty(actual);
            detail.setStatus("APPROVED");
            borrowDetailMapper.updateById(detail);
        }
        order.setStatus("APPROVED");
        borrowOrderMapper.updateById(order);
    }

    /**
     * 借料归还：可分批归还，增加在库和可借数量，减少已借出
     * 支持同一物料多次归还，自动更新累计归还量
     */
    public void returnBorrow(BorrowReturnDTO dto) {
        BorrowOrder order = borrowOrderMapper.selectById(dto.getBorrowOrderId());
        if (order == null || !"APPROVED".equals(order.getStatus())) {
            throw new BusinessException("仅已借出状态的借料单可归还");
        }
        // 查找该借料单下对应物料的明细（可能有多个批次，简化取第一个未还完的）
        List<BorrowOrderDetail> details = borrowDetailMapper.selectList(
                new LambdaQueryWrapper<BorrowOrderDetail>()
                        .eq(BorrowOrderDetail::getBorrowOrderId, dto.getBorrowOrderId())
                        .eq(BorrowOrderDetail::getMaterialCode, dto.getMaterialCode())
                        .eq(BorrowOrderDetail::getStatus, "APPROVED"));
        if (details.isEmpty()) {
            throw new BusinessException("未找到可归还的借料明细");
        }
        int remaining = dto.getReturnQty();
        for (BorrowOrderDetail detail : details) {
            int canReturn = detail.getActualQty() - detail.getReturnedQty();
            if (canReturn <= 0) continue;
            int returnNow = Math.min(canReturn, remaining);
            // 更新明细已归还数量
            detail.setReturnedQty(detail.getReturnedQty() + returnNow);
            if (detail.getReturnedQty().equals(detail.getActualQty())) {
                detail.setStatus("RETURNED");
            }
            borrowDetailMapper.updateById(detail);
            // 更新库存
            Inventory batch = inventoryMapper.selectById(detail.getInventoryId());
            batch.setOnHandQty(batch.getOnHandQty() + returnNow);
            batch.setBorrowedQty(batch.getBorrowedQty() - returnNow);
            batch.setBorrowableQty(batch.getBorrowableQty() + returnNow); // 归还后继续可借
            inventoryMapper.updateById(batch);
            logHelper.log(batch, "BORROW_RETURN", returnNow, order.getOrderNo());
            remaining -= returnNow;
            if (remaining == 0) break;
        }
        if (remaining > 0) {
            throw new BusinessException("归还数量超过未还数量");
        }
        // 检查是否全部归还，更新主单状态
        boolean allReturned = borrowDetailMapper.selectByBorrowOrderId(dto.getBorrowOrderId())
                .stream().allMatch(d -> "RETURNED".equals(d.getStatus()));
        if (allReturned) {
            order.setStatus("RETURNED");
            borrowOrderMapper.updateById(order);
        }
    }
}


原有 OrderService 中补充日志记录
在 OrderService 的关键库存变动处添加 logHelper.log(...)，确保普通领用的出库、取消、拒绝、发货差异均有日志。示例：

java
// 下单扣减后
logHelper.log(batch, "ORDER_FREEZE", -deduct, orderNo);
// 取消回加
logHelper.log(batch, "ORDER_UNFREEZE", detail.getRequestedQty(), order.getOrderNo());
// 发货在库减少
logHelper.log(batch, "OUT_ORDER", -actual, order.getOrderNo());
// 发货差额回加
logHelper.log(batch, "SHIP_DIFF_UNFREEZE", diff, order.getOrderNo());
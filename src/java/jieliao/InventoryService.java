// 在 InventoryService 中修改 returnMaterial 方法
public void returnMaterial(ReturnDTO dto) {
    Inventory inv = inventoryMapper.selectByContactAndMaterial(
            dto.getContactNumber(), dto.getMaterialCode());
    if (inv == null) throw new BusinessException("库存记录不存在");

    int returnQty = dto.getReturnQty();
    // 更新库存
    inv.setOnHandQty(inv.getOnHandQty() + returnQty);
    inv.setReturnedQty(inv.getReturnedQty() + returnQty);
    // 退还后普通可领用数量置0，转为可借出
    inv.setAvailableQty(0);
    inv.setBorrowableQty(inv.getBorrowableQty() + returnQty); // 可借数量增加
    if ("FIRST_IN".equals(inv.getStatus())) {
        inv.setStatus("TAKEN");
    }
    inventoryMapper.updateById(inv);

    // 记录日志
    logHelper.log(inv, "RETURN", returnQty, dto.getContactNumber());
}
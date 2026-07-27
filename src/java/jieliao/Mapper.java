public interface BorrowOrderMapper extends BaseMapper<BorrowOrder> {}
public interface BorrowOrderDetailMapper extends BaseMapper<BorrowOrderDetail> {
    default List<BorrowOrderDetail> selectByBorrowOrderId(Long borrowOrderId) {
        return selectList(new LambdaQueryWrapper<BorrowOrderDetail>()
                .eq(BorrowOrderDetail::getBorrowOrderId, borrowOrderId));
    }
    default BorrowOrderDetail selectByBorrowOrderIdAndMaterial(Long orderId, String materialCode) {
        return selectOne(new LambdaQueryWrapper<BorrowOrderDetail>()
                .eq(BorrowOrderDetail::getBorrowOrderId, orderId)
                .eq(BorrowOrderDetail::getMaterialCode, materialCode));
    }
}
public interface InventoryTransactionLogMapper extends BaseMapper<InventoryTransactionLog> {}
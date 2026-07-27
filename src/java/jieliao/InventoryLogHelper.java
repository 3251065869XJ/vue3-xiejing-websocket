@Component
public class InventoryLogHelper {
    @Autowired
    private InventoryTransactionLogMapper logMapper;

    /**
     * 记录库存变动日志
     */
    public void log(Inventory inv, String changeType, Integer changeQty,
                    String referenceNo) {
        InventoryTransactionLog log = new InventoryTransactionLog();
        log.setInventoryId(inv.getId());
        log.setWarehouseId(inv.getWarehouseId());
        log.setMaterialCode(inv.getMaterialCode());
        log.setChangeType(changeType);
        log.setChangeQty(changeQty);
        log.setBeforeOnHand(inv.getOnHandQty() - changeQty); // 需在调用前保留旧值，此处简化
        log.setAfterOnHand(inv.getOnHandQty());
        log.setBeforeAvailable(inv.getAvailableQty() - (changeType.contains("AVAILABLE") ? changeQty : 0));
        log.setAfterAvailable(inv.getAvailableQty());
        log.setBeforeBorrowable(inv.getBorrowableQty() - (changeType.contains("BORROWABLE") ? changeQty : 0));
        log.setAfterBorrowable(inv.getBorrowableQty());
        log.setBeforeBorrowed(inv.getBorrowedQty() - (changeType.contains("BORROWED") ? changeQty : 0));
        log.setAfterBorrowed(inv.getBorrowedQty());
        log.setReferenceNo(referenceNo);
        logMapper.insert(log);
    }
}
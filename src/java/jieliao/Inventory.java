@Data
@TableName("inventory")
public class Inventory {
    // ... 原有字段 ...
    private Integer borrowableQty;
    private Integer borrowedQty;
}

// BorrowOrder.java
@Data
@TableName("borrow_order")
public class BorrowOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String applicant;
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED, RETURNED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// BorrowOrderDetail.java
@Data
@TableName("borrow_order_detail")
public class BorrowOrderDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long borrowOrderId;
    private Long inventoryId;
    private Long warehouseId;
    private String materialCode;
    private String materialName;
    private Integer requestedQty;
    private Integer actualQty;
    private Integer returnedQty;
    private String status;
}

// InventoryTransactionLog.java
@Data
@TableName("inventory_transaction_log")
public class InventoryTransactionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long inventoryId;
    private Long warehouseId;
    private String materialCode;
    private String changeType;
    private Integer changeQty;
    private Integer beforeOnHand;
    private Integer afterOnHand;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private Integer beforeBorrowable;
    private Integer afterBorrowable;
    private Integer beforeBorrowed;
    private Integer afterBorrowed;
    private String referenceNo;
    private LocalDateTime createTime;
}
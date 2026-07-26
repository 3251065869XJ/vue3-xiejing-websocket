// Warehouse.java
@Data
@TableName("warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String location;
    private LocalDateTime createTime;
}

// Inventory.java - 核心库存实体
@Data
@TableName("inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warehouseId;
    private String contactNumber;
    private String materialCode;
    private String materialName;
    private String materialRemark;
    private LocalDate returnDate;
    private String responsibleForeman;
    private String responsibleRd;
    private String locationCode;
    private Integer bringOutStatus; // 0不带出 1带出
    private Integer inQty;
    private Integer onHandQty;
    private Integer availableQty;
    private Integer outQty;
    private Integer returnedQty;
    private Integer pendingBringOutQty;
    private Integer notBringOutQty;
    private Integer broughtOutQty;
    private String status; // FIRST_IN, TAKEN
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// OrderMain.java
@Data
@TableName("order_main")
public class OrderMain {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String applicant;
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// OrderDetail.java
@Data
@TableName("order_detail")
public class OrderDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long inventoryId;
    private Long warehouseId;
    private String materialCode;
    private String materialName;
    private Integer requestedQty;
    private Integer actualQty;
    private String status;
}

// UserWarehouseRole.java
@Data
@TableName("user_warehouse_role")
public class UserWarehouseRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private Long warehouseId;
    private String role; // ADMIN, APPLICANT
}
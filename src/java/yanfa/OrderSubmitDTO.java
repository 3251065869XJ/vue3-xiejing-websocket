// 领用下单DTO
@Data
public class OrderSubmitDTO {
    private Long warehouseId;
    private String orderType;   // PICK / BORROW
    private List<OrderItemDTO> items;
}

@Data
public class OrderItemDTO {
    private Long inboundItemId; // 选择的具体库存批次
    private Integer quantity;
}

// 退库录入DTO
@Data
public class ReturnInputDTO {
    private String contactNumber;   // 入库联络单号
    private String materialCode;    // 物料编码
    // 以下通过联动自动填充
    private Integer returnQuantity; // 退库数量
}
// 入库请求DTO
@Data
public class InboundDTO {
    @NotBlank(message = "联络单号不能为空")
    private String contactNumber;
    @NotEmpty(message = "物料列表不能为空")
    private List<InboundItemDTO> items;
}

@Data
public class InboundItemDTO {
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String materialRemark;
    @NotNull(message = "入库数量不能为空")
    private Integer inQty;
    @NotNull(message = "带出状态不能为空")
    private Integer bringOutStatus; // 0或1
    private LocalDate returnDate;
    private String responsibleForeman;
    private String responsibleRd;
    private String locationCode;
    @NotNull(message = "库房ID不能为空")
    private Long warehouseId;
}

// 更新入库数量DTO
@Data
public class InventoryUpdateDTO {
    @NotBlank
    private String contactNumber;
    @NotBlank
    private String materialCode;
    @NotNull
    private Integer newInQty;
}

// 领料下单请求DTO
@Data
public class OrderSubmitDTO {
    @NotEmpty(message = "明细不能为空")
    private List<OrderItemDTO> items;
}

@Data
public class OrderItemDTO {
    @NotNull
    private Long warehouseId;
    @NotBlank
    private String materialCode;
    @NotNull
    private Integer requestedQty;
}

// 发货请求DTO（包含实际发货数量）
@Data
public class ShipmentDTO {
    @NotNull
    private Long orderId;
    @NotEmpty
    private List<ShipmentItemDTO> items;
}

@Data
public class ShipmentItemDTO {
    @NotNull
    private Long detailId; // 订单明细ID
    @NotNull
    private Integer actualQty; // 实发数量
}

// 退还请求DTO
@Data
public class ReturnDTO {
    @NotBlank
    private String contactNumber;
    @NotBlank
    private String materialCode;
    @NotBlank
    private String applicant; // 领用人
    @NotNull
    private Integer returnQty; // 退库数量
}
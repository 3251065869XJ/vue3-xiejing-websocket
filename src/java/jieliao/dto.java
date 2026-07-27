// 借料申请请求
@Data
public class BorrowOrderSubmitDTO {
    @NotEmpty
    private List<BorrowItemDTO> items;
}
@Data
public class BorrowItemDTO {
    @NotNull private Long warehouseId;
    @NotBlank private String materialCode;
    @NotNull @Min(1) private Integer requestedQty;
}

// 借料发货请求
@Data
public class BorrowShipmentDTO {
    @NotNull private Long borrowOrderId;
    @NotEmpty private List<BorrowShipmentItemDTO> items;
}
@Data
public class BorrowShipmentItemDTO {
    @NotNull private Long detailId;
    @NotNull @Min(0) private Integer actualQty;
}

// 借料归还请求
@Data
public class BorrowReturnDTO {
    @NotNull private Long borrowOrderId;
    @NotBlank private String materialCode;
    @NotNull @Min(1) private Integer returnQty;
}
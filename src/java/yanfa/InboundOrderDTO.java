// 入库请求DTO - 支持一次性多条物料
@Data
public class InboundOrderDTO {
    private String contactNumber;
    private Long warehouseId;
    private LocalDate inboundDate;
    private String remark;
    private List<InboundItemDTO> items;
}

@Data
public class InboundItemDTO {
    private String materialCode;
    private String materialName;
    private String materialRemark;
    private Integer quantity;
    private LocalDate returnDate;
    private String responsibleForeman;
    private String responsibleRd;
    private String locationCode;
    private String bringOutStatus; // 待带出/不带出/已带出
}
// InboundOrder.java
@Data
@TableName("inbound_order")
public class InboundOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contactNumber;
    private Long warehouseId;
    private Long operatorId;
    private LocalDate inboundDate;
    private String remark;
    private LocalDateTime createTime;
}
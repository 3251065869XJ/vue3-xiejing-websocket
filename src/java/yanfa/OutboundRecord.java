// OutboundRecord.java
@Data
@TableName("outbound_record")
public class OutboundRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warehouseId;
    private Long inboundItemId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private String operationType; // PICK / BORROW / RETURN / BORROW_RETURN
    private Long relatedOrderId;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private LocalDateTime createTime;
}
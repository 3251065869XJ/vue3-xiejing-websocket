// Warehouse.java
@Data
@TableName("warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String location;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
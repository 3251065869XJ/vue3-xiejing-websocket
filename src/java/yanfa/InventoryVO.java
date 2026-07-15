package com.example.material.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InventoryVO {
    private Long id;                 // inbound_item 主键
    private String contactNumber;   // 直接取自同一张表
    private Long warehouseId;
    private String warehouseName;   // 需联表 warehouse 查询
    private String materialCode;
    private String materialName;
    private Integer currentQuantity;
    private String status;
    private String bringOutStatus;
    private String locationCode;
    private Integer pickable;
    private LocalDate inboundDate;
    private String remark;
}

@Select("SELECT i.*, w.name as warehouse_name " +
        "FROM inbound_item i " +
        "LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
        "WHERE i.current_quantity > 0 AND i.status = '已在库'")
List<InventoryVO> selectAvailableInventory();

// 库存查询VO
@Data
public class InventoryVO {
    private Long inboundItemId;
    private String warehouseName;
    private String contactNumber;
    private String materialCode;
    private String materialName;
    private Integer currentQuantity;
    private String status;
    private String bringOutStatus;
    private String locationCode;
    private Integer pickable;
    // ... 其他展示字段
}

// 退库联动VO
@Data
public class ReturnLinkageVO {
    private String warehouseName;
    private String borrower;        // 领用人
    private Integer borrowedQuantity; // 领用数量
    private String materialName;
    private Long outboundRecordId;  // 用于记录关联出库
}


public interface InboundOrderMapper extends BaseMapper<InboundOrder> {}
public interface InboundItemMapper extends BaseMapper<InboundItem> {
    // 自定义库存查询，联表
    @Select("SELECT i.*, w.name as warehouseName, o.contact_number as contactNumber " +
            "FROM inbound_item i " +
            "LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
            "LEFT JOIN inbound_order o ON i.inbound_order_id = o.id " +
            "WHERE i.status = '已在库' AND i.current_quantity > 0")
    List<InventoryVO> selectAvailableInventory();
}
// 其他Mapper类似创建
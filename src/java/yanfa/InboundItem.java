package com.example.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inbound_item")
public class InboundItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 原入库单字段
    private String contactNumber;      // 联络单号
    private Long warehouseId;          // 入库库房ID
    private Long operatorId;           // 操作人（库房管理员ID）
    private LocalDate inboundDate;     // 入库日期
    private String remark;             // 入库备注

    // 物料明细字段
    private String materialCode;
    private String materialName;
    private String materialRemark;
    private Integer quantity;          // 初始入库数量
    private Integer currentQuantity;   // 当前剩余数量
    private LocalDate returnDate;
    private String responsibleForeman;
    private String responsibleRd;
    private String locationCode;
    private String bringOutStatus;     // 待带出/不带出/已带出
    private String status;             // 已在库/已出库/已退库
    private Integer pickable;          // 1-可领用 0-不可领用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
// AttendanceSummaryVO.java
package com.example.attendance.vo;

import lombok.Data;
import java.util.List;

@Data
public class AttendanceSummaryVO {
    private String id;                 // 部门ID作为唯一标识
    private String deptName;
    // 白班指标
    private Integer daySystemManpower;
    private Integer dayRequiredManpower;
    private Integer dayActualAttendance;
    private Integer dayInventoryManpower;
    private Integer dayReceiveCount;
    private Integer daySupportCount;
    // 夜班指标
    private Integer nightSystemManpower;
    private Integer nightRequiredManpower;
    private Integer nightActualAttendance;
    private Integer nightInventoryManpower;
    private Integer nightReceiveCount;
    private Integer nightSupportCount;
    // 子节点
    private List<AttendanceSummaryVO> children;
}
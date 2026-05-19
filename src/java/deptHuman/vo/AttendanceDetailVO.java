// AttendanceDetailVO.java
package com.example.attendance.vo;

import lombok.Data;

@Data
public class AttendanceDetailVO {
    private String section;           // 工段
    private String name;              // 姓名
    private String employeeId;        // 工号
    private String type;              // 类型
    private String meetingRollCall;   // 班会点名
    private String outSupportSection; // 外出支援工段
    private String isReceive;         // 是否接收
    private String support;           // 支援
    private String esdSubmitter;      // ESD状态提交人
}
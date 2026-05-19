// AttendanceDaily.java
package com.example.attendance.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceDaily {
    private Long id;
    private Long employeeId;
    private LocalDate date;
    private String shift; // day / night
    private Boolean isActualAttendance;
    private Boolean isInventory;
    private Boolean receiveFlag;
    private Boolean supportFlag;
    private Long outSupportDeptId;
    private String meetingRollCall;
    private LocalDateTime createdAt;
}
// AttendanceQueryDTO.java
package com.example.attendance.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class AttendanceQueryDTO {
    private Long deptId;          // 当前选中部门ID
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;       // 查询日期
    private String shiftType;     // all / day / night
}